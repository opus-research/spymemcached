package net.spy.memcached.protocol.couch;

public class RowNoDocs implements ViewRow {
	private final String id;
	private final String key;
	private final String value;

	public RowNoDocs(String id, String key, String value) {
		// The id can be the string "null" so convert it to null
		if (id != null && id.equals("null")) {
			this.id = null;
		} else {
			this.id = id;
		}
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
		return id;
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
  public Object getDocument() {
    throw new UnsupportedOperationException("This view result doesn't contain documents");
  }
}
