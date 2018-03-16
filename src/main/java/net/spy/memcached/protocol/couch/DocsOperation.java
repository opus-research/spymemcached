/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

import net.spy.memcached.ops.OperationCallback;

/**
 * An operation that represents a view that calls the map
 * function and includes the documents in the result.
 */
public interface DocsOperation {

  /**
   * Callback for the result of the DocsOperation.
   */
  interface DocsCallback extends OperationCallback {
    void gotData(ViewResponseWithDocs response);
  }
}
