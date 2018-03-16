package cache.prototype;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.transcoders.BaseSerializingTranscoder;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;

public class CouchCacheConnector implements ConnectionObserver {

	private StatCollector collector = null;
	private CouchbaseClient client = null;
	
	public CouchCacheConnector(String host, StatCollector collector) throws URISyntaxException, IOException {
		this(host, collector, "default");
	}
	
	public CouchCacheConnector(String host, StatCollector collector, String bucket) throws URISyntaxException, IOException {

		this.collector = collector;
        List<URI> baseURIs = new ArrayList<URI>();
        String[] hostArray = host.split(",");
        for(String tmpHost : hostArray){
        	URI base = new URI(String.format("http://%s:8091/pools", tmpHost));
        	baseURIs.add(base);
        }
        
//        CouchbaseConnectionFactoryBuilder builder = new CouchbaseConnectionFactoryBuilder();
//        builder.setFailureMode(FailureMode.Redistribute);
//        builder.setHashAlg(DefaultHashAlgorithm.NATIVE_HASH);
        
        //builder.setTimeoutExceptionThreshold(50);
        //builder.setFailureMode(FailureMode.Redistribute);
        //builder.setHashAlg(DefaultHashAlgorithm.CRC_HASH);
        //builder.setHashAlg(DefaultHashAlgorithm.NATIVE_HASH);
//		CouchbaseConnectionFactory cf = builder.buildCouchbaseConnection(baseURIs, "default", "", "");
        
        CouchbaseConnectionFactory cf = new CouchbaseConnectionFactory(baseURIs, bucket, "");

	
        System.out.println("Using connection factory : " + cf);
        client = new CouchbaseClient(cf);
	/*
        BaseSerializingTranscoder tr = (BaseSerializingTranscoder) client.getTranscoder();
        tr.setCompressionThreshold(120000);
	*/
        
        client.addObserver(this);
        
        

	}
	
	public void put(String key, int exp, Object object){
		long startTime = System.currentTimeMillis();
		
		OperationFuture<Boolean> future = null;
		try{
			future = client.set(key, exp, object);
			Boolean ret = future.get(450, TimeUnit.MILLISECONDS);
		}catch(Exception e){
			if(future != null) 
				System.err.println("Put Error " + future.getStatus().getMessage());
			collector.incrementPutErrorCount();
			//e.printStackTrace();
		}finally{
			collector.addPutLateny(System.currentTimeMillis() - startTime);
			collector.incrementPutCount();
		}
	}
	
	
	public Object get(String key){
		long startTime = System.currentTimeMillis();
		Object obj = null;
		GetFuture<Object> future = null;
		try{
			future =  client.asyncGet(key);
			obj = future.get(450, TimeUnit.MILLISECONDS);
		}catch(Exception e){
			if(future != null) 
				System.err.println("Get Error " + future.getStatus().getMessage());
			collector.incrementGetErrorCount();
		}finally{
			collector.addGetLatency(System.currentTimeMillis() - startTime);
			collector.incrementGetCount();
		}
		return obj;
	}

	public void delete(String key){
		long startTime = System.currentTimeMillis();
		try{
			client.delete(key);
		}catch(Exception e){
			collector.incrementDelErrorCount();
		}finally{
			collector.addDelLateny(System.currentTimeMillis() - startTime);
			collector.incrementDelCount();
		}
	}

	@Override
	public void connectionEstablished(SocketAddress sa, int reconnectCount) {
        System.out.println("Connection established with "
                + sa.toString() + " Reconnected count: " + reconnectCount);
	}

	@Override
	public void connectionLost(SocketAddress sa) {
        System.out.println("Connection lost to " + sa.toString());
	}
	
	public void shutdown(){
		client.shutdown();
	}
	
	public MemcachedClient getClient(){
		return client;
	}
}
