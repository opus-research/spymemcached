package net.spy.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.GetlOperation;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.transcoders.Transcoder;

public class MikeClient extends MemcachedClient {

  public MikeClient(ConnectionFactory cf, List<InetSocketAddress> addrs)
      throws IOException {
    super(cf, addrs);
  }

  public MikeClient(List<InetSocketAddress> addrs)
  throws IOException {
super(addrs);
}

  /**
   * Gets and locks the given key asynchronously. By default the maximum allowed
   * timeout is 30 seconds. Timeouts greater than this will be set to 30
   * seconds.
   *
   * @param key the key to fetch and lock
   * @param exp the amount of time the lock should be valid for in seconds.
   * @param tc the transcoder to serialize and unserialize value
   * @return a future that will hold the return value of the fetch
   * @throws IllegalStateException in the rare circumstance where queue is too
   *           full to accept any more requests
   */
  public <T> OperationFuture<CASValue<T>> asyncGetAndLock(final String key,
      int exp, final Transcoder<T> tc) {
    final CountDownLatch latch = new CountDownLatch(1);
    final OperationFuture<CASValue<T>> rv =
        new OperationFuture<CASValue<T>>(key, latch, operationTimeout);

    Operation op = opFact.getl(key, exp, new GetlOperation.Callback() {
      private CASValue<T> val = null;

      public void receivedStatus(OperationStatus status) {
        if (!status.isSuccess()) {
          val = new CASValue<T>(-1, null);
        }
        rv.set(val, status);
      }

      public void gotData(String k, int flags, long cas, byte[] data) {
        assert key.equals(k) : "Wrong key returned";
        assert cas > 0 : "CAS was less than zero:  " + cas;
        val =
            new CASValue<T>(cas, tc.decode(new CachedData(flags, data, tc
                .getMaxSize())));
      }

      public void complete() {
        latch.countDown();
      }
    });
    rv.setOperation(op);
    mconn.enqueueOperation(key, op);
    return rv;
  }

  /**
   * Get and lock the given key asynchronously and decode with the default
   * transcoder. By default the maximum allowed timeout is 30 seconds. Timeouts
   * greater than this will be set to 30 seconds.
   *
   * @param key the key to fetch and lock
   * @param exp the amount of time the lock should be valid for in seconds.
   * @return a future that will hold the return value of the fetch
   * @throws IllegalStateException in the rare circumstance where queue is too
   *           full to accept any more requests
   */
  public OperationFuture<CASValue<Object>> asyncGetAndLock(final String key,
      int exp) {
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
   *           exceeded
   * @throws IllegalStateException in the rare circumstance where queue is too
   *           full to accept any more requests
   */
  public <T> CASValue<T> getAndLock(String key, int exp, Transcoder<T> tc) {
    try {
      return asyncGetAndLock(key, exp, tc).get(operationTimeout,
          TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted waiting for value", e);
    } catch (ExecutionException e) {
      throw new RuntimeException("Exception waiting for value", e);
    } catch (TimeoutException e) {
      throw new OperationTimeoutException("Timeout waiting for value", e);
    }
  }

  /**
   * Get and lock with a single key and decode using the default transcoder. By
   * default the maximum allowed timeout is 30 seconds. Timeouts greater than
   * this will be set to 30 seconds.
   *
   * @param key the key to get and lock
   * @param exp the amount of time the lock should be valid for in seconds.
   * @return the result from the cache (null if there is none)
   * @throws OperationTimeoutException if the global operation timeout is
   *           exceeded
   * @throws IllegalStateException in the rare circumstance where queue is too
   *           full to accept any more requests
   */
  public CASValue<Object> getAndLock(String key, int exp) {
    return getAndLock(key, exp, transcoder);
  }

  /**
   * Unlock the given key from the cache.
   *
   * @param key the key to unlock
   * @param cas the CAS identifier
   * @return whether or not the operation was performed
   * @throws IllegalStateException in the rare circumstance where queue is too
   *           full to accept any more requests
   */
  public OperationFuture<Boolean> unlock(final String key, long casId) {
    final CountDownLatch latch = new CountDownLatch(1);
    final OperationFuture<Boolean> rv = new OperationFuture<Boolean>(key,
            latch, operationTimeout);
    Operation op = opFact.unlock(key, casId,
            new OperationCallback() {

      public void receivedStatus(OperationStatus s) {
        rv.set(s.isSuccess(), s);
      }

      public void complete() {
        latch.countDown();
      }
    });
    rv.setOperation(op);
    mconn.enqueueOperation(key, op);
    return rv;
  }
}
