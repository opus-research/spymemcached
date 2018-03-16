/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.binary;

import net.spy.memcached.ops.FlushOperation;
import net.spy.memcached.ops.OperationCallback;

class FlushOperationImpl extends OperationImpl implements FlushOperation {

  private static final int CMD = 0x08;
  private final int delay;

  public FlushOperationImpl(OperationCallback cb) {
    this(0, cb);
  }

  public FlushOperationImpl(int d, OperationCallback cb) {
    super(CMD, generateOpaque(), cb);
    delay = d;
  }

  @Override
  public void initialize() {
    prepareBuffer("", 0, EMPTY_BYTES, delay);
  }
}
