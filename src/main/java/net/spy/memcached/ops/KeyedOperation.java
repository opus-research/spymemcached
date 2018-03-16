/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

import java.util.Collection;

/**
 * Operations that contain keys.
 */
public interface KeyedOperation extends Operation {

  /**
   * Get the keys requested in this GetOperation.
   */
  Collection<String> getKeys();
}
