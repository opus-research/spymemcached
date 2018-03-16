package net.spy.memcached.internal;

import net.spy.memcached.ops.OperationStatus;

public class SyncResponse {
	private final String key;
	private final long cas;
	private final OperationStatus status;

	public SyncResponse(String key, long cas, OperationStatus status) {
		this.key = key;
		this.cas =cas;
		this.status = status;
	}

	public String getKey() {
		return key;
	}

	public long getCas() {
		return cas;
	}

	public OperationStatus getStatus() {
		return status;
	}

	public String toString() {
		return "Key: " + key + ", Cas: " + cas + ", Status: " + status.getMessage();
	}
}
