/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

/**
 * MultiOperationCallback for get operations.
 */
public class MultiGetsOperationCallback extends MultiOperationCallback
    implements GetsOperation.Callback {

  public MultiGetsOperationCallback(OperationCallback original, int todo) {
    super(original, todo);
  }

  public void gotData(String key, int flags, long cas, byte[] data) {
    ((GetsOperation.Callback) originalCallback).gotData(key, flags, cas, data);
  }
}
