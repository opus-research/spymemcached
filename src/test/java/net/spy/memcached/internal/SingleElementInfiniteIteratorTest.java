/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.internal;

import junit.framework.TestCase;

/**
 * A SingleElementFinitIteratorTest.
 */
public class SingleElementInfiniteIteratorTest extends TestCase {
  private static final String CONSTANT = "foo";
  private SingleElementInfiniteIterator<String> iterator;

  @Override
  protected void setUp() {
    iterator = new SingleElementInfiniteIterator<String>(CONSTANT);
  }

  public void testHasNextAndNext() {
    for (int i = 0; i < 100; ++i) {
      assertTrue(iterator.hasNext());
      assertSame(CONSTANT, iterator.next());
    }
  }

  public void testRemove() {
    try {
      iterator.remove();
      fail("Expected UnsupportedOperationException on a remove.");
    } catch (UnsupportedOperationException e) {
      return;
    }
  }
}
