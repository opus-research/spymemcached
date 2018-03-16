package net.spy.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.GetlOperation;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.transcoders.TranscodeService;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.vbucket.ConfigurationException;
import net.spy.memcached.vbucket.ConfigurationProvider;
import net.spy.memcached.vbucket.ConfigurationProviderHTTP;
import net.spy.memcached.vbucket.Reconfigurable;
import net.spy.memcached.vbucket.config.Bucket;
import net.spy.memcached.vbucket.config.Config;
import net.spy.memcached.vbucket.config.ConfigType;

public class MembaseClient extends MemcachedClient implements MembaseClientIF, Reconfigurable {
	private volatile boolean reconfiguring = false;
	private ConfigurationProvider configurationProvider;

	/**
	 * Get a MemcachedClient based on the REST response from a Membase server
	 * where the username is different than the bucket name.
	 *
	 * To connect to the "default" special bucket for a given cluster, use an
	 * empty string as the password.
	 *
	 * If a password has not been assigned to the bucket, it is typically an
	 * empty string.
	 *
	 * @param baseList the URI list of one or more servers from the cluster
	 * @param bucketName the bucket name in the cluster you wish to use
	 * @param usr the username for the bucket; this nearly always be the same
	 *        as the bucket name
	 * @param pwd the password for the bucket
	 * @throws IOException if connections could not be made
	 * @throws ConfigurationException if the configuration provided by the
	 *         server has issues or is not compatible
	 */
	public MembaseClient(final List<URI> baseList, final String bucketName,
		final String usr, final String pwd) throws IOException, ConfigurationException {
		this(new BinaryConnectionFactory(), baseList, bucketName, usr, pwd);
	}

	/**
	 * Get a MemcachedClient based on the REST response from a Membase server.
	 *
	 * This constructor is merely a convenience for situations where the bucket
	 * name is the same as the user name.  This is commonly the case.
	 *
	 * To connect to the "default" special bucket for a given cluster, use an
	 * empty string as the password.
	 *
	 * If a password has not been assigned to the bucket, it is typically an
	 * empty string.
	 *
	 * @param baseList the URI list of one or more servers from the cluster
	 * @param bucketName the bucket name in the cluster you wish to use
	 * @param pwd the password for the bucket
	 * @throws IOException if connections could not be made
	 * @throws ConfigurationException if the configuration provided by the
	 *         server has issues or is not compatible
	 */
	public MembaseClient(List<URI> baseList, String bucketName, String pwd)
			throws IOException, ConfigurationException {
		this(baseList, bucketName, bucketName, pwd);
	}

	/**
	 * Get a MemcachedClient based on the REST response from a Membase server
	 * where the username is different than the bucket name.
	 *
	 * Note that when specifying a ConnectionFactory you must specify a
	 * BinaryConnectionFactory. Also the ConnectionFactory's protocol
	 * and locator values are always overwritten. The protocol will always
	 * be binary and the locator will be chosen based on the bucket type you
	 * are connecting to.
	 *
	 * To connect to the "default" special bucket for a given cluster, use an
	 * empty string as the password.
	 *
	 * If a password has not been assigned to the bucket, it is typically an
	 * empty string.
	 *
	 * @param cf the ConnectionFactory to use to create connections
	 * @param baseList the URI list of one or more servers from the cluster
	 * @param bucketName the bucket name in the cluster you wish to use
	 * @param usr the username for the bucket; this nearly always be the same
	 *        as the bucket name
	 * @param pwd the password for the bucket
	 * @throws IOException if connections could not be made
	 * @throws ConfigurationException if the configuration provided by the
	 *         server has issues or is not compatible
	 */
	public MembaseClient(ConnectionFactory cf, final List<URI> baseList,
			final String bucketName, final String usr, final String pwd)
			throws IOException, ConfigurationException {
		ConnectionFactoryBuilder cfb = new ConnectionFactoryBuilder(cf);
		for (URI bu : baseList) {
			if (!bu.isAbsolute()) {
				throw new IllegalArgumentException("The base URI must be absolute");
			}
		}
		this.configurationProvider = new ConfigurationProviderHTTP(baseList, usr, pwd);
		Bucket bucket = this.configurationProvider.getBucketConfiguration(bucketName);
		Config config = bucket.getConfig();

		if (cf != null && !(cf instanceof BinaryConnectionFactory)) {
			throw new IllegalArgumentException("ConnectionFactory must be of type " +
					"BinaryConnectionFactory");
		}

		if (config.getConfigType() == ConfigType.MEMBASE) {
			cfb.setFailureMode(FailureMode.Retry)
				.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
				.setHashAlg(HashAlgorithm.KETAMA_HASH)
				.setLocatorType(ConnectionFactoryBuilder.Locator.VBUCKET)
				.setVBucketConfig(bucket.getConfig());
		} else if (config.getConfigType() == ConfigType.MEMCACHE) {
			cfb.setFailureMode(FailureMode.Retry)
				.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
				.setHashAlg(HashAlgorithm.KETAMA_HASH)
				.setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT);
		} else {
			throw new ConfigurationException("Bucket type not supported or JSON response unexpected");
		}

