/**
 * @author Couchbase <info@couchbase.com>
 * @copyright 2011 Couchbase, Inc.
 * All rights reserved.
 */

package net.spy.memcached.vbucket;

import junit.framework.TestCase;
import net.spy.memcached.TestConfig;
import net.spy.memcached.vbucket.config.ConfigurationParserMock;

import java.net.URI;

/**
 * A BucketMonitorTest.
 */
public class BucketMonitorTest extends TestCase {
  private static final String USERNAME = "";
  private static final String PASSWORD = "";
  private static final String STREAMING_URI = "http://" + TestConfig.IPV4_ADDR
      + ":8091/pools/default/bucketsStreaming/default";
  private static final String BUCKET_NAME = "default";
  private static final ConfigurationParserMock CONFIG_PARSER =
      new ConfigurationParserMock();

  public void testInstantiate() throws Exception {

    BucketMonitor bucketMonitor =
        new BucketMonitor(new URI(STREAMING_URI), BUCKET_NAME, USERNAME,
            PASSWORD, CONFIG_PARSER);
    assertEquals(USERNAME, bucketMonitor.getHttpUser());
    assertEquals(PASSWORD, bucketMonitor.getHttpPass());
  }

  public void testObservable() throws Exception {
    BucketMonitor bucketMonitor =
        new BucketMonitor(new URI(STREAMING_URI), BUCKET_NAME, USERNAME,
            PASSWORD, CONFIG_PARSER);

    BucketObserverMock observer = new BucketObserverMock();
    bucketMonitor.addObserver(observer);

    bucketMonitor.addObserver(observer);

    bucketMonitor.startMonitor();

    assertTrue("Update for observer was not called.",
        observer.isUpdateCalled());
    bucketMonitor.shutdown();
  }
}
