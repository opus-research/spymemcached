/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

/**
 * incr and decr operations.
 */
public interface MutatorOperation extends KeyedOperation {

  /**
   * Get the mutator type used for this operation.
   */
  Mutator getType();

  /**
   * Get the amount we're mutating by.
   */
  int getBy();

  /**
   * Get the default value (for when there's no value to mutate).
   */
  long getDefault();

  /**
   * Get the expiration to set in case of a new entry.
   */
  int getExpiration();
}
