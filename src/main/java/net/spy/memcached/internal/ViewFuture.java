package net.spy.memcached.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.protocol.couch.ViewResponse;

public class ViewFuture extends HttpFuture<ViewResponse> {

  protected boolean exceptionOnError;

  public ViewFuture(CountDownLatch latch, long timeout,
      boolean exceptionOnError) {
    super(latch, timeout);
    this.exceptionOnError = exceptionOnError;
  }

  @Override
  public ViewResponse get(long duration, TimeUnit units) throws InterruptedException,
      ExecutionException, TimeoutException {
    if (!latch.await(duration, units)) {
      if (op != null) {
        op.timeOut();
      }
      status = new OperationStatus(false, "Timed out");
      throw new TimeoutException("Timed out waiting for operation");
    }

    if (op != null && op.hasErrored()) {
      status = new OperationStatus(false, op.getException().getMessage());
      throw new ExecutionException(op.getException());
    }

    if (op.isCancelled()) {
      status = new OperationStatus(false, "Operation Cancelled");
      throw new ExecutionException(new RuntimeException("Cancelled"));
    }

    if (op != null && op.isTimedOut()) {
      status = new OperationStatus(false, "Timed out");
      throw new ExecutionException(new OperationTimeoutException(
          "Operation timed out."));
    }

    ViewResponse vr = objRef.get();
    if (exceptionOnError && vr.getErrors().size() > 0) {
      throw new ExecutionException(new RuntimeException("View has errors"));
    }
    return vr;
  }
}
