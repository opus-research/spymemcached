package net.spy.memcached.vbucket;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.NodeLocator;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.vbucket.config.Config;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.net.InetSocketAddress;

/**
 * Implementation of the {@link NodeLocator} interface that contains vbucket hashing methods
 */
public class VBucketNodeLocator<T> extends SpyObject implements NodeLocator<T> {

    private Map<String, MemcachedNode<T>> nodesMap;

    private Config config;

    /**
     * Construct a VBucketNodeLocator over the given JSON configuration string.
     *
     * @param nodes
     * @param jsonConfig
     */
    public VBucketNodeLocator(List<MemcachedNode<T>> nodes, Config jsonConfig) {
        super();
        setNodes(nodes);
        setConfig(jsonConfig);
    }

    /**
     * {@inheritDoc}
     */
    public MemcachedNode<T> getPrimary(String k) {
        int vbucket = config.getVbucketByKey(k);
        int serverNumber = config.getMaster(vbucket);
        String server = config.getServer(serverNumber);
        // choose appropriate MemecachedNode according to config data
        MemcachedNode<T> pNode = nodesMap.get(server);
        if (pNode == null) {
            getLogger().error("The node locator does not have a primary for key %s.", k);
            Collection<MemcachedNode<T>> nodes = nodesMap.values();
            getLogger().error("MemcachedNode has %s entries:", nodesMap.size());
            for (MemcachedNode<T> node : nodes) {
                getLogger().error(node);
            }
        }
        assert (pNode != null);
        return pNode;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<MemcachedNode<T>> getSequence(String k) {
        return nodesMap.values().iterator();
    }

    /**
     * {@inheritDoc}
     */
    public Collection<MemcachedNode<T>> getAll() {
        return this.nodesMap.values();
    }

    /**
     * {@inheritDoc}
     */
    public NodeLocator<T> getReadonlyCopy() {
        return this;
    }
    public void updateLocator(final List<MemcachedNode<T>> nodes, final Config config) {
        setNodes(nodes);
        setConfig(config);
    }

    /**
     * Returns a vbucket index for the given key
     * @param key the key
     * @return vbucket index
     */
    public int getVBucketIndex(String key) {
        return config.getVbucketByKey(key);
    }

    private void setNodes(Collection<MemcachedNode<T>> nodes) {
        HashMap<String, MemcachedNode<T>> vbnodesMap = new HashMap<String, MemcachedNode<T>>();
        getLogger().debug("Updating nodesMap in VBucketNodeLocator.");
        for (MemcachedNode<T> node : nodes) {
            InetSocketAddress addr = (InetSocketAddress) node.getSocketAddress();
            String address = addr.getAddress().getHostName() + ":" + addr.getPort();
	    String hostname = addr.getAddress().getHostAddress() + ":" + addr.getPort();
	    getLogger().debug("Adding node with hostname %s and address %s.", hostname, address);
	    getLogger().debug("Node added is %s.", node);
            vbnodesMap.put(address, node);
	    vbnodesMap.put(hostname, node);
        }

        this.nodesMap = vbnodesMap;
    }

    private void setConfig(final Config config) {
        this.config = config;
    }

    /**
     * Method returns the node that is not contained in the specified collection of the failed nodes
     * @param k the key
     * @param notMyVbucketNodes a collection of the nodes are excluded
     * @return The first MemcachedNode which meets requirements
     */
    public MemcachedNode<T> getAlternative(String k, Collection<MemcachedNode<T>> notMyVbucketNodes) {
        Collection<MemcachedNode<T>> nodes = nodesMap.values();
        nodes.removeAll(notMyVbucketNodes);
        if (nodes.isEmpty()) {
            return null;
        } else {
            return nodes.iterator().next();
        }
    }
}
