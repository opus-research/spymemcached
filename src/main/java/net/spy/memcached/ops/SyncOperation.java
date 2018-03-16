package net.spy.memcached.ops;

import net.spy.memcached.internal.SyncResponse;


/**
 * Sync operation.
 */
public interface SyncOperation extends KeyedOperation {

	/**
	 * Operation callback for the sync request.
	 */
	interface Callback extends OperationCallback {
		/**
		 * Callback for each result from a get.
		 *
		 * @param key the key that was retrieved
		 * @param flags the flags for this value
		 * @param data the data stored under this key
		 */
		void gotData(SyncResponse s);
	}

}
