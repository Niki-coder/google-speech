package com.sample.kwd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KwdData {
	
    private String audioFileName;
    private List<KwdKeyCountData> keywords = new ArrayList<KwdKeyCountData>();
	
    public String getAudioFileName() {
		return audioFileName;
	}
	
	public void setAudioFileName(String audioFileName) {
		this.audioFileName = audioFileName;
	}
	
	public List getKeywords() {
		return keywords;
	}
	
	public void setKeywords(List<KwdKeyCountData> keywords) {
		this.keywords = keywords;
	}
	
	public void addKeyword(KwdKeyCountData keyData ) {
		keywords.add(keyData);
	}
}
