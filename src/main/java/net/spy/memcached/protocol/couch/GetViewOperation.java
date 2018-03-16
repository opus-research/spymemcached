package net.spy.memcached.protocol.couch;

import net.spy.memcached.ops.OperationCallback;

public interface GetViewOperation {
	public interface GetViewCallback extends OperationCallback {
		void gotData(View view);
	}
}
