/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

/**
 * Type of mutation to perform.
 */
public enum Mutator {
  /**
   * Increment a value on the memcached server.
   */
  incr,
  /**
   * Decrement a value on the memcached server.
   */
  decr
}
