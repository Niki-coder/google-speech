/*
 * Copyright 2018 Google Inc. 	
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sample.kwd;

// [START speech_quickstart]
// Imports the Google Cloud client library
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;

import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import com.sample.kwd.GcpRequest;
import com.sample.kwd.interfaces.RequestInterface;
import com.sample.kwd.interfaces.ResponseInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import java.util.Properties;

public class KwdDaemon extends Thread {

	final static Logger logger = Logger.getLogger(KwdDaemon.class.getName());
	final static String AUDIO_FILE_DIR_PROP = "audioFileDirectory";
	final static String ARCHIVE_DIR_PROP = "archiveDirectory";
	final static String BLACKLISTED_WORD_FILE = "blackListedWordFile";
	final static String KWD_SERVER_URL = "kwdServerUrl"; //"http://httpbin.org/post";
	final static String LANGUAGE_PROP = "language";
	
	public static void main(String... args)              {
		Properties prop = new Properties();
		try {
			InputStream input = new FileInputStream("./resources/config.properties");
			if (logger.isDebugEnabled()) {
				logger.debug("Reading config file " + input.toString());
			}
			prop.load(input);
		} catch (IOException ex) {
			logger.error("Failed to load config file");
			return;
		} 

		String blWordFile = prop.getProperty(BLACKLISTED_WORD_FILE);
		logger.debug( BLACKLISTED_WORD_FILE + " : " + blWordFile);

		List<String> blWordList = new ArrayList<String>();
		try{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(blWordFile));
			String line;
			while((line = bufferedReader.readLine()) != null) {
              blWordList.add(line);
			}            
		} catch (Exception ex) {
    	  logger.error("Failed to read the black listed words: " + ex.getStackTrace().toString());
    	  return;
		}
	  
		AudioDirMonitor dirMonitor = new AudioDirMonitor(prop, blWordList);
	
		dirMonitor.start();
	}
}

