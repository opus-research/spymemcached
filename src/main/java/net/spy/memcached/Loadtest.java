package net.spy.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.BulkFuture;

public class Loadtest {
    private static List<InetSocketAddress> adresses = new ArrayList<InetSocketAddress>();
    
    static {
        adresses.add(new InetSocketAddress("127.0.0.1", 11211));
        adresses.add(new InetSocketAddress("10.1.2.82", 11211));
    }
    
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        Properties systemProperties = System.getProperties();
        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger");
        System.setProperties(systemProperties);

        Logger.getLogger("net.spy.memcached").setLevel(Level.ALL);
        
        //get the top Logger
        Logger topLogger = java.util.logging.Logger.getLogger("");

        // Handler for console (reuse it if it already exists)
        Handler consoleHandler = null;
        //see if there is already a console handler
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                //found the console handler
                consoleHandler = handler;
                break;
            }
        }

        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler); 
        }

        //set the console handler to fine:
        consoleHandler.setLevel(java.util.logging.Level.INFO);
        
        final MemcachedClient client = new MemcachedClient(new BinaryConnectionFactory(), adresses);
        
        ExecutorService pool = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 2000; i++) {
            final int ii = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    int count = 1000;
                    for (int i = (ii * count); i < (ii * count) + count ; i++) {
                        List<String> keys = new ArrayList<String>(1000);
                        for (int j = ii; j < ii + 1000; j++) {
                            keys.add("test" + j);
                        }
                        BulkFuture<Map<String, Object>> f = client.asyncGetBulk(keys);
                        try {
                            try {
                                f.get(3000, TimeUnit.MILLISECONDS);
                            } catch (TimeoutException e) {
                               //e.printStackTrace();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Done from " + (ii * count) + " to " + (ii * count + count));
                }
            });
        }
        pool.shutdown();
    }
}
