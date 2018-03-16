package net.spy.memcached.vbucket.config;

import java.util.List;

import net.spy.memcached.HashAlgorithm;

public class DefaultConfig implements Config {

    private HashAlgorithm hashAlgorithm = HashAlgorithm.NATIVE_HASH;

    private int vbucketsCount;

    private int mask;

    private int serversCount;

    private int replicasCount;

    private final List<String> servers;

    private final List<VBucket> vbuckets;

    public DefaultConfig(HashAlgorithm hashAlgorithm, int serversCount, int replicasCount,
		int vbucketsCount, List<String> servers, List<VBucket> vbuckets) {
        this.hashAlgorithm = hashAlgorithm;
        this.serversCount = serversCount;
        this.replicasCount = replicasCount;
        this.vbucketsCount = vbucketsCount;
        this.mask = vbucketsCount - 1;
        this.servers = servers;
        this.vbuckets = vbuckets;
    }

    public int getReplicasCount() {
        return replicasCount;
    }

    public int getVbucketsCount() {
        return vbucketsCount;
    }

    public int getServersCount() {
        return serversCount;
    }

    public String getServer(int serverIndex) {
        if (serverIndex > servers.size() - 1) {
            throw new IllegalArgumentException("Server index is out of bounds, index = "
                    + serverIndex + ", servers count = " + servers.size());
        }
        return servers.get(serverIndex);
    }

    public int getVbucketByKey(String key) {
        int digest = (int) hashAlgorithm.hash(key);
        return digest & mask;
    }

    public int getMaster(int vbucketIndex) {
        if (vbucketIndex > vbuckets.size() - 1) {
            throw new IllegalArgumentException("Vbucket index is out of bounds, index = "
                    + vbucketIndex + ", vbuckets count = " + vbuckets.size());
        }
        return vbuckets.get(vbucketIndex).getServers()[0];
    }

    public int getReplica(int vbucketIndex, int replicaIndex) {
        if (vbucketIndex > vbuckets.size() - 1) {
            throw new IllegalArgumentException("Vbucket index is out of bounds, index = "
                    + vbucketIndex + ", vbuckets count = " + vbuckets.size());
        }
        return vbuckets.get(vbucketIndex).getServers()[replicaIndex + 1];
    }

    public int foundIncorrectMaster(int vbucket, int wrongServer) {
        int mappedServer = this.vbuckets.get(vbucket).getServers()[0];
        int rv = mappedServer;
        if (mappedServer == wrongServer) {
            rv = (rv + 1) % this.serversCount;
            this.vbuckets.get(vbucket).getServers()[0] = rv;
        }
        return rv;
    }

    public List<String> getServers() {
        return servers;
    }

    public List<VBucket> getVbuckets() {
        return vbuckets;
    }

    public ConfigDifference compareTo(Config config) {
        ConfigDifference difference = new ConfigDifference();

        // Verify the servers are equal in their positions
        if (this.serversCount == config.getServersCount()) {
            difference.setSequenceChanged(false);
            for (int i = 0; i < this.serversCount; i++) {
                if (!this.getServer(i).equals(config.getServer(i))) {
                    difference.setSequenceChanged(true);
                    break;
                }
            }
        } else {
            // Just say yes
            difference.setSequenceChanged(true);
        }

        // Count the number of vbucket differences
        if (this.vbucketsCount == config.getVbucketsCount()) {
            int vbucketsChanges = 0;
            for (int i = 0; i < this.vbucketsCount; i++) {
                vbucketsChanges += (this.getMaster(i) == config.getMaster(i)) ? 0 : 1;
            }
            difference.setVbucketsChanges(vbucketsChanges);
        } else {
            difference.setVbucketsChanges(-1);
        }

        return difference;
    }

    public HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }


}
