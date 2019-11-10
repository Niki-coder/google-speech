package com.sample.kwd;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import org.apache.log4j.Logger;

import com.sample.kwd.interfaces.ResponseInterface;

public class S2TExecutorThread implements Runnable {

    private String transcribedLanguageCode = "en-US";
    private String audioDirectory = "audioFiles";
    private String archiveDirectory = "archive";
    private String kwdServerURL;
    private String audioFileName;
    List<String> blackListedWordList;
    final int MATCH_COUNT_THRESHOLD = 1;

	final static Logger logger = Logger.getLogger(S2TExecutorThread.class);

    public S2TExecutorThread(Properties prop, String audioFile, List<String> blWords) {
    	
    	this.audioFileName = audioFile;
    	this.blackListedWordList = blWords;
    	
    	if (prop.containsKey(KwdDaemon.AUDIO_FILE_DIR_PROP)) {
    		this.audioDirectory = prop.getProperty(KwdDaemon.AUDIO_FILE_DIR_PROP);	
    	}
       	
    	if (prop.containsKey(KwdDaemon.ARCHIVE_DIR_PROP)) {
    		this.archiveDirectory = prop.getProperty(KwdDaemon.ARCHIVE_DIR_PROP);
    	}
    	
    	if (prop.containsKey(KwdDaemon.KWD_SERVER_URL)) {
    		this.kwdServerURL = prop.getProperty(KwdDaemon.KWD_SERVER_URL);
    	}
    	
    	if (prop.containsKey(KwdDaemon.LANGUAGE_PROP)) {
    		this.transcribedLanguageCode = prop.getProperty(KwdDaemon.LANGUAGE_PROP);
    	}
    	
    	logger.info("Creating S2TExeuctorThread for audioFile ["+audioFile+"]");
    }

    @Override
    public void run() {
    	List<String> textList = transcribeAudioFile();
    }

    private List<String> transcribeAudioFile() {
      	 
    	String audioFilePathString = this.archiveDirectory +  File.separator + this.audioFileName;
    	if (logger.isDebugEnabled()) {
    		logger.debug("Preparing to transcribing audio file : [" + audioDirectory + "]");
    	}
 
    	GcpClient speechClient = new GcpClient(audioFilePathString);
    	GcpRequest gcpRequest = new GcpRequest();
    	gcpRequest.setAttribute(GcpClient.ATTR_AUDIO_FILENAME, audioFilePathString);
    	gcpRequest.setAttribute(GcpClient.ATTR_LANGUAGE, this.transcribedLanguageCode);

    	ResponseInterface response;
    	List<String> textList = null;
    	
		try {
			response = speechClient.sendRequest(gcpRequest);
			textList = (List<String>) response.getResponseData().get(GcpClient.ATTR_RESPONSE);
			kwdAction(textList);
	        
		} catch (IOException e) {
			logger.error("Failed to transcribe audio file ["+audioFilePathString+"] with error ["+ e.toString()+"]");
		}
        
        
        return textList;
        
    }

    private void kwdAction(List<String> textList) {

    	Map<String, Integer> matches = new HashMap<String, Integer>();
    	int matchCount = 0;
    	
		for(String str: textList) {
			logger.info("Analyzing transcbied message : ["+str+"]");
			
			for(String blWord: blackListedWordList) {
				logger.debug("Checking black listed word ["+blWord+"]");
				
				if (str.contains(blWord)) {
					++matchCount;
					
					if (matches.get(blWord) == null) {
						matches.put(blWord, new Integer(1));
					} else {
						Integer count = matches.get(blWord) + 1;
						matches.put(blWord, count);
					}
				}
			}
		}

		logger.info("Match count is = " + matchCount);

		if (matchCount >= MATCH_COUNT_THRESHOLD) {  
			logger.info("Blacklisted word crossed threshold" );
			KwdActionExecutor action = new KwdActionExecutor(this.kwdServerURL, audioFileName, matches);
			action.execute();
		} else {
			logger.info("No BlackListed Words Found in the transcribed message");
		}
		
    	String audioFilePathString = this.audioDirectory +  File.separator + this.audioFileName;
		File destFile = new File(this.archiveDirectory.toString() + File.separator + audioFileName);

		logger.info("Processed audio file " + audioFilePathString + ". Archiving it now");
		
		File sourceFile = new File(audioFilePathString);
		
		if (sourceFile.renameTo(destFile)) {
			logger.info("Deleting the source file " + sourceFile.toString());
			logger.info("Moved the the source file to " + destFile.toString());
			sourceFile.delete();
		} else {
			logger.error("Failed to archive ["+sourceFile.toString()+"] to ["+destFile.toString()+"]");
		}	
    }
    
    
    @Override
    public String toString(){
    	String description = "Audio Directory: [ " + this.audioDirectory + "]\n" +
    						 "Archive Directory: [ " + this.archiveDirectory + "]\n" +
    						 "Language: [" + this.transcribedLanguageCode + "]\n" +
    						 "Kwd Server URL: [" + this.kwdServerURL + "]\n";
    
        return description;
    }
}