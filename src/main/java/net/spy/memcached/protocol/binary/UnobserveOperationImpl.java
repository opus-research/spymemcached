package net.spy.memcached.protocol.binary;

import net.spy.memcached.ops.KeyedOperation;
import net.spy.memcached.ops.OperationCallback;

public class UnobserveOperationImpl extends SingleKeyOperationImpl
  implements KeyedOperation {

  static final byte UNOBS_CMD = (byte) 0xb2;

  /**
   * Length of the extra header stuff for a GET response.
   */
  static final int EXTRA_HDR_LEN = 0;

  private final long cas;
  private final String obsSet;

  public UnobserveOperationImpl(String key, long c, String os, OperationCallback cb) {
    super(UNOBS_CMD, generateOpaque(), key, cb);
    cas = c;
    obsSet = os;
  }

  @Override
  public void initialize() {
    prepareBuffer(key, cas, obsSet.getBytes());
  }
}
