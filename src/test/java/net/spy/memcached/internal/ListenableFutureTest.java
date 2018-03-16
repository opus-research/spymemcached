/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.spy.memcached.internal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ListenableFutureTest {

  private ExecutorService service = Executors.newCachedThreadPool();

  @Test
  public void verifyOnComplete() throws Exception {
    DummyListenableFuture<String> future =
      new DummyListenableFuture<String>(false, service);

    final CountDownLatch latch = new CountDownLatch(1);
    future.addListener(new GenericFutureListener() {
      @Override
      public void onComplete(Future future) throws Exception {
        assertEquals("Hello World", (String) future.get());
        latch.countDown();
      }
    });

    future.set("Hello World");
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void verifyOnCompleteWhenAlreadyDone() throws Exception {
    DummyListenableFuture<String> future =
      new DummyListenableFuture<String>(true, service);

    final CountDownLatch latch = new CountDownLatch(1);
    future.addListener(new GenericFutureListener() {
      @Override
      public void onComplete(Future future) throws Exception {
        latch.countDown();
      }
    });

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void verifyOnCompleteWhenCancelled() throws Exception {
    DummyListenableFuture<String> future =
      new DummyListenableFuture<String>(false, service);

    final CountDownLatch latch = new CountDownLatch(1);
    future.addListener(new GenericFutureListener() {
      @Override
      public void onComplete(Future future) throws Exception {
        assertTrue(future.isCancelled());
        latch.countDown();
      }
    });

    future.cancel(true);

    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

  @Test
  public void verifyRemoval() throws Exception {
    DummyListenableFuture<String> future =
      new DummyListenableFuture<String>(false, service);

    final CountDownLatch latch = new CountDownLatch(1);
    final GenericFutureListener listener = new GenericFutureListener() {
      @Override
      public void onComplete(Future future) throws Exception {
        latch.countDown();
      }
    };

    future.addListener(listener);
    future.removeListener(listener);

    Thread.sleep(500);
    assertEquals(1, latch.getCount());
  }

  @Test
  public void verifyMultipleListeners() throws Exception {
    DummyListenableFuture<String> future =
      new DummyListenableFuture<String>(false, service);

    final CountDownLatch latch = new CountDownLatch(2);
    final GenericFutureListener listener1 = new GenericFutureListener() {
      @Override
      public void onComplete(Future future) throws Exception {
        latch.countDown();
      }
    };

    final GenericFutureListener listener2 = new GenericFutureListener() {
      @Override
      public void onComplete(Future future) throws Exception {
        latch.countDown();
      }
    };

    future.addListener(listener1);
    future.addListener(listener2);

    future.set("Hello World");
    assertTrue(latch.await(1, TimeUnit.SECONDS));
  }

}
