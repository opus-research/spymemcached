package net.spy.memcached.protocol;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.compat.SpyObject;

/**
 * Represents a node with the memcached cluster, along with buffering and
 * operation queues.
 */
public abstract class TCPNodeImpl<T> extends SpyObject
	implements MemcachedNode<T> {

	private final SocketAddress socketAddress;
	private final ByteBuffer rbuf;
	private final ByteBuffer wbuf;
	protected final BlockingQueue<T> writeQ;
	protected final BlockingQueue<T> readQ;
	protected final BlockingQueue<T> inputQueue;
	// This has been declared volatile so it can be used as an availability
	// indicator.
	private volatile int reconnectAttempt=1;
	private SocketChannel channel;
	protected int toWrite=0;
	protected T optimizedOp=null;
	private volatile SelectionKey sk=null;
	protected final boolean shouldAuth;
	protected CountDownLatch authLatch;
	private ArrayList<T> reconnectBlocked;

	// operation Future.get timeout counter
	private final AtomicInteger continuousTimeout = new AtomicInteger(0);


	public TCPNodeImpl(SocketAddress sa, SocketChannel c,
			int bufSize, BlockingQueue<T> rq,
			BlockingQueue<T> wq, BlockingQueue<T> iq,
			boolean waitForAuth) {
		super();
		assert sa != null : "No SocketAddress";
		assert c != null : "No SocketChannel";
		assert bufSize > 0 : "Invalid buffer size: " + bufSize;
		assert rq != null : "No operation read queue";
		assert wq != null : "No operation write queue";
		assert iq != null : "No input queue";
		socketAddress=sa;
		setChannel(c);
		rbuf=ByteBuffer.allocate(bufSize);
		wbuf=ByteBuffer.allocate(bufSize);
		getWbuf().clear();
		readQ=rq;
		writeQ=wq;
		inputQueue=iq;
		shouldAuth = waitForAuth;
		setupForAuth();
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#copyInputQueue()
	 */
	public final void copyInputQueue() {
		Collection<T> tmp=new ArrayList<T>();

		// don't drain more than we have space to place
		inputQueue.drainTo(tmp, writeQ.remainingCapacity());

		writeQ.addAll(tmp);
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#destroyInputQueue()
	 */
	public Collection<T> destroyInputQueue() {
		Collection<T> rv=new ArrayList<T>();
		inputQueue.drainTo(rv);
		return rv;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#transitionWriteItem()
	 */
	public final void transitionWriteItem() {
		T op=removeCurrentWriteOp();
		assert op != null : "There is no write item to transition";
		getLogger().debug("Finished writing %s", op);
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#optimize()
	 */
	protected abstract void optimize();

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getCurrentReadOp()
	 */
	public final T getCurrentReadOp() {
		return readQ.peek();
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#removeCurrentReadOp()
	 */
	public final T removeCurrentReadOp() {
		return readQ.remove();
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getCurrentWriteOp()
	 */
	public final T getCurrentWriteOp() {
		return optimizedOp == null ? writeQ.peek() : optimizedOp;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#removeCurrentWriteOp()
	 */
	public final T removeCurrentWriteOp() {
		T rv=optimizedOp;
		if(rv == null) {
			rv=writeQ.remove();
		} else {
			optimizedOp=null;
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#hasReadOp()
	 */
	public final boolean hasReadOp() {
		return !readQ.isEmpty();
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#hasWriteOp()
	 */
	public final boolean hasWriteOp() {
		return !(optimizedOp == null && writeQ.isEmpty());
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#insertOp(net.spy.memcached.ops.Operation)
	 */
	public final void insertOp(T op) {
		ArrayList<T> tmp = new ArrayList<T>(
				inputQueue.size() + 1);
		tmp.add(op);
		inputQueue.drainTo(tmp);
		inputQueue.addAll(tmp);
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getSelectionOps()
	 */
	public final int getSelectionOps() {
		int rv=0;
		if(getChannel().isConnected()) {
			if(hasReadOp()) {
				rv |= SelectionKey.OP_READ;
			}
			if(toWrite > 0 || hasWriteOp()) {
				rv |= SelectionKey.OP_WRITE;
			}
		} else {
			rv = SelectionKey.OP_CONNECT;
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getRbuf()
	 */
	public final ByteBuffer getRbuf() {
		return rbuf;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getWbuf()
	 */
	public final ByteBuffer getWbuf() {
		return wbuf;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getSocketAddress()
	 */
	public final SocketAddress getSocketAddress() {
		return socketAddress;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#isActive()
	 */
	public final boolean isActive() {
		return reconnectAttempt == 0
			&& getChannel() != null && getChannel().isConnected();
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#reconnecting()
	 */
	public final void reconnecting() {
		reconnectAttempt++;
		continuousTimeout.set(0);
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#connected()
	 */
	public final void connected() {
		reconnectAttempt=0;
		continuousTimeout.set(0);
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getReconnectCount()
	 */
	public final int getReconnectCount() {
		return reconnectAttempt;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#toString()
	 */
	@Override
	public final String toString() {
		int sops=0;
		if(getSk()!= null && getSk().isValid()) {
			sops=getSk().interestOps();
		}
		int rsize=readQ.size() + (optimizedOp == null ? 0 : 1);
		int wsize=writeQ.size();
		int isize=inputQueue.size();
		return "{QA sa=" + getSocketAddress() + ", #Rops=" + rsize
			+ ", #Wops=" + wsize
			+ ", #iq=" + isize
			+ ", topRop=" + getCurrentReadOp()
			+ ", topWop=" + getCurrentWriteOp()
			+ ", toWrite=" + toWrite
			+ ", interested=" + sops + "}";
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#registerChannel(java.nio.channels.SocketChannel, java.nio.channels.SelectionKey)
	 */
	public final void registerChannel(SocketChannel ch, SelectionKey skey) {
		setChannel(ch);
		setSk(skey);
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#setChannel(java.nio.channels.SocketChannel)
	 */
	public final void setChannel(SocketChannel to) {
		assert channel == null || !channel.isOpen()
			: "Attempting to overwrite channel";
		channel = to;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getChannel()
	 */
	public final SocketChannel getChannel() {
		return channel;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#setSk(java.nio.channels.SelectionKey)
	 */
	public final void setSk(SelectionKey to) {
		sk = to;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getSk()
	 */
	public final SelectionKey getSk() {
		return sk;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getBytesRemainingInBuffer()
	 */
	public final int getBytesRemainingToWrite() {
		return toWrite;
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#writeSome()
	 */
	public final int writeSome() throws IOException {
		int wrote=channel.write(wbuf);
		assert wrote >= 0 : "Wrote negative bytes?";
		toWrite -= wrote;
		assert toWrite >= 0
			: "toWrite went negative after writing " + wrote
				+ " bytes for " + this;
		getLogger().debug("Wrote %d bytes", wrote);
		return wrote;
	}


	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#setContinuousTimeout
	 */
	public void setContinuousTimeout(boolean timedOut) {
		if (timedOut && isActive()) {
			continuousTimeout.incrementAndGet();
		} else {
			continuousTimeout.set(0);
		}
	}

	/* (non-Javadoc)
	 * @see net.spy.memcached.MemcachedNode#getContinuousTimeout
	 */
	public int getContinuousTimeout() {
		return continuousTimeout.get();
	}


	public final void fixupOps() {
		// As the selection key can be changed at any point due to node
		// failure, we'll grab the current volatile value and configure it.
		SelectionKey s = sk;
		if(s != null && s.isValid()) {
			int iops=getSelectionOps();
			getLogger().debug("Setting interested opts to %d", iops);
			s.interestOps(iops);
		} else {
			getLogger().debug("Selection key is not valid.");
		}
	}

	public final void authComplete() {
		if (reconnectBlocked != null && reconnectBlocked.size() > 0 ) {
		    inputQueue.addAll(reconnectBlocked);
		}
		authLatch.countDown();
	}

	public final void setupForAuth() {
		if (shouldAuth) {
			authLatch = new CountDownLatch(1);
			if (inputQueue.size() > 0) {
				reconnectBlocked = new ArrayList<T>(
				inputQueue.size() + 1);
				inputQueue.drainTo(reconnectBlocked);
			}
			assert(inputQueue.size() == 0);
			setupResend();
		} else {
			authLatch = new CountDownLatch(0);
		}
	}

}
