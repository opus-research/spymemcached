package net.spy.memcached.vbucket.config;

public class VBucket {

    public final static int MAX_REPLICAS = 4;

    public final static int MAX_BUCKETS = 65536;

    private final int[] servers = new int[MAX_REPLICAS + 1];

    public int[] getServers() {
        return servers;
    }
}
