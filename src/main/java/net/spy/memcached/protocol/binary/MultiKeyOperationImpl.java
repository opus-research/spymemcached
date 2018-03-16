package net.spy.memcached.protocol.binary;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.KeyedOperation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.VBucketAware;

abstract class MultiKeyOperationImpl extends OperationImpl
		implements VBucketAware, KeyedOperation {
	protected final Map<String, Short> vbmap = new HashMap<String, Short>();

	protected MultiKeyOperationImpl(int c, int o, OperationCallback cb) {
		super(c, o, cb);
	}

	public Collection<String> getKeys() {
		return vbmap.keySet();
	}

	public Collection<MemcachedNode> getNotMyVbucketNodes() {
		return notMyVbucketNodes;
	}

	public void addNotMyVbucketNode(MemcachedNode node) {
		notMyVbucketNodes.add(node);
	}

	public void setNotMyVbucketNodes(Collection<MemcachedNode> nodes) {
		notMyVbucketNodes = nodes;
	}

	public boolean setVBucket(String k, short vb) {
		if (vbmap.containsKey(k)) {
			vbmap.put(k, new Short(vb));
			return true;
		}
		return false;
	}

	public short getVBucket(String k) {
		assert vbmap.containsKey(k) : "Key " + k + " not contained in operation" ;
		return vbmap.get(k);
	}
}
