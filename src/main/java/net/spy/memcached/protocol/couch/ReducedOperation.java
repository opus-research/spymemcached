/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

import net.spy.memcached.ops.OperationCallback;

/**
 * An operation that represents a view that calls the map
 * function and the reduce function and gets the result.
 */
public interface ReducedOperation {

  /**
   * A ReducedCallback.
   */
  interface ReducedCallback extends OperationCallback {
    void gotData(ViewResponseReduced response);
  }

}
