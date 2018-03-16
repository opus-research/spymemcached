package net.spy.memcached.protocol.couch;

import java.util.Collection;

public interface ViewResponse extends Iterable<ViewRow> {
	Collection<RowError> getErrors();

	int size();
}
