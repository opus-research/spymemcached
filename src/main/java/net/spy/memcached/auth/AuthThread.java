/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2011 Couchbase, Inc.
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.spy.memcached.KeyUtil;
import net.spy.memcached.MemcachedConnection;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.OperationFactory;
import net.spy.memcached.compat.SpyThread;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.SASLMechsOperation;

/**
 * A thread that does SASL authentication.
 */
public class AuthThread extends SpyThread {

  private final MemcachedConnection conn;
  private final AuthDescriptor authDescriptor;
  private final OperationFactory opFact;
  private final MemcachedNode node;

  public AuthThread(MemcachedConnection c, OperationFactory o,
      AuthDescriptor a, MemcachedNode n) {
    conn = c;
    opFact = o;
    authDescriptor = a;
    node = n;
    start();
  }

  /**
   * Determine the best (strongest) authentication method supported by the
   * client and the server.
   *
   * <p>As of now, MD5-CRAM is used if supported, otherwise the fallback
   * method is PLAIN.</p>
   *
   * @param types a stringified list of supported mechs by the server.
   * @return the determined auth type.
   */
  private AuthType determineAuthType(String types) {
    if(types.contains("CRAM-MD5")) {
      return AuthType.CRAM_MD5;
    } else if(types.contains("PLAIN")) {
      return AuthType.PLAIN;
    } else {
      getLogger().warn("Received unknown SASL auth mechanism: " + types);
    }
    return null;
  }

  private AuthType listSupportedSASLMechanisms(AtomicBoolean done) {
    final CountDownLatch listMechsLatch = new CountDownLatch(1);
    final AtomicReference<AuthType> authType = new AtomicReference<AuthType>();
    Operation listMechsOp = opFact.saslMechs(new OperationCallback() {
      @Override
      public void receivedStatus(OperationStatus status) {
        if(status.isSuccess()) {
          authType.set(determineAuthType(status.getMessage()));
          getLogger().debug("Received SASL supported mechs: "
            + status.getMessage());
        }
      }

      @Override
      public void complete() {
        listMechsLatch.countDown();
      }

    });

    conn.insertOperation(node, listMechsOp);

    try {
      listMechsLatch.await(10, TimeUnit.SECONDS);
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

    return authType.get();
  }

  private void doPlainAuth(final AtomicBoolean done,
    OperationStatus priorStatus) {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicReference<OperationStatus> foundStatus =
      new AtomicReference<OperationStatus>();

    final OperationCallback cb = new OperationCallback() {

      @Override
      public void receivedStatus(OperationStatus val) {
        // If the status we found was null, we're done.
        if (val.getMessage().length() == 0) {
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
    final Operation op = buildOperation(priorStatus, cb);
    conn.insertOperation(node, op);

    try {
      latch.await();
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

    // Get the new status to inspect it.
    priorStatus = foundStatus.get();
    if (priorStatus != null) {
      if (!priorStatus.isSuccess()) {
        getLogger().warn(
            "Authentication failed to " + node.getSocketAddress());
      }
    }
  }

  private void doCramMD5Auth(AtomicBoolean done) {
    System.out.println("now its time for cram md5");
    done.set(true);
  }

  @Override
  public void run() {
    final AtomicBoolean done = new AtomicBoolean();

    AuthType chosenMechanism = listSupportedSASLMechanisms(done);

    while (!done.get()) {
      if(chosenMechanism == AuthType.PLAIN) {
        OperationStatus priorStatus = null;
        doPlainAuth(done, priorStatus);
      } else if(chosenMechanism == AuthType.CRAM_MD5) {
        doCramMD5Auth(done);
      } else {
        throw new IllegalStateException("Unhandled SASL Auth mechanism found: "
          + chosenMechanism);
      }
    }
  }

  private Operation buildOperation(OperationStatus st, OperationCallback cb) {
    if (st == null) {
      return opFact.saslAuth(authDescriptor.getMechs(),
          node.getSocketAddress().toString(), null,
          authDescriptor.getCallback(), cb);
    } else {
      return opFact.saslStep(authDescriptor.getMechs(), KeyUtil.getKeyBytes(
          st.getMessage()), node.getSocketAddress().toString(), null,
          authDescriptor.getCallback(), cb);
    }
  }
}
