/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.ops;

import net.spy.memcached.tapmessage.TapOpcode;
import net.spy.memcached.tapmessage.ResponseMessage;

/**
 * Tap operation.
 */
public interface TapOperation extends Operation {

  /**
   * Operation callback for the tap dump request.
   */
  interface Callback extends OperationCallback {
    /**
     * Callback for each result from a get.
     * 
     * @param message the response message sent from the server
     */
    void gotData(ResponseMessage message);

    void gotAck(TapOpcode opcode, int opaque);
  }

  void streamClosed(OperationState state);
}
