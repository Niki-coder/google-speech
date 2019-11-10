package com.sample.kwd;

import java.util.HashMap;
import java.util.Map;

import com.sample.kwd.interfaces.RequestInterface;

public class GcpRequest implements RequestInterface {
	
	protected Map<String, Object> attributes;
	
	GcpRequest() {
		attributes = new HashMap<String, Object>();
	}
	
	public void setRequestData(Map<String, Object> data) {
		attributes = data;
	}
	
	public Map<String, Object>  getRequestData() {
		return attributes;
	}
	
	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}
	
	public Object getAttribute(String key) throws Exception{
		assert(attributes != null);
		
		Object value = null;
		if (attributes.containsKey(key)) {
			value = attributes.get(key);
		}
		
		return value;
	}
}