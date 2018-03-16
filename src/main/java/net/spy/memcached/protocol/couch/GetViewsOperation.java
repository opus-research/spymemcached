package net.spy.memcached.protocol.couch;

import java.util.List;

import net.spy.memcached.ops.OperationCallback;

public interface GetViewsOperation {
	interface GetViewsCallback extends OperationCallback {
		void gotData(List<View> views);
	}
}
