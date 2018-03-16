package cache.prototype;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Driver implements Runnable {

  private static Logger logger = Logger.getLogger(Driver.class.getName());
  private static final DecimalFormat dFormat = new DecimalFormat("#0.0");
  private ThreadPoolExecutor tpe;
  private Properties prop = new Properties();
  private volatile boolean finished = true;
  private String host;
  private String data;
  private int objectTTL;
  private StatCollector collector;
  private CouchCacheConnector connector = null;
  private ConcurrentHashMap<String, Integer> keyMap = new ConcurrentHashMap<String, Integer>();
  private int threads;
  private String keyPrefix;

  public Driver(StatCollector collector, int msgSize, int objectTTL,
    int threads, String keyPrefix, String bucket) throws Exception {
    String configFile = System.getProperty("configFile",
      "config.properties");
    prop.load(this.getClass().getClassLoader().getResourceAsStream(configFile));
    this.host = prop.getProperty("host");

    this.collector = collector;
    this.objectTTL = objectTTL;
    this.threads = threads;

    //Build a character array of desired size
    char arr[] = new char[msgSize];
    java.util.Arrays.fill(arr, 'A');
    data = new String(arr);

    this.keyPrefix = keyPrefix;

    this.connector = new CouchCacheConnector(host, collector, bucket);
    System.out.println("Connector has been created.");
  }

  /*
   * Start the load
   */
  public void start() {
    tpe = new ThreadPoolExecutor(threads, threads, 60,
      TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    for (int i = 0; i < threads; i++) {
      tpe.submit(this);
    }
    finished = false;
  }

  /*
   * Pre-Load keys into the cache
   */
  public void storeObjects(StatCollector collector, String keySuffix) {

    try {
      String configFile = System.getProperty("configFile",
        "config.properties");
      Properties p = new Properties();
      p.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile));

      int noOfObjects =
        Integer.valueOf(p.getProperty("preload.noOfObjects", "1000"));
      int objectTTL =
        Integer.valueOf(p.getProperty("preload.objectTTL", "300"));

      System.out.println("num of objects " + noOfObjects);
      if (noOfObjects > 0) {
        for (int i = 0; i < noOfObjects; i++) {
          String key = System.nanoTime() + keySuffix;
          String putKey = key;
          connector.put(putKey, objectTTL, new POJO(1, data));
          keyMap.put(putKey, 1);
          if (i % 100 == 0) {
            logger.info("Loaded: count " + i);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /*
   * Print statistical data every 5 seconds
   */
  public void printStats(long intervalReportTime) {
    long putCount = collector.getPutCount().get();
    long getCount = collector.getGetCount().get();

    logger.info(
      " putCnt= " + putCount
      + " getCnt= " + getCount
      + " putRate= " + dFormat.format(((double) putCount) / intervalReportTime)
      + " getRate=" + dFormat.format(((double) getCount) / intervalReportTime)
      + " putLatency= "
      + ((putCount != 0)
      ? collector.getPutLatency().get()
      / (putCount * 1000) : 0)
      + " getLatency= " + ((getCount != 0)
      ? collector.getGetLatency().get()
      / (getCount * 1000) : 0)
      + " PutErrCnt= " + collector.getPutErrorCount()
      + " GetErrCnt= " + collector.getGetErrorCount()
      + " GetStaleCnt= " + collector.getStaleCount()
      + " UnAvail "
      + connector.getClient().getUnavailableServers());
    collector.reset();
  }

  public boolean isRunning() {
    return !finished;
  }

  public void stopRunning() {
    finished = true;
  }

  public void run() {
    testFailover();
  }

  /*
   * Iterate through the list of pre-loaded keys and re-load them if they are not
   * available while changes occur in the cluster
   */
  public void testFailover() {
    int sleep = Integer.parseInt(System.getProperty("sleep", "100"));

    int factor =
      Integer.parseInt(System.getProperty("factor", "1"));
    long counter = 0;
    try {
      CouchCacheConnector couch = connector;
      for (int i = 0; i < Integer.MAX_VALUE && !finished; i++) {
        Iterator<String> en = keyMap.keySet().iterator();
        while (en.hasNext()) {
          try {
            String key = en.next();
            Object obj = couch.get(key);
            //Put the object
            if (obj == null) {
              collector.incrementGetCount();
              couch.put(key, objectTTL, data);
            }
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            if (counter++ % factor == 0) {
              Thread.sleep(sleep);
            }
          }
        }
      }
    } catch (Exception e) {
    }
  }

  public static void main(String[] args) {

    int numIntervals =
      Integer.parseInt(System.getProperty("intervals", "32000"));
    int intervalReportTime =
      Integer.parseInt(System.getProperty("itime", "5"));
    int threadCount = Integer.parseInt(System.getProperty("threads", "1"));
    int msgSize = Integer.parseInt(System.getProperty("msgsize", "5120"));
    int objectTTL = Integer.parseInt(System.getProperty("objectTTL", "300"));

    String keyPrefix =
      System.getProperty("keyprefix", "_1994190_RESG-plabq13.dev.sabre.com.1333635157880-9");
    String bucket = System.getProperty("bucket", "CBucket1");
    logger.log(Level.INFO, "Number of Intervals : " + numIntervals);
    logger.log(Level.INFO, "Seconds per interval : " + intervalReportTime);
    logger.log(Level.INFO, "Number of concurrent threads : " + threadCount);

    Driver driver = null;
    try {
      StatCollector collector = new StatCollector();

      driver = new Driver(collector, msgSize, objectTTL, threadCount, keyPrefix, bucket);
      driver.storeObjects(collector, keyPrefix);
      driver.start();

      int intervalCounter = 0;
      while (intervalCounter < numIntervals && driver.isRunning()) {
        Thread.sleep(intervalReportTime * 1000);
        driver.printStats(intervalReportTime);
        intervalCounter++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (driver != null) {
        driver.stopRunning();
      }
    }

  }
}
