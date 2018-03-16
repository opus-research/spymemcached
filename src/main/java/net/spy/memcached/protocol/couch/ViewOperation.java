package net.spy.memcached.protocol.couch;

import net.spy.memcached.ops.OperationCallback;

public interface ViewOperation {

	interface ViewCallback extends OperationCallback {
		void gotData(ViewResponse response);
	}
}
