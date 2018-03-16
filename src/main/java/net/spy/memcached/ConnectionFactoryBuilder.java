package net.spy.memcached;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationQueueFactory;
import net.spy.memcached.protocol.ascii.AsciiOperationFactory;
import net.spy.memcached.protocol.binary.BinaryOperationFactory;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.vbucket.VBucketNodeLocator;
import net.spy.memcached.vbucket.config.Config;

/**
 * Builder for more easily configuring a ConnectionFactory.
 */
public class ConnectionFactoryBuilder {

	private OperationQueueFactory opQueueFactory;
	private OperationQueueFactory readQueueFactory;
	private OperationQueueFactory writeQueueFactory;

	private Transcoder<Object> transcoder;

	private FailureMode failureMode;

	private Collection<ConnectionObserver> initialObservers
		= Collections.emptyList();

	private OperationFactory opFact;

	private Locator locator = Locator.ARRAY_MOD;
	private long opTimeout = -1;
	private boolean isDaemon = false;
	private boolean shouldOptimize = true;
	private boolean useNagle = false;
	private long maxReconnectDelay =
		DefaultConnectionFactory.DEFAULT_MAX_RECONNECT_DELAY;

	private int readBufSize = -1;
	private HashAlgorithm hashAlg;
	private AuthDescriptor authDescriptor = null;
	private long opQueueMaxBlockTime = -1;

	private int timeoutExceptionThreshold = DefaultConnectionFactory.DEFAULT_MAX_TIMEOUTEXCEPTION_THRESHOLD;
    private Config vBucketConfig;
	/**
	 * Set the operation queue factory.
	 */
	public ConnectionFactoryBuilder setOpQueueFactory(OperationQueueFactory q) {
		opQueueFactory = q;
		return this;
	}

	/**
	 * Set the read queue factory.
	 */
	public ConnectionFactoryBuilder setReadOpQueueFactory(OperationQueueFactory q) {
		readQueueFactory = q;
		return this;
	}

	/**
	 * Set the write queue factory.
	 */
	public ConnectionFactoryBuilder setWriteOpQueueFactory(OperationQueueFactory q) {
		writeQueueFactory = q;
		return this;
	}

	/**
	 * Set the maximum amount of time (in milliseconds) a client is willing to
	 * wait for space to become available in an output queue.
	 */
	public ConnectionFactoryBuilder setOpQueueMaxBlockTime(long t) {
		opQueueMaxBlockTime = t;
		return this;
	}

	/**
	 * Set the default transcoder.
	 */
	public ConnectionFactoryBuilder setTranscoder(Transcoder<Object> t) {
		transcoder = t;
		return this;
	}

	/**
	 * Set the failure mode.
	 */
	public ConnectionFactoryBuilder setFailureMode(FailureMode fm) {
		failureMode = fm;
		return this;
	}

	/**
	 * Set the initial connection observers (will observe initial connection).
	 */
	public ConnectionFactoryBuilder setInitialObservers(
			Collection<ConnectionObserver> obs) {
		initialObservers = obs;
		return this;
	}

	/**
	 * Set the operation factory.
	 *
	 * Note that the operation factory is used to also imply the type of
	 * nodes to create.
	 *
	 * @see MemcachedNode
	 */
	public ConnectionFactoryBuilder setOpFact(OperationFactory f) {
		opFact = f;
		return this;
	}

	/**
	 * Set the default operation timeout in milliseconds.
	 */
	public ConnectionFactoryBuilder setOpTimeout(long t) {
		opTimeout = t;
		return this;
	}

	/**
	 * Set the daemon state of the IO thread (defaults to true).
	 */
	public ConnectionFactoryBuilder setDaemon(boolean d) {
		isDaemon = d;
		return this;
	}

	/**
	 * Set to false if the default operation optimization is not desirable.
	 */
	public ConnectionFactoryBuilder setShouldOptimize(boolean o) {
		shouldOptimize = o;
		return this;
	}

	/**
	 * Set the read buffer size.
	 */
	public ConnectionFactoryBuilder setReadBufferSize(int to) {
		readBufSize = to;
		return this;
	}

	/**
	 * Set the hash algorithm.
	 */
	public ConnectionFactoryBuilder setHashAlg(HashAlgorithm to) {
		hashAlg = to;
		return this;
	}

	/**
	 * Set to true if you'd like to enable the Nagle algorithm.
	 */
	public ConnectionFactoryBuilder setUseNagleAlgorithm(boolean to) {
		useNagle = to;
		return this;
	}

	/**
	 * Convenience method to specify the protocol to use.
	 */
	public ConnectionFactoryBuilder setProtocol(Protocol prot) {
		switch(prot) {
			case TEXT:
				opFact = new AsciiOperationFactory();
				break;
			case BINARY:
				opFact = new BinaryOperationFactory();
				break;
			default: assert false : "Unhandled protocol: " + prot;
		}
		return this;
	}

