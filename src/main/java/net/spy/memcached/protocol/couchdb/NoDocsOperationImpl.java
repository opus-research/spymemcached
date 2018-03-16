package net.spy.memcached.protocol.couchdb;

import java.text.ParseException;

import net.spy.memcached.ops.OperationErrorType;
import net.spy.memcached.ops.OperationException;
import net.spy.memcached.ops.OperationStatus;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NoDocsOperationImpl extends HttpOperationImpl implements
		NoDocsOperation {

	public NoDocsOperationImpl(HttpRequest r, NoDocsCallback cb) {
		super(r, cb);
	}

	@Override
	public void handleResponse(HttpResponse response) {
		String json = getEntityString(response);
		System.out.println(json);
		try {
			OperationStatus status = parseViewForStatus(json);
			ViewResponseNoDocs vr = parseNoDocsViewResult(json);

			((NoDocsCallback) callback).gotData(vr);
			callback.receivedStatus(status);
		} catch (ParseException e) {
			exception = new OperationException(OperationErrorType.GENERAL,
					"Error parsing JSON");
		}
		callback.complete();
	}

	private ViewResponseNoDocs parseNoDocsViewResult(String json)
			throws ParseException {
		ViewResponseNoDocs vr = new ViewResponseNoDocs();
		if (json != null) {
			try {
				JSONObject base = new JSONObject(json);
				if (base.has("rows")) {
					JSONArray ids = base.getJSONArray("rows");
					for (int i = 0; i < ids.length(); i++) {
						String id = ids.getJSONObject(i).getString("id");
						String key = ids.getJSONObject(i).getString("key");
						String value = ids.getJSONObject(i).getString("value");
						vr.add(new RowNoDocs(id, key, value));
					}
				}
			} catch (JSONException e) {
				throw new ParseException("Cannot read json: " + json, 0);
			}
		}
		return vr;
	}
}
