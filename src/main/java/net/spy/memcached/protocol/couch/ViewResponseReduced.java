package net.spy.memcached.protocol.couch;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class ViewResponseReduced implements ViewResponse {

	final Collection<ViewRow> rows;
	final Collection<RowError> errors;

	public ViewResponseReduced(final Collection<ViewRow> r,
			final Collection<RowError> e) {
		rows = r;
		errors = e;
	}

	public Collection<RowError> getErrors() {
		return errors;
	}

	public int size() {
		return rows.size();
	}

	@Override
	public Iterator<ViewRow> iterator() {
		return rows.iterator();
	}

	public Map<String, Object> getMap() {
		throw new UnsupportedOperationException("This view doesn't contain documents");
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (ViewRow r : rows) {
			s.append(r.getKey());
			s.append(" : ");
			s.append(r.getValue());
			s.append("\n");
		}
		return s.toString();
	}
}
