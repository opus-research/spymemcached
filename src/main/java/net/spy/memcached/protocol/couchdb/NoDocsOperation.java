package net.spy.memcached.protocol.couchdb;

import net.spy.memcached.ops.OperationCallback;

public interface NoDocsOperation {

	interface NoDocsCallback extends OperationCallback {
		void gotData(ViewResponseNoDocs response);
	}
}
