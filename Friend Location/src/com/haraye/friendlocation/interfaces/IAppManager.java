package com.haraye.friendlocation.interfaces;

import java.io.UnsupportedEncodingException;

import org.json.JSONObject;


public interface IAppManager {

	public boolean isNetworkConnected();
	
	public String signUpUser(String displaynameText, String usernameText, String passwordText, String email, String img);
	public String updateImage(String id, String img);
	public void getlocation();
	
	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException; 
	public boolean isUserAuthenticated();
	public String getUsername();
	
	public JSONObject getNonFriendsList();
	public void addNewFriendRequest(String friendUserId);
	public void sendFriendsReqsResponse(String friendId,	String responseString);
	
	public String sendMessage(String username,String tousername, String message) throws UnsupportedEncodingException;
	
	public void exit();
	
}
