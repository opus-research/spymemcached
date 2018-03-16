package net.spy.memcached.ops;

import net.spy.memcached.MemcachedNode;

import java.util.Collection;

public interface VBucketAware {
    boolean setVBucket(String key, short vbucket);
    short getVBucket(String key);
    Collection<MemcachedNode> getNotMyVbucketNodes();
    void addNotMyVbucketNode(MemcachedNode node);
    void setNotMyVbucketNodes(Collection<MemcachedNode> nodes);
}
