package net.spy.memcached.protocol.couchdb;

public class RowNoDocs extends RowReduced {
	private String id;

	public RowNoDocs(String id, String key, String value) {
		super(key, value);
		if (id != null && id.equals("null")) {
			this.id = null;
		} else {
			this.id = id;
		}
	}

	public String getId() {
		return id;
	}
}
