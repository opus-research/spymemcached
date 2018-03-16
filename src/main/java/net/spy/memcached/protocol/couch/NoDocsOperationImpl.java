package net.spy.memcached.protocol.couch;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.http.HttpRequest;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NoDocsOperationImpl extends ViewOperationImpl {

	public NoDocsOperationImpl(HttpRequest r, ViewCallback cb) {
		super(r, cb);
	}

	@Override
	protected ViewResponse parseResult(String json)
			throws ParseException {
		final Collection<ViewRow> rows = new LinkedList<ViewRow>();
		final Collection<RowError> errors = new LinkedList<RowError>();
		if (json != null) {
			try {
				JSONObject base = new JSONObject(json);
				if (base.has("rows")) {
					JSONArray ids = base.getJSONArray("rows");
					for (int i = 0; i < ids.length(); i++) {
						JSONObject elem = ids.getJSONObject(i);
						String id = elem.getString("id");
						String key = elem.getString("key");
						String value = elem.getString("value");
						rows.add(new ViewRowNoDocs(id, key, value));
					}
				}
				if (base.has("errors")) {
					JSONArray ids = base.getJSONArray("errors");
					for (int i = 0; i < ids.length(); i++) {
						JSONObject elem = ids.getJSONObject(i);
						String from = elem.getString("from");
						String reason = elem.getString("reason");
						errors.add(new RowError(from, reason));
					}
				}
			} catch (JSONException e) {
				throw new ParseException("Cannot read json: " + json, 0);
			}
		}
		return new ViewResponseNoDocs(rows, errors);
	}
}
