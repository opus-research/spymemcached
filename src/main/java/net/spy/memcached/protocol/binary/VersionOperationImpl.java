/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.binary;

import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.VersionOperation;

class VersionOperationImpl extends OperationImpl implements VersionOperation {

  private static final int CMD = 0x0b;

  public VersionOperationImpl(OperationCallback cb) {
    super(CMD, generateOpaque(), cb);
  }

  @Override
  public void initialize() {
    prepareBuffer("", 0, EMPTY_BYTES);
  }

  @Override
  protected void decodePayload(byte[] pl) {
    getCallback().receivedStatus(new OperationStatus(true, new String(pl)));
  }

}