	/**
	 * Set the locator type.
	 */
	public ConnectionFactoryBuilder setLocatorType(Locator l) {
		locator = l;
		return this;
	}

	/**
	 * Set the maximum reconnect delay.
	 */
	public ConnectionFactoryBuilder setMaxReconnectDelay(long to) {
		assert to > 0 : "Reconnect delay must be a positive number";
		maxReconnectDelay = to;
		return this;
	}

	/**
	 * Set the auth descriptor to enable authentication on new connections.
	 */
	public ConnectionFactoryBuilder setAuthDescriptor(AuthDescriptor to) {
		authDescriptor = to;
		return this;
	}

	/**
	 * Set the maximum timeout exception threshold
	 */
	public ConnectionFactoryBuilder setTimeoutExceptionThreshold(int to) {
		assert to > 1 : "Minimum timeout exception threshold is 2";
		if (to > 1) {
			timeoutExceptionThreshold = to -2;
		}
		return this;
	}

    public Config getVBucketConfig() {
        return vBucketConfig;
    }

    public void setVBucketConfig(Config vBucketConfig) {
        this.vBucketConfig = vBucketConfig;
    }

	/**
	 * Get the ConnectionFactory set up with the provided parameters.
	 */
	public ConnectionFactory build() {
		return new DefaultConnectionFactory() {

			@Override
			public BlockingQueue<Operation> createOperationQueue() {
				return opQueueFactory == null ?
						super.createOperationQueue() : opQueueFactory.create();
			}

			@Override
			public BlockingQueue<Operation> createReadOperationQueue() {
				return readQueueFactory == null ?
						super.createReadOperationQueue()
						: readQueueFactory.create();
			}

			@Override
			public BlockingQueue<Operation> createWriteOperationQueue() {
				return writeQueueFactory == null ?
						super.createReadOperationQueue()
						: writeQueueFactory.create();
			}

			@Override
			public NodeLocator createLocator(List<MemcachedNode> nodes) {
				switch(locator) {
					case ARRAY_MOD:
						return new ArrayModNodeLocator(nodes, getHashAlg());
					case CONSISTENT:
						return new KetamaNodeLocator(nodes, getHashAlg());
                    case VBUCKET:
                        return new VBucketNodeLocator(nodes, getVBucketConfig());
					default: throw new IllegalStateException(
							"Unhandled locator type: " + locator);
				}
			}

			@Override
			public Transcoder<Object> getDefaultTranscoder() {
				return transcoder == null ?
						super.getDefaultTranscoder() : transcoder;
			}

			@Override
			public FailureMode getFailureMode() {
				return failureMode == null ?
						super.getFailureMode() : failureMode;
			}

			@Override
			public HashAlgorithm getHashAlg() {
				return hashAlg == null ? super.getHashAlg() : hashAlg;
			}

			@Override
			public Collection<ConnectionObserver> getInitialObservers() {
				return initialObservers;
			}

			@Override
			public OperationFactory getOperationFactory() {
				return opFact == null ? super.getOperationFactory() : opFact;
			}

			@Override
			public long getOperationTimeout() {
				return opTimeout == -1 ?
						super.getOperationTimeout() : opTimeout;
			}

			@Override
			public int getReadBufSize() {
				return readBufSize == -1 ?
						super.getReadBufSize() : readBufSize;
			}

			@Override
			public boolean isDaemon() {
				return isDaemon;
			}

			@Override
			public boolean shouldOptimize() {
				return shouldOptimize;
			}

			@Override
			public boolean useNagleAlgorithm() {
				return useNagle;
			}

			@Override
			public long getMaxReconnectDelay() {
				return maxReconnectDelay;
			}

			@Override
			public AuthDescriptor getAuthDescriptor() {
				return authDescriptor;
			}

			@Override
			public long getOpQueueMaxBlockTime() {
				return opQueueMaxBlockTime > -1 ? opQueueMaxBlockTime
						: super.getOpQueueMaxBlockTime();
			}

			@Override
			public int getTimeoutExceptionThreshold() {
				return timeoutExceptionThreshold;
			}

		};

	}

	/**
	 * Type of protocol to use for connections.
	 */
	public static enum Protocol {
		/**
		 * Use the text (ascii) protocol.
		 */
		TEXT,
		/**
		 * Use the binary protocol.
		 */
		BINARY
	}

	/**
	 * Type of node locator to use.
	 */
	public static enum Locator {
		/**
		 * Array modulus - the classic node location algorithm.
		 */
		ARRAY_MOD,
		/**
		 * Consistent hash algorithm.
		 *
		 * This uses ketema's distribution algorithm, but may be used with any
		 * hash algorithm.
		 */
		CONSISTENT,
        /**
         * VBucket support
         */
        VBUCKET
	}
}