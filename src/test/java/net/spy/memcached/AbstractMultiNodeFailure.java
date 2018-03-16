package net.spy.memcached;

import junit.framework.TestCase;
import net.spy.memcached.vbucket.ConfigurationProviderHTTP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.couchbase.mock.CouchbaseMock;

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
 * Tests case when 50% of nodes are fail.
 * User: vitaly.rudenya
 * Date: 22.06.11
 * Time: 08:49
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

    private Process couchBaseMockProcess;

    static {
        try {
            BASE_LIST.add(new URI(BASE_LIST_URL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private MemcachedClient memcachedClient;

    private Log logger = LogFactory.getLog(this.getClass());

    protected abstract CouchbaseMock.BucketType getBucketType();

    public void setUp() throws Exception {
        try {

            CouchbaseMockRunner couchbaseMock = new CouchbaseMockRunner();
            couchbaseMock.setDaemon(true);
            couchbaseMock.start();

            try {
                //wait for mock server startup
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                logger.error(ex);
            }

            final URL startupUrl = new URL("http://localhost:8091/setup/startNodes?numnodes=2&numVBuckets=512");
            URLConnection connection = urlConnBuilder(startupUrl);
            connection.getInputStream().close();

            try {
                //wait for mock server nodes startup
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                logger.error(ex);
            }

            memcachedClient = new MemcachedClient(BASE_LIST, BUCKET_NAME, USER_NAME, USER_PASSWORD);
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
    }

    public void testNodeFail() throws Exception {

        memcachedClient.set(OBJ_KEY, 100000, OBJ_KEY);

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            logger.error(ex);
        }

        failPrimaryNode();

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            logger.error(ex);
        }
        try {
                    assertEquals(OBJ_KEY, memcachedClient.get(OBJ_KEY));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            fail("Fail during getting data with primary non active node");
        }
    }

    public void tearDown() throws Exception {
        couchBaseMockProcess.destroy();
    }

    /**
     * Fail primary node
     */
    private void failPrimaryNode() {

        int port = ((InetSocketAddress) memcachedClient.getNodeLocator().getPrimary(OBJ_KEY).getSocketAddress()).
                getPort();

        logger.info("Failing primary node on port no " + port);

        try {
            final URL shutdownNodeUrl = new URL("http://localhost:8091/setup/failNode?portNo=" + port);
            URLConnection connection = urlConnBuilder(shutdownNodeUrl);
            connection.getInputStream().close();
        } catch (IOException e) {
            fail("Can't shutdown primary node");
        }
    }

    private static URLConnection urlConnBuilder(URL specURL) throws IOException {
        URLConnection connection = specURL.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("user-agent", "spymemcached vbucket client");
        connection.setRequestProperty("X-memcachekv-Store-Client-Specification-Version", ConfigurationProviderHTTP.CLIENT_SPEC_VER);
        connection.setRequestProperty("Authorization", buildAuthHeader(HTTP_USER_NAME, HTTP_PASSWORD));

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
        String encodedText = org.apache.commons.codec.binary.Base64.encodeBase64String(clearText.toString().getBytes());
        char[] encodedWoNewline = new char[encodedText.length() - 2];
        encodedText.getChars(0, encodedText.length() - 2, encodedWoNewline, 0);
        return "Basic " + new String(encodedWoNewline);
    }

    private static String getClassPath() {

        String[] libs = System.getProperty("java.class.path").split(File.pathSeparator);

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
                        new ProcessBuilder("java", "-classpath", classPath, "org.couchbase.mock.CouchbaseMock",
                                getBucketType().toString());
                couchBaseMockProcess = processBuilder.start();

                StreamReader error = new StreamReader(couchBaseMockProcess.getErrorStream());
                error.setDaemon(true);
                error.start();

                StreamReader output = new StreamReader(couchBaseMockProcess.getInputStream());
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
