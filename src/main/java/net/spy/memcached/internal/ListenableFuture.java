

package net.spy.memcached.internal;

import java.util.concurrent.Future;

/**
 *
 */
public interface ListenableFuture<T, L extends GenericFutureListener> extends Future<T> {

  Future<T> addListener(L listener);

  Future<T> removeListener(L listener);

}
