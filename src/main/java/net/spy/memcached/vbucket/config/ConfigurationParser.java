/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket.config;

import java.text.ParseException;
import java.util.Map;

/**
 * A ConfigParser.
 */
public interface ConfigurationParser {
  Map<String, Pool> parseBase(final String base) throws ParseException;

  Map<String, Bucket> parseBuckets(String buckets) throws ParseException;

  Bucket parseBucket(String sBucket) throws ParseException;

  void loadPool(Pool pool, String sPool) throws ParseException;
}
