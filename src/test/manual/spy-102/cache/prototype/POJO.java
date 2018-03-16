package cache.prototype;

import java.io.IOException;
import java.io.Serializable;

public class POJO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 875469135296034813L;
	
	private int version;
	private String data;

	public POJO(int version, String data){
		this.version = version;
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
}
