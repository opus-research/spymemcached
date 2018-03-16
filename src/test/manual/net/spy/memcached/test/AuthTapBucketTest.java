/**
 * Copyright (C) 2014 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package net.spy.memcached.test;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.NodeLocator;
import net.spy.memcached.TapClient;
import net.spy.memcached.tapmessage.ResponseMessage;
import net.spy.memcached.tapmessage.TapStream;

import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.vbucket.ConfigurationProviderHTTP;
import com.couchbase.client.vbucket.config.Bucket;
import com.couchbase.client.vbucket.config.Config;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
 * Sample of using Couchbase TAP to extract all of the data from
 * only one node.
 *
 * This is a bit messy (not everything should be static) but it
 * does demonstrate the concepts.
 *
 * The idea here is to do two things.  We connect to the one node
 * we care about, and set up a TAP dump against it.  We also set
 * up a config connection to get the configuration for the bucket
 * then during the TAP dump, we check to see if the item we received
 * is actually "active" on that given node.  This is because all nodes
 * have both active and replica vbuckets and TAP does not allow us to
 * filter to just one.
 *
 * A possible improvement here would be to TAP just the active vbuckets.
 * That requires a bit more config juggling.  The solution here is quick
 * to implement at the cost of network IO.
 *
 */
public class AuthTapBucketTest {

    private static NodeLocator cbLocator;
    private static String bucket = "named";
    private static String bucketPass = "named";
    private static String host = "192.168.1.204";
    private static String hostInetMCSocket = host + ":11210";
    private static ConfigurationProviderHTTP configProviderHttp;
    private static Bucket theBucket;
    private static Config bucketConfig;

    public static void main (String args[]) throws IOException, ConfigurationException {


        List<URI> baseList = Arrays.asList(
                URI.create("http://192.168.1.201:8091/pools"),
                URI.create("http://192.168.0.202:8091/pools"));




        // be cautious with these, since each configProvider and connFactory will be getting
        // a cluster map.  if there are many instances of this class, the cluster can be
        // overwhelmed
        configProviderHttp = new ConfigurationProviderHTTP(baseList, bucket, bucketPass);

        theBucket = configProviderHttp.getBucketConfiguration(bucket);
        bucketConfig = theBucket.getConfig();

        TapClient tc = new TapClient("named", "named", AddrUtil.getAddresses(hostInetMCSocket));

        TapStream testing = tc.tapDump("testing");

        while (tc.hasMoreMessages()) {
            ResponseMessage m;
            if ((m = tc.getNextMessage()) != null) {
                // WARNING: this isn't really right since it doesn't consider transcoders, but will generally work
                // if you're storing JSON strings and your keys are strings.  Watch your encodings!
                String item = m.getKey() + "," + new String(m.getValue());
                String key = m.getKey();
                System.err.println("Got this key: \"" + key + "\" Is active? " + isActiveForKey(m.getKey()));
            }
        }

    }

    /**
     * Return whether or not the node being used is active for the given key.
     *
     * A vbucket may be in the "replica" state in more than one place on a
     * cluster but it will be active in only one place.  This will allow us
     * to filter out what is active on this vbucket and what is not.
     *
     * This is not the correct way to use any of these internal to CouchbaseClient interfaces,
     * but works for our purposes here. It's for reuse of code, but not in a good way.
     * watch out for possible differences in name because one side uses hostname and the other
     * side uses IP address.
     *
     * @param key
     * @return boolean indicating this node is active or not
     */
    public static boolean isActiveForKey(String key) {
        String lookup = bucketConfig.getServer(bucketConfig.getMaster(bucketConfig.getVbucketByKey(key)));
        return hostInetMCSocket.equals(lookup);
    }

}
