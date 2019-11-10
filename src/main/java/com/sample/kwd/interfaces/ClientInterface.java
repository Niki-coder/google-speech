package com.sample.kwd.interfaces;

import java.io.IOException;

import com.sample.kwd.interfaces.RequestInterface;
import com.sample.kwd.interfaces.ResponseInterface;

public interface ClientInterface {
	
	public ResponseInterface sendRequest(RequestInterface request) throws IOException;
	
	
}