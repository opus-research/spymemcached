/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.binary;

import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.SASLAuthOperation;

/**
 * SASL authenticator.
 */
public class SASLAuthOperationImpl extends SASLBaseOperationImpl implements
    SASLAuthOperation {

  private static final int CMD = 0x21;

  public SASLAuthOperationImpl(String[] m, String s, Map<String, ?> p,
      CallbackHandler h, OperationCallback c) {
    super(CMD, m, EMPTY_BYTES, s, p, h, c);
  }

  @Override
  protected byte[] buildResponse(SaslClient sc) throws SaslException {
    return sc.hasInitialResponse() ? sc.evaluateChallenge(challenge)
        : EMPTY_BYTES;
  }
}
