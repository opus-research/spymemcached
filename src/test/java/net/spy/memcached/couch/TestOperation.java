/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.couch;

import net.spy.memcached.ops.OperationCallback;

/**
 * A TestOperation.
 */
public interface TestOperation {
  /**
   * A TestCallback.
   */
  interface TestCallback extends OperationCallback {
    void getData(String response);
  }
}
