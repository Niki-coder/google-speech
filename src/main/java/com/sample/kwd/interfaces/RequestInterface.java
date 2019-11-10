package com.sample.kwd.interfaces;

import java.util.Map;

public interface RequestInterface {
	public void setRequestData(Map<String, Object> requestAttributes);
	public Map<String, Object>  getRequestData();
}