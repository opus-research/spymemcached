package net.spy.memcached;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.util.DefaultKetamaNodeLocatorConfiguration;
import net.spy.memcached.util.KetamaNodeLocatorConfiguration;

/**
 * This is an implementation of the Ketama consistent hash strategy from
 * last.fm.  This implementation may not be compatible with libketama as
 * hashing is considered separate from node location.
 *
 * Note that this implementation does not currently supported weighted nodes.
 *
 * @see <a href="http://www.last.fm/user/RJ/journal/2007/04/10/392555/">RJ's blog post</a>
 */
public final class KetamaNodeLocator<T> extends SpyObject implements NodeLocator<T> {


	private volatile TreeMap<Long, MemcachedNode<T>> ketamaNodes;
	final Collection<MemcachedNode<T>> allNodes;

	final HashAlgorithm hashAlg;
	final KetamaNodeLocatorConfiguration<T> config;


	/**
	 * Create a new KetamaNodeLocator using specified nodes and the specifed hash algorithm.
	 *
	 * @param nodes The List of nodes to use in the Ketama consistent hash continuum
	 * @param alg The hash algorithm to use when choosing a node in the Ketama consistent hash continuum
	 */
	public KetamaNodeLocator(List<MemcachedNode<T>> nodes, HashAlgorithm alg) {
        this(nodes, alg, new DefaultKetamaNodeLocatorConfiguration<T>());
	}

	/**
	 * Create a new KetamaNodeLocator using specified nodes and the specifed hash algorithm and configuration.
	 *
	 * @param nodes The List of nodes to use in the Ketama consistent hash continuum
	 * @param alg The hash algorithm to use when choosing a node in the Ketama consistent hash continuum
	 * @param conf
	 */
	public KetamaNodeLocator(List<MemcachedNode<T>> nodes, HashAlgorithm alg, KetamaNodeLocatorConfiguration<T> conf) {
		super();
		allNodes = nodes;
		hashAlg = alg;
		config = conf;

		setKetamaNodes(nodes);

    }

	private KetamaNodeLocator(TreeMap<Long, MemcachedNode<T>> smn,
			Collection<MemcachedNode<T>> an, HashAlgorithm alg, KetamaNodeLocatorConfiguration<T> conf) {
		super();
		ketamaNodes=smn;
		allNodes=an;
		hashAlg=alg;
        config=conf;
	}

	public Collection<MemcachedNode<T>> getAll() {
		return allNodes;
	}

	public MemcachedNode<T> getPrimary(final String k) {
		MemcachedNode<T> rv=getNodeForKey(hashAlg.hash(k));
		assert rv != null : "Found no node for key " + k;
		return rv;
	}

	long getMaxKey() {
		return getKetamaNodes().lastKey();
	}

	MemcachedNode<T> getNodeForKey(long hash) {
		final MemcachedNode<T> rv;
		if(!ketamaNodes.containsKey(hash)) {
			// Java 1.6 adds a ceilingKey method, but I'm still stuck in 1.5
			// in a lot of places, so I'm doing this myself.
			SortedMap<Long, MemcachedNode<T>> tailMap=getKetamaNodes().tailMap(hash);
			if(tailMap.isEmpty()) {
				hash=getKetamaNodes().firstKey();
			} else {
				hash=tailMap.firstKey();
			}
		}
		rv=getKetamaNodes().get(hash);
		return rv;
	}

	public Iterator<MemcachedNode<T>> getSequence(String k) {
		// Seven searches gives us a 1 in 2^7 chance of hitting the
		// same dead node all of the time.
		return new KetamaIterator<T>(k, 7, getKetamaNodes(), hashAlg);
	}

	public NodeLocator<T> getReadonlyCopy() {
		TreeMap<Long, MemcachedNode<T>> smn=new TreeMap<Long, MemcachedNode<T>>(
			getKetamaNodes());
		Collection<MemcachedNode<T>> an=
			new ArrayList<MemcachedNode<T>>(allNodes.size());

		// Rewrite the values a copy of the map.
		for(Map.Entry<Long, MemcachedNode<T>> me : smn.entrySet()) {
			me.setValue(new MemcachedNodeROImpl<T>(me.getValue()));
		}
		// Copy the allNodes collection.
		for(MemcachedNode<T> n : allNodes) {
			an.add(new MemcachedNodeROImpl<T>(n));
		}

		return new KetamaNodeLocator<T>(smn, an, hashAlg, config);
	}

    /**
     * @return the ketamaNodes
     */
    protected TreeMap<Long, MemcachedNode<T>> getKetamaNodes() {
	return ketamaNodes;
    }

    /**
     * Setup the KetamaNodeLocator with the list of nodes it should use.
     *
     * @param nodes a List of MemcachedNodes for this KetamaNodeLocator to use in its continuum
     */
    protected void setKetamaNodes(List<MemcachedNode<T>> nodes) {
	TreeMap<Long, MemcachedNode<T>> newNodeMap = new TreeMap<Long, MemcachedNode<T>>();
	int numReps= config.getNodeRepetitions();
	for(MemcachedNode<T> node : nodes) {
		// Ketama does some special work with md5 where it reuses chunks.
		if(hashAlg == HashAlgorithm.KETAMA_HASH) {
			for(int i=0; i<numReps / 4; i++) {
				byte[] digest=HashAlgorithm.computeMd5(config.getKeyForNode(node, i));
				for(int h=0;h<4;h++) {
					Long k = ((long)(digest[3+h*4]&0xFF) << 24)
						| ((long)(digest[2+h*4]&0xFF) << 16)
						| ((long)(digest[1+h*4]&0xFF) << 8)
						| (digest[h*4]&0xFF);
					newNodeMap.put(k, node);
					getLogger().debug("Adding node %s in position %d", node, k);
				}

			}
		} else {
			for(int i=0; i<numReps; i++) {
				newNodeMap.put(hashAlg.hash(config.getKeyForNode(node, i)), node);
			}
		}
	}
	assert newNodeMap.size() == numReps * nodes.size();
	ketamaNodes = newNodeMap;

    }

}
