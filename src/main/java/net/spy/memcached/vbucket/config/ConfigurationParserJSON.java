/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket.config;

import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import java.text.ParseException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import net.spy.memcached.compat.SpyObject;

/**
 * A ConfigParserJSON.
 */
public class ConfigurationParserJSON extends SpyObject implements
    ConfigurationParser {
  private static final String NAME_ATTR = "name";
  private static final String URI_ATTR = "uri";
  private static final String STREAMING_URI_ATTR = "streamingUri";

  public Map<String, Pool> parseBase(String base) throws ParseException {
    Map<String, Pool> parsedBase = new HashMap<String, Pool>();
    JSONArray poolsJA = null;
    try {
      JSONObject baseJO = new JSONObject(base);
      poolsJA = baseJO.getJSONArray("pools");
    } catch (JSONException e) {
      throw new ParseException("Can not read base " + base, 0);
    }
    for (int i = 0; i < poolsJA.length(); ++i) {
      try {
        JSONObject poolJO = poolsJA.getJSONObject(i);
        String name = (String) poolJO.get(NAME_ATTR);
        if (name == null || "".equals(name)) {
          throw new ParseException("Pool's name is missing.", 0);
        }
        String uri = (String) poolJO.get(URI_ATTR);
        if (uri == null || "".equals(uri)) {
          throw new ParseException("Pool's uri is missing.", 0);
        }
        String streamingUri = (String) poolJO.get(STREAMING_URI_ATTR);
        Pool pool = new Pool(name, new URI(uri), new URI(streamingUri));
        parsedBase.put(name, pool);
      } catch (JSONException e) {
        getLogger()
            .error("One of the pool configuration can not be parsed.", e);
      } catch (URISyntaxException e) {
        getLogger().error("Server provided an incorrect uri.", e);
      }
    }
    return parsedBase;

  }

  public void loadPool(Pool pool, String sPool) throws ParseException {
    try {
      JSONObject poolJO = new JSONObject(sPool);
      JSONObject poolBucketsJO = poolJO.getJSONObject("buckets");
      URI bucketsUri = new URI((String) poolBucketsJO.get("uri"));
      pool.setBucketsUri(bucketsUri);
    } catch (JSONException e) {
      throw new ParseException(e.getMessage(), 0);
    } catch (URISyntaxException e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }

  public Map<String, Bucket> parseBuckets(String buckets)
    throws ParseException {
    Map<String, Bucket> bucketsMap = new HashMap<String, Bucket>();
    try {
      JSONArray bucketsJA = new JSONArray(buckets);
      for (int i = 0; i < bucketsJA.length(); ++i) {
        JSONObject bucketJO = bucketsJA.getJSONObject(i);
        Bucket bucket = parseBucketFromJSON(bucketJO);
        bucketsMap.put(bucket.getName(), bucket);
      }
    } catch (JSONException e) {
      throw new ParseException(e.getMessage(), 0);
    }
    return bucketsMap;
  }

  public Bucket parseBucket(String sBucket) throws ParseException {
    try {
      return parseBucketFromJSON(new JSONObject(sBucket));
    } catch (JSONException e) {
      throw new ParseException(e.getMessage(), 0);
    }
  }

  private Bucket parseBucketFromJSON(JSONObject bucketJO)
    throws ParseException {
    try {
      String bucketname = bucketJO.get("name").toString();
      String streamingUri = bucketJO.get("streamingUri").toString();
      ConfigFactory cf = new DefaultConfigFactory();
      Config config = cf.create(bucketJO);
      List<Node> nodes = new ArrayList<Node>();
      JSONArray nodesJA = bucketJO.getJSONArray("nodes");
      for (int i = 0; i < nodesJA.length(); ++i) {
        JSONObject nodeJO = nodesJA.getJSONObject(i);
        String statusValue = nodeJO.get("status").toString();
        Status status = null;
        try {
          status = Status.valueOf(statusValue);
        } catch (IllegalArgumentException e) {
          getLogger().error("Unknown status value: " + statusValue);
        }
        String hostname = nodeJO.get("hostname").toString();
        JSONObject portsJO = nodeJO.getJSONObject("ports");
        Map<Port, String> ports = new HashMap<Port, String>();
        for (Port port : Port.values()) {
          String portValue = portsJO.get(port.toString()).toString();
          if (portValue == null || portValue.isEmpty()) {
            continue;
          }
          ports.put(port, portValue);
        }
        Node node = new Node(status, hostname, ports);
        nodes.add(node);
      }
      return new Bucket(bucketname, config, new URI(streamingUri), nodes);
    } catch (JSONException e) {
      throw new ParseException(e.getMessage(), 0);
    } catch (URISyntaxException e) {
      throw new ParseException(e.getMessage(), 0);
    }

  }
}
