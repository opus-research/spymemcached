/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket.config;

import java.util.List;

import net.spy.memcached.HashAlgorithm;

/**
 * A Config.
 */
public interface Config {

  // Config access

  int getReplicasCount();

  int getVbucketsCount();

  int getServersCount();

  HashAlgorithm getHashAlgorithm();

  String getServer(int serverIndex);

  // VBucket access

  int getVbucketByKey(String key);

  int getMaster(int vbucketIndex);

  int getReplica(int vbucketIndex, int replicaIndex);

  int foundIncorrectMaster(int vbucket, int wrongServer);

  ConfigDifference compareTo(Config config);

  List<String> getServers();

  List<VBucket> getVbuckets();

  ConfigType getConfigType();

}
