/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.spy.memcached.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class DummyListenableFuture<T> extends AbstractListenableFuture<T, GenericFutureListener> {

  private boolean done;
  private boolean cancelled = false;

  private T content = null;

  public DummyListenableFuture(boolean alreadyDone, ExecutorService service) {
    super(service);
    this.done = alreadyDone;
  }

  @Override
  public boolean cancel(boolean bln) {
    cancelled = true;
    notifyListeners();
    return true;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    try {
      return get(1, TimeUnit.SECONDS);
    } catch (TimeoutException ex) {
      return null;
    }
  }

  @Override
  public T get(long l, TimeUnit tu) throws InterruptedException, ExecutionException, TimeoutException {
    return content;
  }

  public void set(T content) {
    notifyListeners();
    this.content = content;
  }

  @Override
  public DummyListenableFuture<T> addListener(GenericFutureListener listener) {
    super.addToListeners((GenericFutureListener) listener);
    return this;
  }

  @Override
  public DummyListenableFuture<T> removeListener(GenericFutureListener listener) {
    super.removeFromListeners((GenericFutureListener) listener);
    return this;
  }

}
