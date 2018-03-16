package net.spy.memcached;

import junit.framework.TestCase;
import net.spy.memcached.vbucket.ConfigurationProviderHTTP;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests case when 50% of nodes have failed.
 */
public abstract class AbstractMultiNodeFailure extends TestCase {

  private static final String OBJ_KEY = "blah1";
  private static final String BASE_LIST_URL = "http://localhost:8091/pools";
  private static final String BUCKET_NAME = "default";
  private static final String USER_PASSWORD = "password";
  private static final String USER_NAME = "Administrator";
  private static final List<URI> BASE_LIST = new ArrayList<URI>();
  private static final String HTTP_USER_NAME = "Administrator";
  private static final String HTTP_PASSWORD = "password";
  private static final String STARTUP_URL =
    "http://localhost:8091/setup/startNodes?numnodes=2&numVBuckets=512";
  private static final String SHUTDOWN_NODE_URL =
    "http://localhost:8091/setup/failNode?portNo=";
  public static final String BASE_BUCKET_TYPE = "BASE";
  public static final String CACHE_BUCKET_TYPE = "CACHE";
  private Process couchBaseMockProcess;

  static {
    try {
      BASE_LIST.add(new URI(BASE_LIST_URL));
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }
  private MemcachedClient membaseClient;

  protected abstract String getBucketType();

  @Override
  public void setUp() throws Exception {
    CouchbaseMockRunner couchbaseMock = new CouchbaseMockRunner();
    couchbaseMock.setDaemon(true);
    couchbaseMock.start();

    Thread.sleep(1000);

    final URL startupUrl = new URL(STARTUP_URL);
    URLConnection connection = urlConnBuilder(startupUrl);
    connection.getInputStream().close();

    Thread.sleep(1000);

    membaseClient = new MembaseClient(BASE_LIST, BUCKET_NAME, USER_NAME,
      USER_PASSWORD);
  }

  public void testNodeFail() throws Exception {

    membaseClient.set(OBJ_KEY, 100000, OBJ_KEY);

    Thread.sleep(500);
    failPrimaryNode();
    Thread.sleep(500);

    assertEquals("Fail during getting data with primary non active node.",
      OBJ_KEY, membaseClient.get(OBJ_KEY));
  }

  public void tearDown() throws Exception {
    couchBaseMockProcess.destroy();
  }

  /**
   * Fail primary node
   */
  private void failPrimaryNode() {

    InetSocketAddress address =
      (InetSocketAddress) membaseClient.getNodeLocator()
                                       .getPrimary(OBJ_KEY).getSocketAddress();
    int port = address.getPort();

    try {
      final URL shutdownNodeUrl = new URL(SHUTDOWN_NODE_URL + port);
      URLConnection connection = urlConnBuilder(shutdownNodeUrl);
      connection.getInputStream().close();
    } catch (IOException e) {
      fail("Can't shutdown primary node. " + e.getLocalizedMessage());
    }
  }

  private static URLConnection urlConnBuilder(URL specURL) throws IOException {
    URLConnection connection = specURL.openConnection();
    connection.setRequestProperty("Accept", "application/json");
    connection.setRequestProperty("user-agent", "spymemcached vbucket client");
    connection.setRequestProperty(
      "X-memcachekv-Store-Client-Specification-Version",
      ConfigurationProviderHTTP.CLIENT_SPEC_VER);
    connection.setRequestProperty("Authorization",
      buildAuthHeader(HTTP_USER_NAME, HTTP_PASSWORD));

    return connection;

  }

  private static String buildAuthHeader(String username, String password) {
    // apparently netty isn't familiar with HTTP Basic Auth
    StringBuilder clearText = new StringBuilder(username);
    clearText.append(':');
    if (password != null) {
      clearText.append(password);
    }
    // and apache base64 codec has extra \n\l we have to strip off
    String encodedText =
      Base64.encodeBase64String(clearText.toString().getBytes());
    char[] encodedWoNewline = new char[encodedText.length() - 2];
    encodedText.getChars(0, encodedText.length() - 2, encodedWoNewline, 0);
    return "Basic " + new String(encodedWoNewline);
  }

  private static String getClassPath() {

    String[] libs =
      System.getProperty("java.class.path").split(File.pathSeparator);

    for (String lib : libs) {
      if (lib.contains("CouchbaseMock")) {
        return lib;
      }
    }

    return "";
  }

  private class StreamReader extends Thread {

    private InputStream in;

    public StreamReader(InputStream in) {
      this.in = in;
    }

    public void run() {

      byte[] buf = new byte[1024];
      while (true) {
        try {
          int len = in.read(buf);
          if (len > 0) {
            String error = new String(buf, 0, len);
            System.out.print(error);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class CouchbaseMockRunner extends Thread {

    public void run() {

      String classPath = getClassPath();

      try {
        ProcessBuilder processBuilder =
          new ProcessBuilder("java", "-classpath",
          classPath, "org.couchbase.mock.CouchbaseMock",
          getBucketType());
        couchBaseMockProcess = processBuilder.start();

        StreamReader error =
          new StreamReader(couchBaseMockProcess.getErrorStream());
        error.setDaemon(true);
        error.start();

        StreamReader output =
          new StreamReader(couchBaseMockProcess.getInputStream());
        output.setDaemon(true);
        output.start();

        couchBaseMockProcess.waitFor();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
