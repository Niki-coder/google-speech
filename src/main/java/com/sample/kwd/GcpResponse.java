package com.sample.kwd;

import java.util.HashMap;
import java.util.Map;

import com.sample.kwd.interfaces.ResponseInterface;

public class GcpResponse implements ResponseInterface{

	protected Map<String, Object> attributes;
	
	GcpResponse() {
		attributes = new HashMap();
	}
	
	public Map<String, Object> getResponseData() {
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