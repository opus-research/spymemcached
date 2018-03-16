/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.ascii;

import net.spy.memcached.ops.OperationCallback;

/**
 * For testing, allow subclassing of the operation impl.
 */
public abstract class ExtensibleOperationImpl extends OperationImpl {

  public ExtensibleOperationImpl() {
    super();
  }

  public ExtensibleOperationImpl(OperationCallback cb) {
    super(cb);
  }

}
