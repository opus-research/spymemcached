/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket.config;

import java.io.File;

import org.codehaus.jettison.json.JSONObject;

/**
 * A ConfigFactory.
 */
public interface ConfigFactory {

  Config create(File file);

  Config create(String data);

  Config create(JSONObject jsonObject);
}
