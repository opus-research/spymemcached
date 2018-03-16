/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

import java.util.List;

import net.spy.memcached.ops.OperationCallback;

/**
 * A ViewsOperation.
 */
public interface ViewsOperation {

  /**
   * A ViewsCallback.
   */
  interface ViewsCallback extends OperationCallback {
    void gotData(List<View> views);
  }
}
