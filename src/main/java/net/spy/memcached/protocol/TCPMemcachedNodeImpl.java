package net.spy.memcached.protocol;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationState;

public abstract class TCPMemcachedNodeImpl extends TCPNodeImpl<Operation> {

	private final long opQueueMaxBlockTime;
	private long defaultOpTimeout;

	public TCPMemcachedNodeImpl(SocketAddress sa, SocketChannel c, int bufSize,
			BlockingQueue<Operation> rq, BlockingQueue<Operation> wq,
			BlockingQueue<Operation> iq, long opQueueMaxBlockTime,
			boolean waitForAuth, long dt) {
		super(sa, c, bufSize, rq, wq, iq, waitForAuth);
		this.opQueueMaxBlockTime = opQueueMaxBlockTime;
		defaultOpTimeout = dt;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#setupResend()
	 */
	public final void setupResend() {
		// First, reset the current write op, or cancel it if we should
		// be authenticating
		Operation op=getCurrentWriteOp();
		if(shouldAuth && op != null) {
		    op.cancel();
		} else if(op != null) {
			ByteBuffer buf=op.getBuffer();
			if(buf != null) {
				buf.reset();
			} else {
				getLogger().info("No buffer for current write op, removing");
				removeCurrentWriteOp();
			}
		}
		// Now cancel all the pending read operations.  Might be better to
		// to requeue them.
		while(hasReadOp()) {
			op=removeCurrentReadOp();
			if (op != getCurrentWriteOp()) {
				getLogger().warn("Discarding partially completed op: %s", op);
				op.cancel();
			}
		}

		while(shouldAuth && hasWriteOp()) {
			op=removeCurrentWriteOp();
			getLogger().warn("Discarding partially completed op: %s", op);
			op.cancel();
		}


		getWbuf().clear();
		getRbuf().clear();
		toWrite=0;
	}

	// Prepare the pending operations.  Return true if there are any pending
	// ops
	private boolean preparePending() {
		// Copy the input queue into the write queue.
		copyInputQueue();

		// Now check the ops
		Operation nextOp=getCurrentWriteOp();
		while(nextOp != null && nextOp.isCancelled()) {
			getLogger().info("Removing cancelled operation: %s", nextOp);
			removeCurrentWriteOp();
			nextOp=getCurrentWriteOp();
		}
		return nextOp != null;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#fillWriteBuffer(boolean)
	 */
	public final void fillWriteBuffer(boolean shouldOptimize) {
		if(toWrite == 0 && readQ.remainingCapacity() > 0) {
			getWbuf().clear();
			Operation o=getCurrentWriteOp();
			if (o != null && (o.isCancelled())) {
				getLogger().debug("Not writing cancelled op.");
				Operation cancelledOp = removeCurrentWriteOp();
				assert o == cancelledOp;
				return;
                        }
			if (o != null && o.isTimedOut(defaultOpTimeout)) {
				getLogger().debug("Not writing timed out op.");
				Operation timedOutOp = removeCurrentWriteOp();
				assert o == timedOutOp;
				return;
			}
			while(o != null && toWrite < getWbuf().capacity()) {
				assert o.getState() == OperationState.WRITING;
				// This isn't the most optimal way to do this, but it hints
				// at a larger design problem that may need to be taken care
				// if in the bowels of the client.
				// In practice, readQ should be small, however.
				if(!readQ.contains(o)) {
					readQ.add(o);
				}

				ByteBuffer obuf=o.getBuffer();
				assert obuf != null : "Didn't get a write buffer from " + o;
				int bytesToCopy=Math.min(getWbuf().remaining(),
						obuf.remaining());
				byte b[]=new byte[bytesToCopy];
				obuf.get(b);
				getWbuf().put(b);
				getLogger().debug("After copying stuff from %s: %s",
						o, getWbuf());
				if(!o.getBuffer().hasRemaining()) {
					o.writeComplete();
					transitionWriteItem();

					preparePending();
					if(shouldOptimize) {
						optimize();
					}

					o=getCurrentWriteOp();
				}
				toWrite += bytesToCopy;
			}
			getWbuf().flip();
			assert toWrite <= getWbuf().capacity()
				: "toWrite exceeded capacity: " + this;
			assert toWrite == getWbuf().remaining()
				: "Expected " + toWrite + " remaining, got "
				+ getWbuf().remaining();
		} else {
			getLogger().debug("Buffer is full, skipping");
		}
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#addOp(net.spy.memcached.ops.Operation)
	 */
	public final void addOp(Operation op) {
		try {
			if (!authLatch.await(1, TimeUnit.SECONDS)) {
			    op.cancel();
				getLogger().warn(
					"Operation canceled because authentication " +
					"or reconnection and authentication has " +
					"taken more than one second to complete.");
				getLogger().debug("Canceled operation %s", op.toString());
				return;
			}
			if(!inputQueue.offer(op, opQueueMaxBlockTime,
					TimeUnit.MILLISECONDS)) {
				throw new IllegalStateException("Timed out waiting to add "
						+ op + "(max wait=" + opQueueMaxBlockTime + "ms)");
			}
		} catch(InterruptedException e) {
			// Restore the interrupted status
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting to add "
					+ op);
		}
	}
}
