/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

import net.spy.memcached.ops.OperationCallback;

/**
 * A ViewOperation.
 */
public interface ViewOperation {
  
  /**
   * A ViewCallback.
   */
  public interface ViewCallback extends OperationCallback {
    void gotData(View view);
  }
}
