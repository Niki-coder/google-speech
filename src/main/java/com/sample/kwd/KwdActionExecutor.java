package com.sample.kwd;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.kwd.interfaces.ExecutorInterface;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;


public class KwdActionExecutor implements ExecutorInterface {

	String serverUrl;
	Map<String, Integer> blackListHz;
	String audioFileName;
	final static Logger logger = Logger.getLogger(KwdActionExecutor.class);


	public KwdActionExecutor(String url, String fileName, Map<String, Integer> blListHz) {
		serverUrl = url;
		blackListHz = blListHz;
		audioFileName = fileName;
	}
	
	public boolean execute() {
		
	   ObjectMapper mapper = new ObjectMapper();
	   KwdData kwdData = new KwdData();
	   kwdData.setAudioFileName(audioFileName);
	   Iterator it = blackListHz.entrySet().iterator();
	   while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        //System.out.println(pair.getKey() + " = " + pair.getValue());
	        KwdKeyCountData keyWordFrequency = new KwdKeyCountData();
	        keyWordFrequency.setKeyword((String)pair.getKey());
	        keyWordFrequency.setFrequency((Integer) pair.getValue());
	        kwdData.addKeyword(keyWordFrequency);
	   }
	   
	   String jsonString;
	   
	   try {
		   jsonString = mapper.writeValueAsString(kwdData);
	   } catch (Exception e) {
			e.printStackTrace();
			return false;
	   }

	   logger.info("JSON STRING: " + jsonString);
	   
	   try  {
		   CloseableHttpClient httpclient = HttpClients.createDefault();

            List<NameValuePair> form = new ArrayList<NameValuePair>();
            
            HttpPost httpPost = new HttpPost(serverUrl);
			StringEntity entity = new StringEntity(jsonString);

            httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

            logger.info("Executing request : " + httpPost.getRequestLine());

            ResponseHandler<String> responseHandler = new ResponseHandler<String>(){
                
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					
					// TODO Auto-generated method stub
					int status = response.getStatusLine().getStatusCode();
	                if (status >= 200 && status < 300) {
	                    HttpEntity responseEntity = response.getEntity();
	                    return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
	                } else {
	                    throw new ClientProtocolException("Unexpected response status: " + status);
	                }
					//return null;
	                
				}
            };
            String responseBody = httpclient.execute(httpPost, responseHandler);
            logger.info("Server Response : "+ responseBody);
        } catch (IOException ex) {
        	logger.error("Failed to Execute the KWD Request");
        	return false;
        }
		return true;
	}
	
}
