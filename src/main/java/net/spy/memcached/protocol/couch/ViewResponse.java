/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

import java.util.Collection;

/**
 * Holds the response of a queried view.
 */
public interface ViewResponse<T> extends Iterable<T> {
  Collection<RowError> getErrors();

  int size();
}
