/**
 * Copyright (C) 2006-2009 Dustin Sallings
 * Copyright (C) 2009-2013 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package net.spy.memcached.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import net.spy.memcached.compat.SpyObject;

/**
 *
 */
public abstract class AbstractListenableFuture<T, L extends GenericFutureListener> extends SpyObject implements ListenableFuture<T, L> {

  private final ExecutorService service;

  private List<GenericFutureListener<? extends Future<T>>> listeners;

  public AbstractListenableFuture(ExecutorService executor) {
    super();
    this.service = executor;
    listeners = new ArrayList<GenericFutureListener<? extends Future<T>>>();
  }

  protected ExecutorService executor() {
    return service;
  }

  protected Future<T> addToListeners(GenericFutureListener<? extends Future<T>> listener) {
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

  protected Future<T> removeFromListeners(GenericFutureListener<? extends Future<T>> listener) {
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
