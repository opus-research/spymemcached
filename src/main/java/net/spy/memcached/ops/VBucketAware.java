package net.spy.memcached.ops;

import net.spy.memcached.MemcachedNode;

import java.util.Collection;

public interface VBucketAware {
    void setVBucket(int vbucket);
    Collection<MemcachedNode<Operation>> getNotMyVbucketNodes();
    void addNotMyVbucketNode(MemcachedNode<Operation> node);
    void setNotMyVbucketNodes(Collection<MemcachedNode<Operation>> nodes);
}
