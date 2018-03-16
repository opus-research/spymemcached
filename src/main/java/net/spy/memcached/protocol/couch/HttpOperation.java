/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.protocol.couch;

import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

/**
 * An HttpOperation.
 */
public interface HttpOperation {

  HttpRequest getRequest();

  OperationCallback getCallback();

  boolean isCancelled();

  boolean hasErrored();

  boolean isTimedOut();

  void cancel();

  void timeOut();

  OperationException getException();

  void handleResponse(HttpResponse response);
}
