package net.spy.memcached.protocol.binary;

import net.spy.memcached.ops.KeyedOperation;
import net.spy.memcached.ops.OperationCallback;

public class ObserveOperationImpl extends SingleKeyOperationImpl
  implements KeyedOperation {

  static final byte OBS_CMD = (byte) 0xb1;

  /**
   * Length of the extra header stuff for a GET response.
   */
  static final int EXTRA_HDR_LEN = 0;

  private final long cas;
  private final long exp;
  private final String obsSet;

  public ObserveOperationImpl(String key, long c, long e, String os, OperationCallback cb) {
    super(OBS_CMD, generateOpaque(), key, cb);
    cas = c;
    exp = e;
    obsSet = os;
  }

  @Override
  public void initialize() {
    prepareBuffer(key, cas, obsSet.getBytes(), exp);
  }
}
