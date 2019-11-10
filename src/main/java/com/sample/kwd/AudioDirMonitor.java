package com.sample.kwd;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sample.kwd.interfaces.ResponseInterface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioDirMonitor extends Thread {

    String audioDirectory;
    String archiveDirectory;
    String serverUrl;
    String languageProp;
    Properties configData;
    
    ExecutorService executor = Executors.newFixedThreadPool(20);

    
    List<String> blackListedWordList;
    
	final static Logger logger = Logger.getLogger(AudioDirMonitor.class);

    public AudioDirMonitor(Properties prop, List<String> blWordList) {
		
    	audioDirectory = prop.getProperty(KwdDaemon.AUDIO_FILE_DIR_PROP);
    	archiveDirectory = prop.getProperty(KwdDaemon.ARCHIVE_DIR_PROP);
    	serverUrl = prop.getProperty(KwdDaemon.KWD_SERVER_URL);
    	languageProp = prop.getProperty(KwdDaemon.LANGUAGE_PROP);
    	configData = prop;
    	
    	if (logger.isDebugEnabled()) {
			logger.debug("audioDirName : " + audioDirectory);
			logger.debug("archiveDirName : " + archiveDirectory);
			logger.debug("server URL     : " + serverUrl);
			logger.debug("language Prop     : " + languageProp);
		}
       
        blackListedWordList = blWordList;
    }

    public void run() { 
        
        Path audioDirectoryPath = Paths.get(audioDirectory);
        Path archiveDirectoryPath = Paths.get(archiveDirectory);

        logger.info("Running AudioDirMonitor [" + audioDirectoryPath.toString() + ", " +archiveDirectoryPath + "]");
        
        if (!audioDirectoryPath.toFile().exists()) {
        	logger.error("Audio Directory ["+audioDirectoryPath.toString()+"] doesn't exist. Exiting!");
        	return;
        }
        
        if (!archiveDirectoryPath.toFile().exists()) {
        	logger.error("Archive Directory ["+archiveDirectoryPath.toString()+"] doesn't exist. Exiting!");
        	return;
        }

        WatchService watcher = null;
        try {

        	watcher = FileSystems.getDefault().newWatchService();
        } 
        catch(IOException ex) {
			logger.error("Error In Initializing Directory Watcher service.....Exiting! "+ ex.toString());
			
        }

		while (true) {
            try {
                audioDirectoryPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
                logger.info("Waiting For New Requests....");  
                WatchKey watckKey = watcher.take();
                for (WatchEvent<?> event : watckKey.pollEvents()) {
                	logger.info("Found File To Process [" + event.context().toString() + "]");
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    	String audioFileName = event.context().toString(); 
                    	                 	
                    	Map<String, Integer> matches = new HashMap<String, Integer>();
                    	
                    	int matchCount = 0;
                    	
                    	Runnable worker = new S2TExecutorThread(configData, audioFileName, blackListedWordList); 
                    	
                    	if (logger.isDebugEnabled()) {
                    		logger.debug("executing worker thread: " + worker);
                    	}
             
                    	executor.execute(worker);              	
                    }
                }
           
                watckKey.reset();
                
            } catch (Exception e) {
                System.out.println("Error: " + e.toString());
            }    
		}
    }
}


