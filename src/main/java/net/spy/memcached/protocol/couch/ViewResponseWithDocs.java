package net.spy.memcached.protocol.couch;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ViewResponseWithDocs implements ViewResponse {

	Map<String, Object> map;
	final Collection<ViewRow> rows;
	final Collection<RowError> errors;

	public ViewResponseWithDocs(final Collection<ViewRow> r,
			final Collection<RowError> e) {
		map = null;
		rows = r;
		errors = e;
		for (ViewRow row : rows) {
			map.put(row.getId(), row.getDocument());
		}
	}

	public void addError(RowError r) {
		errors.add(r);
	}

	public Collection<RowError> getErrors() {
		return errors;
	}

	@Override
	public Iterator<ViewRow> iterator() {
		return rows.iterator();
	}

	@Override
	public int size() {
		return rows.size();
	}

	@Override
	public Map<String, Object> getMap() {
		if (map == null) {
			map = new HashMap<String, Object>();
			Iterator<ViewRow> itr = iterator();
			
			while(itr.hasNext()) {
				ViewRow cur = itr.next();
				map.put(cur.getId(), cur.getDocument());
			}
		}
		return Collections.unmodifiableMap(map);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (ViewRow r : rows) {
			s.append(r.getId());
			s.append(" : ");
			s.append(r.getKey());
			s.append(" : ");
			s.append(r.getValue());
			s.append(" : ");
			s.append(r.getDocument());
			s.append("\n");
		}
		return s.toString();
	}
}
