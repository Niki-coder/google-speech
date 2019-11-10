package com.sample.kwd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;
import com.google.protobuf.ByteString;
import com.sample.kwd.interfaces.ClientInterface;
import com.sample.kwd.interfaces.RequestInterface;
import com.sample.kwd.interfaces.ResponseInterface;

import java.io.IOException;
import org.apache.log4j.Logger;

public class GcpClient implements ClientInterface{
	
	final static Logger logger = Logger.getLogger(GcpClient.class);
	final static String ATTR_AUDIO_FILENAME = "audioFileName";
	final static String ATTR_RESPONSE = "response";
	final static String ATTR_LANGUAGE = "language";
	
	public GcpClient(String filename) {
		super();
	}
	
	protected List<String> tanscribe(String fileName, String language) {
		
		List<String> textList = new ArrayList<String>();

		SpeechClient speechClient = null;
	    try {
	    	speechClient = SpeechClient.create();
	    	// Reads the audio file into memory
	    	Path path = Paths.get(fileName);
	    	byte[] data = Files.readAllBytes(path);
	    	ByteString audioBytes = ByteString.copyFrom(data);

	    	// Builds the sync recognize request
	    	RecognitionConfig config = RecognitionConfig.newBuilder()
	    			.setEncoding(AudioEncoding.LINEAR16)
	    			.setLanguageCode(language)
	    			.build();
	    	
	    	RecognitionAudio audio = RecognitionAudio.newBuilder()
	    			.setContent(audioBytes)
	    			.build();
	    	
	    	logger.info("Waiting for speech recognition results from google for audio file [" + fileName + "].....");
	    	
	        long lStartTime = System.nanoTime();
	    	RecognizeResponse response = speechClient.recognize(config, audio);
	        long lEndTime = System.nanoTime();

	        long milliSeconds = (lEndTime - lStartTime)/1000000;

	        List<SpeechRecognitionResult> results = response.getResultsList();
	    	
	    	if (results.size() == 0) {
	    		logger.error("Google-Speech-Text API Failed To Transcribe the AudioFile ["+fileName+"]");
	    	}		
	
	    	for (SpeechRecognitionResult result : results) {
	    		// There can be several alternative transcripts for a given chunk of speech. Just use the
	    		// first (most likely) one here.
		        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
		        logger.info("=============================================");
		        logger.info("AudioFile: " + fileName);
		        logger.info("Transcription: " + alternative.getTranscript());
		        logger.info("Confidence  : " + alternative.getConfidence());
		        logger.info("Processing Time: " + milliSeconds + " ms");
		        logger.info("=============================================");
		        textList.add(alternative.getTranscript());
	    	}
	    	} catch (IOException e) {
	    		logger.error("Failed to process audioFile: " + e.getMessage());
	    	}
	    	finally {
	    		speechClient.shutdown();
	    	}


		return textList;
	}
	protected boolean validateRequest(RequestInterface request) {
  		
  		Map<String, Object> requestData = request.getRequestData();
  		if (logger.isDebugEnabled()) {
  			Iterator it = requestData.entrySet().iterator();
  		    while (it.hasNext()) {
  		        Map.Entry pair = (Map.Entry)it.next();
  		        logger.debug(pair.getKey() + " = " + pair.getValue());
  		    }
  		}
  		
  		boolean validRequest = true;
  		if (request != null && !requestData.containsKey(GcpClient.ATTR_AUDIO_FILENAME)) {
  			validRequest = false;
  		}
  		
  		
  		return validRequest;
  	}
  	
	public ResponseInterface sendRequest(RequestInterface request) throws IOException {

		logger.info("Got request for transcribing");
		GcpResponse respData = new GcpResponse();

		if (!validateRequest(request)) {
			throw new IOException("Mandatory Request Parameter \"audioFileName\" Not Found!");
		} else {
			
			// Get the audio file to validate
			String audioFile = (String) request.getRequestData().get(GcpClient.ATTR_AUDIO_FILENAME);
			
			
			String language = "en-US";
			if(request.getRequestData().containsKey(GcpClient.ATTR_LANGUAGE))
				language = (String) request.getRequestData().get(GcpClient.ATTR_LANGUAGE);
			
			
			List<String> response = tanscribe(audioFile,language);
			respData.setAttribute(GcpClient.ATTR_RESPONSE, response);
		}
		
		
		return respData;
	}
}

