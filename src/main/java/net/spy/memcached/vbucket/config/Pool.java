package net.spy.memcached.vbucket.config;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pool represents a collection of buckets
 */
public class Pool {
    // pool name
    private final String name;
    // pool's uri
    private final URI uri;
    // pool's streaming uri
    private final URI streamingUri;
    // buckets uri
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<String, Bucket>();
    // buckets related to this pool
    private URI bucketsUri;

    public Pool(String name, URI uri, URI streamingUri) {
        this.name = name;
        this.uri = uri;
        this.streamingUri = streamingUri;
    }

    public String getName() {
        return name;
    }

    public URI getUri() {
        return uri;
    }

    public URI getStreamingUri() {
        return streamingUri;
    }

    private Map<String, Bucket> getBuckets() {
        return buckets;
    }

    public URI getBucketsUri() {
        return bucketsUri;
    }

    void setBucketsUri(URI bucketsUri) {
        this.bucketsUri = bucketsUri;
    }
}
