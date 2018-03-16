package net.spy.memcached.vbucket;


import net.spy.memcached.vbucket.config.Bucket;

public interface ConfigurationProvider {
    Bucket getBucketConfiguration(String bucketname) throws ConfigurationException;
    void subscribe(final String bucketName, final Reconfigurable rec) throws ConfigurationException;
    void unsubscribe(final String bucketName, final Reconfigurable rec);

    void shutdown();

    String getAnonymousAuthBucket();
}
