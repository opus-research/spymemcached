/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket.config;

/**
 * Config may have types. These types are associated with the individual bucket
 * types as discerned from the configuration for the bucket served up by the
 * Membase server.
 */
public enum ConfigType {
  /**
   * Memcache bucket type.
   */
  MEMCACHE,
  /**
   * Membase bucket type.
   */
  MEMBASE;
}
