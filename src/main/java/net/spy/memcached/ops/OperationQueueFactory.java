/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

import java.util.concurrent.BlockingQueue;

/**
 * Factory used for creating operation queues.
 */
public interface OperationQueueFactory {

  /**
   * Create an instance of a queue.
   */
  BlockingQueue<Operation> create();
}
