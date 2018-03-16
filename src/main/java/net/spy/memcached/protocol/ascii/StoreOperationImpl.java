/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.ascii;

import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.StoreOperation;
import net.spy.memcached.ops.StoreType;

/**
 * Operation to store data in a memcached server.
 */
final class StoreOperationImpl extends BaseStoreOperationImpl implements
    StoreOperation {

  private final StoreType storeType;

  public StoreOperationImpl(StoreType t, String k, int f, int e, byte[] d,
      OperationCallback cb) {
    super(t.name(), k, f, e, d, cb);
    storeType = t;
  }

  public StoreType getStoreType() {
    return storeType;
  }
}
