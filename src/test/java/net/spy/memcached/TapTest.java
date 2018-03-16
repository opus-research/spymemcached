package net.spy.memcached;

import java.util.HashMap;
import java.util.Map.Entry;

import net.spy.memcached.tapmessage.ResponseMessage;

public class TapTest extends ClientBaseCase {
	
	@Override
	protected void initClient() throws Exception {
		initClient(new BinaryConnectionFactory() {
			@Override
			public long getOperationTimeout() {
				return 15000;
			}
			@Override
			public FailureMode getFailureMode() {
				return FailureMode.Retry;
			}
		});
	}

	public void testBackfill() throws Exception {
		TapClient tc = new TapClient(AddrUtil.getAddresses("127.0.0.1:11210"));
		tc.tapBackfill(null, null, 10, null, null);
		
		HashMap<String, Boolean> items = new HashMap<String, Boolean>();
		for (int i = 0; i < 25; i++) {
			System.out.println("key"+i);
			client.set("key" + i, 0, "value" + i);
			items.put("key" + i + ",value" + i, new Boolean(false));
		}
		
		while(tc.hasMoreMessages()) {
			ResponseMessage m;
			if ((m = tc.getNextMessage()) != null) {
				String key = m.getKey() + "," + new String(m.getValue());
				if (items.containsKey(key)) {
					items.put(key, new Boolean(true));
				} else {
					fail();
				}
			}
		}
		checkTapKeys(items);
		assertTrue(client.flush().get().booleanValue());
	}
	
	private void checkTapKeys(HashMap<String, Boolean> items) {
		for (Entry<String, Boolean> kv : items.entrySet()) {
			if (!kv.getValue().booleanValue()) {
				fail();
			}
		}
	}
}
