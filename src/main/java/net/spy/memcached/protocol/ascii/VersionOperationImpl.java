/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.ascii;

import java.nio.ByteBuffer;

import net.spy.memcached.ops.NoopOperation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.VersionOperation;

/**
 * Operation to request the version of a memcached server.
 */
final class VersionOperationImpl extends OperationImpl implements
    VersionOperation, NoopOperation {

  private static final byte[] REQUEST = "version\r\n".getBytes();

  public VersionOperationImpl(OperationCallback c) {
    super(c);
  }

  @Override
  public void handleLine(String line) {
    assert line.startsWith("VERSION ");
    getCallback().receivedStatus(
        new OperationStatus(true, line.substring("VERSION ".length())));
    transitionState(OperationState.COMPLETE);
  }

  @Override
  public void initialize() {
    setBuffer(ByteBuffer.wrap(REQUEST));
  }
}
