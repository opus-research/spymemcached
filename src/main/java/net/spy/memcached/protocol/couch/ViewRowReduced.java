package net.spy.memcached.protocol.couch;

public class ViewRowReduced implements ViewRow {
	private final String key;
	private final String value;

	public ViewRowReduced(String key, String value) {
		// The key can be the string "null" so convert it to null
		if (key != null && key.equals("null")) {
			this.key = null;
		} else {
			this.key = key;
		}
		// The value can be the string "null" so convert it to null
		if (value != null && value.equals("null")) {
			this.value = null;
		} else {
			this.value = value;
		}
	}

  @Override
  public String getId() {
    throw new UnsupportedOperationException("Reduced views don't contain document id's");
  }

  @Override
	public String getKey() {
		return key;
	}

  @Override
	public String getValue() {
		return value;
	}

  @Override
  public String getDocument() {
    throw new UnsupportedOperationException("Reduced views don't contain documents");
  }
}
