/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.binary;

import net.spy.memcached.OperationFactory;
import net.spy.memcached.OperationFactoryTestBase;

/**
 * An OperationFactoryTest.
 */
public class OperationFactoryTest extends OperationFactoryTestBase {

  @Override
  protected OperationFactory getOperationFactory() {
    return new BinaryOperationFactory();
  }

}
