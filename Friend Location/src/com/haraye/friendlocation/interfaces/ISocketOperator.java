package com.haraye.friendlocation.interfaces;

import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONObject;


public interface ISocketOperator {
	
	//public String sendHttpRequest(String params);
	public JSONObject sendHttpRequestJson(String params);
	public String makeHttpRequest(List<NameValuePair> params);
	public int startListening(int port);
	public void stopListening();
	public void exit();
	public int getListeningPort();

}
