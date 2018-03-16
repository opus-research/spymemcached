package net.spy.memcached.protocol.couch;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface ViewResponse extends Iterable<ViewRow> {
	Collection<RowError> getErrors();

	Iterator<ViewRow> iterator();

	Map<String, Object> getMap();

	int size();
}
