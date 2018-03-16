/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spy.memcached.protocol.binary;

import net.spy.memcached.ops.UnlockOperation;
import net.spy.memcached.ops.OperationCallback;

class UnlockOperationImpl extends SingleKeyOperationImpl implements
    UnlockOperation {

  private static final byte CMD = (byte) 0x95;

  private final long cas;

  public UnlockOperationImpl(String k, OperationCallback cb) {
    this(k, 0, cb);
  }

  public UnlockOperationImpl(String k, long c,
          OperationCallback cb) {
    super(CMD, generateOpaque(), k, cb);
    cas = c;
  }

  @Override
  public void initialize() {
    prepareBuffer(key, cas, EMPTY_BYTES);
  }

  @Override
  public String toString() {
    return super.toString() + " Cas: " + cas;
  }
}
