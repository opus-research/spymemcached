package net.spy.memcached.protocol.couch;

public class RowError {
	private static final boolean error = true;
	private final String from;
	private final String reason;

	public RowError(String from, String reason) {
		this.from = from;
		this.reason = reason;
	}

	public boolean getError() {
		return error;
	}

	public String getFrom() {
		return from;
	}

	public String getReason() {
		return reason;
	}
}
