package net.spy.memcached.protocol.couchdb;

public class RowReduced {
	private String key;
	private String value;

	public RowReduced(String key, String value) {
		if (key != null && key.equals("null")) {
			this.key = null;
		} else {
			this.key = key;
		}
		if (value != null && value.equals("null")) {
			this.value = null;
		} else {
			this.value = value;
		}
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
