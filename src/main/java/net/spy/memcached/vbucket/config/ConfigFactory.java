package net.spy.memcached.vbucket.config;

import org.codehaus.jettison.json.JSONObject;

public interface ConfigFactory {

    Config createConfigFromFile(String filename);

    Config createConfigFromString(String data);

    Config createConfigFromJSON(JSONObject jsonObject);
}
