package net.spy.memcached.protocol.couch;

public interface ViewRow {
  String getId();
  
  String getKey();
  
  String getValue();
  
  Object getDocument();
}
