
package net.spy.memcached.internal;

import java.util.EventListener;
import java.util.concurrent.Future;

/**
 *
 */
public interface GenericFutureListener<F extends Future<?>> extends EventListener {

  void onComplete(F future) throws Exception;

}
