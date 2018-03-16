package net.spy.memcached.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Provides some additional flexibility for async getBulk operations: getSome
 * allows getting partial data when timeout is reached.
 * 
 * @author boris.partensky@gmail.com
 * @param <V>
 * 
 */
public interface BulkFuture<V> extends Future<V> {
	
	public boolean isTimeout();

    /**
     * this method does not throw timeout exception. Instead, if timeout is
     * reached, it returns what's there and sets isTimeout flag. This allows to
     * get partial data when timeout is reached.
     * 
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
	public V getSome(long to, TimeUnit unit)
			throws InterruptedException, ExecutionException;

}
