/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2014 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package net.spy.memcached.auth;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.spy.memcached.KeyUtil;
import net.spy.memcached.MemcachedConnection;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.OperationFactory;
import net.spy.memcached.compat.SpyThread;
import net.spy.memcached.compat.log.Level;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationStatus;

/**
 * SASL Authentication.
 */
public class AuthThread extends SpyThread {

  /**
   * If a SASL step takes longer than this period in miliseconds, a warning
   * will be issued.
   */
  public static final int AUTH_TIME_THRESHOLD = 250;

  /**
   * Separator used to split SASL mechanisms apart.
   */
  public static final String MECH_SEPARATOR = " ";

  /**
   * The connection to use for the SASL communication.
   */
  private final MemcachedConnection conn;

  /**
   * Contains callbacks and other information for auth.
   */
  private final AuthDescriptor authDescriptor;

  /**
   * The operation factory to generate the SASL ops.
   */
  private final OperationFactory opFact;

  /**
   * The target node to talk to.
   */
  private final MemcachedNode node;

  /**
   * Create a new {@link AuthThread} and start it.
   *
   * @param c the underlying connection.
   * @param o the operation factory.
   * @param a the auth descriptor.
   * @param n the remote node.
   */
  public AuthThread(MemcachedConnection c, OperationFactory o,
    AuthDescriptor a, MemcachedNode n) {
    conn = c;
    opFact = o;
    authDescriptor = a;
    node = n;
    start();
  }

  /**
   * Helper method to list the supported SASL mechanisms on the target node.
   *
   * @param done signalling the finish state of the sasl list process.
   * @return a list of SASL mechanisms.
   */
  protected String[] listSupportedSASLMechanisms(final AtomicBoolean done) {
    final CountDownLatch listMechsLatch = new CountDownLatch(1);
    final AtomicReference<String> supportedMechs =
      new AtomicReference<String>();

    Operation listMechsOp = opFact.saslMechs(new OperationCallback() {
      @Override
      public void receivedStatus(OperationStatus status) {
        if(status.isSuccess()) {
          supportedMechs.set(status.getMessage());
          getLogger().debug("Received SASL supported mechs: "
            + status.getMessage());
        }
      }

      @Override
      public void complete() {
        listMechsLatch.countDown();
      }
    });

    long saslRequestStart = System.currentTimeMillis();
    conn.insertOperation(node, listMechsOp);
    try {
      if (!conn.isShutDown()) {
        listMechsLatch.await();
      } else {
        done.set(true); // Connection is shutting down, tear down.
      }
    } catch(InterruptedException ex) {
      // we can be interrupted if we were in the
      // process of auth'ing and the connection is
      // lost or dropped due to bad auth
      Thread.currentThread().interrupt();
      if (listMechsOp != null) {
        listMechsOp.cancel();
      }
      done.set(true); // If we were interrupted, tear down.
    }
    long saslRequestDiff = System.currentTimeMillis() - saslRequestStart;
    String msg = String.format("SASL MechList Step on node %s took %dms",
      node.toString(), saslRequestDiff);
    Level level =
      saslRequestDiff >= AUTH_TIME_THRESHOLD ? Level.WARN : Level.DEBUG;
    getLogger().log(level, msg);

    String supported = supportedMechs.get();
    if (supported == null || supported.isEmpty()) {
      throw new IllegalStateException("Got empty SASL auth mech list.");
    }

    return supported.split(MECH_SEPARATOR);
  }

  @Override
  public void run() {
    final AtomicBoolean done = new AtomicBoolean();

    String[] supportedMechs;
    if (authDescriptor.getMechs() == null
      || authDescriptor.getMechs().length == 0) {
      supportedMechs = listSupportedSASLMechanisms(done);
    } else {
      supportedMechs = authDescriptor.getMechs();
    }

    OperationStatus priorStatus = null;
    while (!done.get()) {
      final CountDownLatch latch = new CountDownLatch(1);
      final AtomicReference<OperationStatus> foundStatus =
        new AtomicReference<OperationStatus>();

      final OperationCallback cb = new OperationCallback() {

        @Override
        public void receivedStatus(OperationStatus val) {
          // If the status we found was null, we're done.
          if (val.getMessage().isEmpty()) {
            done.set(true);
            node.authComplete();
            getLogger().info("Authenticated to " + node.getSocketAddress());
          } else {
            foundStatus.set(val);
          }
        }

        @Override
        public void complete() {
          latch.countDown();
        }
      };

      // Get the prior status to create the correct operation.
      final Operation op = buildOperation(priorStatus, cb, supportedMechs);
      long saslRequestStart = System.currentTimeMillis();
      conn.insertOperation(node, op);

      try {
        if (!conn.isShutDown()) {
          latch.await();
        } else {
          done.set(true); // Connection is shutting down, tear.down.
        }
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // we can be interrupted if we were in the
        // process of auth'ing and the connection is
        // lost or dropped due to bad auth
        Thread.currentThread().interrupt();
        if (op != null) {
          op.cancel();
        }
        done.set(true); // If we were interrupted, tear down.
      }
      long saslRequestDiff = System.currentTimeMillis() - saslRequestStart;

      String msg = String.format("SASL Auth Step on node %s took %dms",
        node.toString(), saslRequestDiff);
      Level level =
        saslRequestDiff >= AUTH_TIME_THRESHOLD ? Level.WARN : Level.DEBUG;
      getLogger().log(level, msg);

      // Get the new status to inspect it.
      priorStatus = foundStatus.get();
      if (priorStatus != null) {
        if (!priorStatus.isSuccess()) {
          getLogger().warn(
              "Authentication failed to " + node.getSocketAddress());
        }
      }
    }
  }

  private Operation buildOperation(OperationStatus st, OperationCallback cb,
    final String [] supportedMechs) {
    if (st == null) {
      return opFact.saslAuth(supportedMechs,
          node.getSocketAddress().toString(), null,
          authDescriptor.getCallback(), cb);
    } else {
      return opFact.saslStep(supportedMechs, KeyUtil.getKeyBytes(
          st.getMessage()), node.getSocketAddress().toString(), null,
          authDescriptor.getCallback(), cb);
    }
  }
}
