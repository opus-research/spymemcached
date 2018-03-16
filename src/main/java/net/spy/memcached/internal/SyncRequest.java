package net.spy.memcached.internal;

public class SyncRequest {
	private final String key;
	private final Long cas;

	public SyncRequest(String key) {
		this.key = key;
		this.cas = 0L;
	}

	public SyncRequest(String key, Long cas) {
		this.key = key;
		this.cas =cas;
	}

	public String getKey() {
		return key;
	}

	public Long getCas() {
		return cas;
	}

	@Override
	public String toString() {
		return "Key: " + key + ", Cas: " + cas;
	}
}
