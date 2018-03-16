package net.spy.memcached;

import net.spy.memcached.vbucket.config.Config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * NodeLocator implementation for dealing with simple array lookups using a
 * modulus of the hash code and node list length.
 */
public final class ArrayModNodeLocator implements NodeLocator {

	private MemcachedNode[] nodes;

	private final HashAlgorithm hashAlg;

	/**
	 * Construct an ArraymodNodeLocator over the given array of nodes and
	 * using the given hash algorithm.
	 *
	 * @param n the array of nodes
	 * @param alg the hash algorithm
	 */
	public ArrayModNodeLocator(List<MemcachedNode> n, HashAlgorithm alg) {
		super();
		nodes=n.toArray(new MemcachedNode[n.size()]);
		hashAlg=alg;
	}

	private ArrayModNodeLocator(MemcachedNode[] n, HashAlgorithm alg) {
		super();
		nodes=n;
		hashAlg=alg;
	}

	public Collection<MemcachedNode> getAll() {
		return Arrays.asList(nodes);
	}

	public MemcachedNode getPrimary(String k) {
		return nodes[getServerForKey(k)];
	}

	public Iterator<MemcachedNode> getSequence(String k) {
		return new NodeIterator(getServerForKey(k));
	}

	public NodeLocator getReadonlyCopy() {
		MemcachedNode[] n=new MemcachedNode[nodes.length];
		for(int i=0; i<nodes.length; i++) {
			n[i] = new MemcachedNodeROImpl(nodes[i]);
		}
		return new ArrayModNodeLocator(n, hashAlg);
	}

	@Override
	public void updateLocator(List<MemcachedNode> nodes, Config conf) {
		this.nodes=nodes.toArray(new MemcachedNode[nodes.size()]);
	}

	private int getServerForKey(String key) {
		int rv=(int)(hashAlg.hash(key) % nodes.length);
		assert rv >= 0 : "Returned negative key for key " + key;
		assert rv < nodes.length
			: "Invalid server number " + rv + " for key " + key;
		return rv;
	}

	class NodeIterator implements Iterator<MemcachedNode> {

		private final int start;
		private int next=0;

		public NodeIterator(int keyStart) {
			start=keyStart;
			next=start;
			computeNext();
			assert next >= 0 || nodes.length == 1
				: "Starting sequence at " + start + " of "
					+ nodes.length + " next is " + next;
		}

		public boolean hasNext() {
			return next >= 0;
		}

		private void computeNext() {
			if(++next >= nodes.length) {
				next=0;
			}
			if(next == start) {
				next=-1;
			}
		}

		public MemcachedNode next() {
			try {
				return nodes[next];
			} finally {
				computeNext();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException("Can't remove a node");
		}

	}
}
