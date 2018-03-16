package net.spy.memcached;

import java.util.Collection;
import java.util.Iterator;

/**
 * Interface for locating a node by hash value.
 */
public interface NodeLocator<T> {

	/**
	 * Get the primary location for the given key.
	 *
	 * @param k the object key
	 * @return the QueueAttachment containing the primary storage for a key
	 */
	MemcachedNode<T> getPrimary(String k);

	/**
	 * Get an iterator over the sequence of nodes that make up the backup
	 * locations for a given key.
	 *
	 * @param k the object key
	 * @return the sequence of backup nodes.
	 */
	Iterator<MemcachedNode<T>> getSequence(String k);

	/**
	 * Get all memcached nodes.  This is useful for broadcasting messages.
	 */
	Collection<MemcachedNode<T>> getAll();

	/**
	 * Create a read-only copy of this NodeLocator.
	 */
	NodeLocator<T> getReadonlyCopy();
}
