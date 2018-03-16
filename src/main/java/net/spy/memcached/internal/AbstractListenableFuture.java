/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.spy.memcached.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import net.spy.memcached.compat.SpyObject;

/**
 *
 */
public abstract class AbstractListenableFuture<T> extends SpyObject implements ListenableFuture<T> {

  private final ExecutorService service;

  private List<GenericFutureListener<? extends Future<? super T>>> listeners;


  public AbstractListenableFuture(ExecutorService executor) {
    super();
    this.service = executor;
    listeners = new ArrayList<GenericFutureListener<? extends Future<? super T>>>();
  }

  protected ExecutorService executor() {
    return service;
  }

  @Override
  public Future<T> addListener(GenericFutureListener<? extends Future<? super T>> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("The listener can't be null.");
    }

    if(isDone()) {
      notifyListener(executor(), this, listener);
      return this;
    }

    synchronized(this) {
      if (!isDone()) {
        listeners.add(listener);
        return this;
      }
    }

    notifyListener(executor(), this, listener);
    return this;
  }

  protected void notifyListener(final ExecutorService executor,
    final Future<?> future, final GenericFutureListener listener) {
    executor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          listener.onComplete(future);
        } catch(Throwable t) {
          getLogger().warn(
            "Exception thrown wile executing " + listener.getClass().getName()
            + ".operationComplete()", t);
        }
      }
    });
  }

  protected void notifyListeners() {
    for(GenericFutureListener<? extends Future<? super T>> listener : listeners) {
      notifyListener(executor(), this, listener);
    }
  }

  @Override
  public Future<T> removeListener(GenericFutureListener<? extends Future<? super T>> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("The listener can't be null.");
    }

    if(isDone()) {
      return this;
    }

    synchronized(this) {
      if (!isDone()) {
        listeners.remove(listener);
      }
    }

    return this;
  }
}