		if (!this.configurationProvider.getAnonymousAuthBucket().equals(bucketName) && usr != null) {
			AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"},
				new PlainCallbackHandler(usr, pwd));
			cfb.setAuthDescriptor(ad);
		}

		cf = cfb.build();
		
		List<InetSocketAddress> addrs = AddrUtil.getAddresses(bucket.getConfig().getServers());
		if(cf == null) {
			throw new NullPointerException("Connection factory required");
		}
		if(addrs == null) {
			throw new NullPointerException("Server list required");
		}
		if(addrs.isEmpty()) {
			throw new IllegalArgumentException(
			"You must have at least one server to connect to");
		}
		if(cf.getOperationTimeout() <= 0) {
			throw new IllegalArgumentException(
				"Operation timeout must be positive.");
		}
		tcService = new TranscodeService(cf.isDaemon());
		transcoder=cf.getDefaultTranscoder();
		opFact=cf.getOperationFactory();
		assert opFact != null : "Connection factory failed to make op factory";
		conn=cf.createConnection(addrs);
		assert conn != null : "Connection factory failed to make a connection";
		operationTimeout = cf.getOperationTimeout();
		authDescriptor = cf.getAuthDescriptor();
		if(authDescriptor != null) {
			addObserver(this);
		}
		setName("Memcached IO over " + conn);
		setDaemon(cf.isDaemon());
		
		start();
	}

	public void reconfigure(Bucket bucket) {
		reconfiguring = true;
		try {
			conn.reconfigure(bucket);
		} catch (IllegalArgumentException ex) {
			getLogger().warn("Failed to reconfigure client, staying with previous configuration.", ex);
		} finally {
			reconfiguring = false;
		}
	}

	/**
	 * Gets and locks the given key asynchronously. By default the maximum allowed
	 * timeout is 30 seconds. Timeouts greater than this will be set to 30 seconds.
	 *
	 * @param key the key to fetch and lock
	 * @param exp the amount of time the lock should be valid for in seconds.
	 * @param tc the transcoder to serialize and unserialize value
	 * @return a future that will hold the return value of the fetch
	 * @throws IllegalStateException in the rare circumstance where queue
	 *         is too full to accept any more requests
	 */
	public <T> OperationFuture<CASValue<T>> asyncGetAndLock(final String key, int exp,
			final Transcoder<T> tc) {
		final CountDownLatch latch=new CountDownLatch(1);
		final OperationFuture<CASValue<T>> rv=
			new OperationFuture<CASValue<T>>(key, latch, operationTimeout);

		Operation op=opFact.getl(key, exp,
				new GetlOperation.Callback() {
			private CASValue<T> val=null;
			public void receivedStatus(OperationStatus status) {
				rv.set(val, status);
			}
			public void gotData(String k, int flags, long cas, byte[] data) {
				assert key.equals(k) : "Wrong key returned";
				assert cas > 0 : "CAS was less than zero:  " + cas;
				val=new CASValue<T>(cas, tc.decode(
					new CachedData(flags, data, tc.getMaxSize())));
			}
			public void complete() {
				latch.countDown();
			}});
		rv.setOperation(op);
		addOp(key, op);
		return rv;
	}

	/**
	 * Get and lock the given key asynchronously and decode with the default
	 * transcoder. By default the maximum allowed timeout is 30 seconds.
	 * Timeouts greater than this will be set to 30 seconds.
	 *
	 * @param key the key to fetch and lock
	 * @param exp the amount of time the lock should be valid for in seconds.
	 * @return a future that will hold the return value of the fetch
	 * @throws IllegalStateException in the rare circumstance where queue
	 *         is too full to accept any more requests
	 */
	public OperationFuture<CASValue<Object>> asyncGetAndLock(final String key, int exp) {
		return asyncGetAndLock(key, exp, transcoder);
	}

	/**
	 * Getl with a single key. By default the maximum allowed timeout is 30
	 * seconds. Timeouts greater than this will be set to 30 seconds.
	 *
	 * @param key the key to get and lock
	 * @param exp the amount of time the lock should be valid for in seconds.
	 * @param tc the transcoder to serialize and unserialize value
	 * @return the result from the cache (null if there is none)
	 * @throws OperationTimeoutException if the global operation timeout is
	 *		   exceeded
	 * @throws IllegalStateException in the rare circumstance where queue
	 *         is too full to accept any more requests
	 */
	public <T> CASValue<T> getAndLock(String key, int exp, Transcoder<T> tc) {
		try {
			return asyncGetAndLock(key, exp, tc).get(
					operationTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted waiting for value", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("Exception waiting for value", e);
		} catch (TimeoutException e) {
			throw new OperationTimeoutException("Timeout waiting for value", e);
		}
	}

	/**
	 * Get and lock with a single key and decode using the default transcoder.
	 * By default the maximum allowed timeout is 30 seconds. Timeouts greater
	 * than this will be set to 30 seconds.
	 * @param key the key to get and lock
	 * @param exp the amount of time the lock should be valid for in seconds.
	 * @return the result from the cache (null if there is none)
	 * @throws OperationTimeoutException if the global operation timeout is
	 *		   exceeded
	 * @throws IllegalStateException in the rare circumstance where queue
	 *         is too full to accept any more requests
	 */
	public CASValue<Object> getAndLock(String key, int exp) {
		return getAndLock(key, exp, transcoder);
	}
	
	/**
	 * Infinitely loop processing IO.
	 */
	@Override
	public void run() {
		while(running) {
            if (!reconfiguring) {
                try {
                    conn.handleIO();
                } catch (IOException e) {
                    logRunException(e);
                } catch (CancelledKeyException e) {
                    logRunException(e);
                } catch (ClosedSelectorException e) {
                    logRunException(e);
                } catch (IllegalStateException e) {
                    logRunException(e);
                }
			}
		}
		getLogger().info("Shut down memcached client");
	}
	
	@Override
	public boolean shutdown(long timeout, TimeUnit unit) {
		boolean shutdownResult = super.shutdown(timeout, unit);
		if (configurationProvider != null) {
            configurationProvider.shutdown();
        }
		return shutdownResult;
	}
}
