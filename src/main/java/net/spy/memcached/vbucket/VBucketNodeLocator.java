/*
 * Copyright (c) 2009, NorthScale, Inc.
 *
 * All rights reserved.
 *
 * info@northscale.com
 *
 */

package net.spy.memcached.vbucket;

import com.northscale.jvbucket.Config;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.NodeLocator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.net.InetSocketAddress;

/**
 * @author Eugene Shelestovich
 */
public class VBucketNodeLocator implements NodeLocator {

    private Map<String, MemcachedNode> nodesMap;

    private Config config;

    /**
     * Construct a VBucketNodeLocator over the given JSON configuration string.
     *
     * @param nodes
     * @param jsonConfig
     */
    public VBucketNodeLocator(List<MemcachedNode> nodes, Config jsonConfig) {
        super();
        setNodes(nodes);
        setConfig(jsonConfig);
    }


    public MemcachedNode getPrimary(String k) {
        int vbucket = config.getVbucketByKey(k);
        int serverNumber = config.getMaster(vbucket);
        String server = config.getServer(serverNumber);
        // choose appropriate MemecachedNode according to config data
        return nodesMap.get(server);
    }

    public Iterator<MemcachedNode> getSequence(String k) {
        return nodesMap.values().iterator();
    }

    public Collection<MemcachedNode> getAll() {
        return this.nodesMap.values();
    }

    public NodeLocator getReadonlyCopy() {
        return this;
    }
    public void updateLocator(final List<MemcachedNode> nodes, final Config config) {
        setNodes(nodes);
        setConfig(config);
    }

    public int getVBucketIndex(String key) {
        return config.getVbucketByKey(key);
    }
    private void setNodes(Collection<MemcachedNode> nodes) {
        Map<String, MemcachedNode> nodesMap = new HashMap<String, MemcachedNode>();
        for (MemcachedNode node : nodes) {
            InetSocketAddress addr = (InetSocketAddress) node.getSocketAddress();
            String address = addr.getAddress().getHostAddress() + ":" + addr.getPort();
            nodesMap.put(address, node);
        }

        this.nodesMap = nodesMap;
    }
    private void setConfig(final Config config) {
        this.config = config;
    }
}
