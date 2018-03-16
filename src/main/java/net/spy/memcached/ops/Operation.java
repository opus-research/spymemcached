package net.spy.memcached.ops;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.spy.memcached.MemcachedNode;


/**
 * Base interface for all operations.
 */
public interface Operation {

	/**
	 * Has this operation been cancelled?
	 */
	boolean isCancelled();

	/**
	 * True if an error occurred while processing this operation.
	 */
	boolean hasErrored();

	/**
	 * Get the exception that occurred (or null if no exception occurred).
	 */
	OperationException getException();

	/**
	 * Get the callback for this get operation.
	 */
	OperationCallback getCallback();

	/**
	 * Cancel this operation.
	 */
	void cancel();

	/**
	 * Get the current state of this operation.
	 */
	OperationState getState();

	/**
	 * Get the write buffer for this operation.
	 */
	ByteBuffer getBuffer();

	/**
	 * Invoked after having written all of the bytes from the supplied output
	 * buffer.
	 */
	void writeComplete();

	/**
	 * Initialize this operation.  This is used to prepare output byte buffers
	 * and stuff.
	 */
	void initialize();

	/**
	 * Read data from the given byte buffer and dispatch to the appropriate
	 * read mechanism.
	 */
	void readFromBuffer(ByteBuffer data) throws IOException;

	/**
	 * Handle a raw data read.
	 */
	void handleRead(ByteBuffer data);

	/**
	 * Get the node that should've been handling this operation.
	 */
	MemcachedNode getHandlingNode();

	/**
	 * Set a reference to the node that will be/is handling this operation.
	 *
	 * @param to a memcached node
	 */
	void setHandlingNode(MemcachedNode to);

	/**
	 * Mark this operation as one which has exceeded its timeout value.
	 */
	public void timeOut();

	/**
	 * True if the operation has timed out.
	 *
	 * <p>A timed out operation may or may not have been sent to the server
	 * already, but it exceeded either the specified or the default timeout
	 * value.
	 */
	public boolean isTimedOut();

	/**
	 * True if the operation has timed out.
	 *
	 * The ttl allows the caller to specify how long the operation should
	 * have been given since its creation, returning true if the operation
	 * has exceeded that time period.
	 *
	 * <p>A timed out operation may or may not have been sent to the server
	 * already, but it exceeded either the specified or the default timeout
	 * value.
	 *
	 * <p>In the rare case this may be called with a longer timeout value
	 * after having been called with a shorter value that caused the
	 * operation to be timed out, an IllegalArgumentException may be thrown.
	 *
	 * @param ttlMillis the max amount of time an operation may have existed
	 * since its creation in milliseconds.
	 */
	public boolean isTimedOut(long ttlMillis);

	public boolean isTimedOutUnsent();
}
