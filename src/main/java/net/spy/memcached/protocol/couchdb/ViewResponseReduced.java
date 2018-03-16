package net.spy.memcached.protocol.couchdb;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class ViewResponseReduced implements ViewResponse<RowReduced> {

	Collection<RowReduced> rows;
	
	public ViewResponseReduced() {
		rows = new LinkedList<RowReduced>();
	}

	public void add(RowReduced r) {
		rows.add(r);
	}

	public int size() {
		return rows.size();
	}

	@Override
	public Iterator<RowReduced> iterator() {
		return rows.iterator();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (RowReduced r : rows) {
			s.append(r.getKey() + " : " + r.getValue() + "\n");
		}
		return s.toString();
	}
}
