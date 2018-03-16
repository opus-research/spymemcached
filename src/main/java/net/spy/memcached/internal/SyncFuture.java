package net.spy.memcached.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.MemcachedConnection;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;

public class SyncFuture<T> implements BulkFuture<Collection<T>> {
	private final Collection<T> syncs;
	private final Collection<Operation> ops;
	private final CountDownLatch latch;
	private OperationStatus status;
	private boolean cancelled=false;
	private boolean timedout=false;

	public SyncFuture(Collection<T> syncs, Collection<Operation> ops,
			CountDownLatch l) {
		super();
		this.syncs = syncs;
		this.ops = ops;
		latch=l;
		status = null;
	}

	@Override
	public boolean cancel(boolean cancel) {
		boolean rv=false;
		for(Operation op : ops) {
			rv |= op.getState() == OperationState.WRITING;
			op.cancel();
		}
		cancelled=true;
		return rv;
	}

	@Override
	public Collection<T> get() throws InterruptedException, ExecutionException {
		try {
			return get(2500, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw new RuntimeException("Timed out waiting forever", e);
		}
	}

	@Override
	public Collection<T> get(long to, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		Collection<Operation> timedoutOps = new HashSet<Operation>();
		if (!latch.await(to, unit)) {
            for (Operation op : ops) {
                if (op.getState() != OperationState.COMPLETE) {
                    MemcachedConnection.opTimedOut(op);
                    timedoutOps.add(op);
                } else {
                    MemcachedConnection.opSucceeded(op);
                }
            }
        }
        for (Operation op : ops) {
            if (op.isCancelled()) {
                throw new ExecutionException(new RuntimeException("Cancelled"));
            }
        }
		return syncs;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}

	@Override
	public boolean isTimeout() {
		return timedout;
	}

	@Override
	public Collection<T> getSome(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setStatus(OperationStatus s) {
		status = s;
	}

	@Override
	public OperationStatus getStatus() {
		if (status == null) {
			try {
				get();
			} catch (InterruptedException e) {
				status = new OperationStatus(false, "Interrupted");
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				return status;
			}
		}
		return status;
	}
}
