package net.spy.memcached.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.compat.log.LoggerFactory;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationState;

/**
 * Future for handling results from bulk gets.
 *
 * Not intended for general use.
 *
 * @param <T> types of objects returned from the GET
 */
public class BulkGetFuture<T> implements BulkFuture<Map<String, T>> {
	private final Map<String, Future<T>> rvMap;
	private final Collection<Operation> ops;
	private final CountDownLatch latch;
	private boolean cancelled=false;
	private boolean timeout = false;

	public BulkGetFuture(Map<String, Future<T>> m,
			Collection<Operation> getOps, CountDownLatch l) {
		super();
		rvMap = m;
		ops = getOps;
		latch=l;
	}

	public boolean cancel(boolean ign) {
		boolean rv=false;
		for(Operation op : ops) {
			rv |= op.getState() == OperationState.WRITING;
			op.cancel();
		}
		for (Future<T> v : rvMap.values()) {
			v.cancel(ign);
		}
		cancelled=true;
		return rv;
	}

	public Map<String, T> get()
		throws InterruptedException, ExecutionException {
		try {
			return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw new RuntimeException("Timed out waiting forever", e);
		}
	}
	
	public Map<String, T> getSome(long to, TimeUnit unit)
			throws InterruptedException, ExecutionException {
		Collection<Operation> timedoutOps = new HashSet<Operation>();
		Map<String, T> ret = internalGet(to, unit, timedoutOps);
		if (timedoutOps.size() > 0) {
			timeout = true;
			LoggerFactory.getLogger(getClass()).warn(
					new CheckedOperationTimeoutException(
							"Operation timed out: ", timedoutOps).getMessage());
		}
		return ret;

	}

	public Map<String, T> get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		Collection<Operation> timedoutOps = new HashSet<Operation>();
		Map<String, T> ret = internalGet(timeout, unit, timedoutOps);
		if (timedoutOps.size() > 0) {
			throw new CheckedOperationTimeoutException("Operation timed out.",
					timedoutOps);
		}
		return ret;
	}

	private Map<String, T> internalGet(long timeout, TimeUnit unit,
			Collection<Operation> timedoutOps) throws InterruptedException,
			ExecutionException {
		if (!latch.await(timeout, unit)) {
			for (Operation op : ops) {
				if (op.getState() != OperationState.COMPLETE) {
					timedoutOps.add(op);
				}
			}
		}
		for (Operation op : ops) {
			if (op.isCancelled()) {
				throw new ExecutionException(new RuntimeException("Cancelled"));
			}
			if (op.hasErrored()) {
				throw new ExecutionException(op.getException());
			}
		}
		Map<String, T> m = new HashMap<String, T>();
		for (Map.Entry<String, Future<T>> me : rvMap.entrySet()) {
			m.put(me.getKey(), me.getValue().get());
		}
		return m;
	}


	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isDone() {
		return latch.getCount() == 0;
	}
	
	public boolean isTimeout() {
		return timeout;
	}
}