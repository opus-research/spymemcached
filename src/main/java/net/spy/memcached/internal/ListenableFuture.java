

package net.spy.memcached.internal;

import java.util.concurrent.Future;

/**
 *
 */
public interface ListenableFuture<T> extends Future<T> {

  Future<T> addListener(GenericFutureListener<? extends Future<? super T>> listener);

  Future<T> removeListener(GenericFutureListener<? extends Future<? super T>> listener);

}
