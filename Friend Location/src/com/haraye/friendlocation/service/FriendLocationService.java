package com.haraye.friendlocation.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.haraye.friendlocation.Common;
import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.interfaces.ISocketOperator;
import com.haraye.friendlocation.tools.FriendsController;
import com.haraye.friendlocation.tools.MessageController;

public class FriendLocationService extends Service implements IAppManager{

	private String TAG="FriendLocationSErvice";
	
	public static final String TAKE_MESSAGE = "Take_Message";
	public static final String FRIEND_LIST_UPDATED = "Take Friend List";
	public static final String FRIEND_REQ_LIST_UPDATED = "Take Friend Request List";
	public static final String MESSAGE_LIST_UPDATED = "Take Message List";
	public ConnectivityManager conManager = null;
	private final int UPDATE_TIME_PERIOD = 15000;
	
	private final IBinder mBinder = new IMBinder();
	ISocketOperator socketOperator = new SocketOperator(this);
	
	private String lattitude="0";
	private String longitude="0";
	
	JSONObject appData;
	JSONArray friendsReq;
	boolean friendsReqUpdated=false;
	boolean updateLocation=true;
	JSONArray userData;
	JSONArray friends;
	JSONArray messages;
	
	private String username;
	 public static String logUserId;
	 JSONObject userJsonObj;
	 
	 private Handler handler;
	//private String userId = new String();
	//private String userDisplayName;
	private String password;
	private boolean authenticatedUser = false;
	
	 // timer to take the updated data from server
	private Timer timer;
	
	
	public class IMBinder extends Binder {
		public IAppManager getService() {
			return FriendLocationService.this;
		}
		
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		handler = new Handler();
		// Timer is used to take the friendList info every UPDATE_TIME_PERIOD;
				timer = new Timer();
				
				Thread thread = new Thread()
				{
					@Override
					public void run() {			
						
						//socketOperator.startListening(LISTENING_PORT_NO);
						Random random = new Random();
						int tryCount = 0;
						while (socketOperator.startListening(10000 + random.nextInt(20000))  == 0 )
						{		
							tryCount++; 
							if (tryCount > 10)
							{
								// if it can't listen a port after trying 10 times, give up...
								break;
							}
							
						}
					}
				};		
				thread.start();
	}
	
	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return this.username;
	}

	
	
	@Override
	public String sendMessage(String userId, String touserId, String message)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		
		String params = "username="+ URLEncoder.encode(this.username,"UTF-8") +
				"&password="+ URLEncoder.encode(this.password,"UTF-8") +
				"&to=" + URLEncoder.encode(touserId,"UTF-8") +
				"&message="+ URLEncoder.encode(message,"UTF-8") +
				"&action="  + URLEncoder.encode("sendMessage","UTF-8")+
				"&";		
				
		Log.i("PARAMS", params);
		String rStr = null;
		try {
			
			JSONObject result = socketOperator.sendHttpRequestJson(params);		
			
				if(result.getInt("success") == 1){
					rStr="success";
				}
			
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		Log.e("SERVICE 151"," "+e.toString());
	}	

				
		return rStr;
	}
	
	
	private JSONObject getUserId() throws UnsupportedEncodingException 	{		
		// after authentication, server replies with friendList JsonObject
		JSONObject json =null;
		try {
		FriendLocationService.this.getlocation();	
		sendLocationInfo(this.username ,	this.password, lattitude, longitude);
	    json = socketOperator.sendHttpRequestJson(getAuthenticateUserParams(username, password));
	    appData = json;
	    Log.e(TAG, " "+json.toString());
	    int success, success_us, success_fr, success_msg, success_frq;
		
			success = json.getInt("success");
			success_fr = json.getInt("fr_success");
			success_msg = json.getInt("msg_success");
			success_us = json.getInt("us_success");
			success_frq = json.getInt("freq_success");
			if(success==1){
				
			if(success_fr==1){
				friends = appData.getJSONArray("Friends");
				FriendsController.setFriendsInfo(friends);
			}else{
				friends = null;
				FriendsController.setFriendsInfo(friends);
			}
			if(success_frq==1)
			{
				if(friendsReq ==null){
					friendsReqUpdated = true;
					friendsReq = appData.getJSONArray("FriendsReq");
					FriendsController.setFriendsReqInfo(friendsReq);				
				}else if (friendsReq.length() != appData.getJSONArray("FriendsReq").length()){
					friendsReqUpdated = true;
					friendsReq = appData.getJSONArray("FriendsReq");
					FriendsController.setFriendsReqInfo(friendsReq);
				}else{friendsReqUpdated=false;}
				
			}else{
				friendsReqUpdated = true;
				friendsReq = null;
				FriendsController.setFriendsReqInfo(friendsReq);
			}
			if(success_msg==1){
				messages = appData.getJSONArray("Messages");
				MessageController.setMessagesInfo(messages);
			}else{
				messages = null;
				MessageController.setMessagesInfo(messages);
			}
			if(success_us == 1){ 
				userData = appData.getJSONArray("Users"); 
				FriendsController.setUserInfo(userData); 
				FriendLocationService.logUserId = userData.getJSONObject(0).getString("id"); 
				
				
			}
		}else{json=null;}
			
			String r=String.valueOf(success);
			Log.v("success",r); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("SERVICE 193 "," "+e.toString());
			json=null;
		}
			
		 return json;
	}

	

	@Override
	public String authenticateUser(String usernameText, String passwordText)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		this.username = usernameText;
		this.password = passwordText;	
		
		this.authenticatedUser = false;
		JSONObject jOb=this.getUserId();
		String userId = null;
		
		//Log.e(TAG, " "+userId);
		try {
			if (jOb != null && jOb.getInt("success")==1) 
			{			
				// if user is authenticated then return string from server is not equal to AUTHENTICATION_FAILED
				this.authenticatedUser = true;
				userId="1";
				timer.schedule(new TimerTask()
				{			
					public void run() 
					{
						try {
							
							
							int success, success_us, success_frq, success_fr, success_msg;
							
							JSONObject appData = FriendLocationService.this.getUserId();
							
							success = appData.getInt("success");
							success_fr = appData.getInt("fr_success");
							success_frq = appData.getInt("freq_success");
							success_msg = appData.getInt("msg_success");
							success_us = appData.getInt("us_success");
							if(success==1){
							if(success_fr==1){
								friends = appData.getJSONArray("Friends");
								FriendsController.setFriendsInfo(friends);
								
								Intent i = new Intent(FRIEND_LIST_UPDATED);
								i.putExtra(Common.FRIEND_LIST,friends.toString());
								sendBroadcast(i);	
								Log.i("friend list broadcast sent ", "");
							}
							if(success_frq==1 && friendsReqUpdated){
								friendsReq = appData.getJSONArray("FriendsReq");
								FriendsController.setFriendsReqInfo(friendsReq);
								
								Intent i = new Intent(FRIEND_REQ_LIST_UPDATED);
								i.putExtra(Common.FRIEND_REQ_LIST,friendsReq.toString());
								sendBroadcast(i);	
								Log.i("friend list broadcast sent ", "");
							}
							if(success_msg==1){
								messages = appData.getJSONArray("Messages");
								MessageController.setMessagesInfo(messages);
								
								Intent i = new Intent(MESSAGE_LIST_UPDATED);
								i.putExtra(Common.MESSAGE_LIST,messages.toString());
								sendBroadcast(i);	
								Log.i("message list broadcast sent ", "");
							}
							if(success_us == 1)
							{
								userData = appData.getJSONArray("Users"); 
								FriendsController.setUserInfo(userData); 
								logUserId = userData.getJSONObject(0).getString("id");
							}
																				
						}else {
								Log.i("friend list returned null", "");
							}
						}
						catch (Exception e) {
							Log.e("SERVICE 289", " "+e.toString());
						}					
					}			
				}, UPDATE_TIME_PERIOD, UPDATE_TIME_PERIOD);
					
				
			//	setAuthenticatedUserData(userId);			
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return userId;	
	}

	@Override
	public boolean isNetworkConnected() {
		// TODO Auto-generated method stub
		return conManager.getActiveNetworkInfo().isConnected();
	}

	@Override
	public boolean isUserAuthenticated() {
		// TODO Auto-generated method stub
		return authenticatedUser;
	}
	
	private String getAuthenticateUserParams(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{			
		String params = "username=" + URLEncoder.encode(usernameText,"UTF-8") +
						"&password="+ URLEncoder.encode(passwordText,"UTF-8") +
						"&lattitude="+ URLEncoder.encode(lattitude,"UTF-8") +
						"&longitude="+ URLEncoder.encode(longitude,"UTF-8") +
						"&action="  + URLEncoder.encode("authenticateUser","UTF-8")+
						"&port="    + URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&";		
		
		return params;		
	}

	public void sendLocationInfo(String username, String password, String lat, String longi){
		String params = "username=" + username +
				"&password=" + password +
				"&lattitude=" + lat +
				"&longitude=" + longi +
				"&action=" + "updateLocation"+
				"&";
	
	socketOperator.sendHttpRequestJson(params);		
	
	
		
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("IMService is being destroyed", "...");
		super.onDestroy();
	}
	@Override
	public void exit() {
		// TODO Auto-generated method stub
		timer.cancel();
		socketOperator.exit(); 
		socketOperator = null;
		this.stopSelf();
	}

	@Override
	public String signUpUser(String displaynameText, String usernameText,
			String passwordText, String email, String img) {
		// TODO Auto-generated method stub
		String rStr = "0:";
		try {
		Log.e(TAG, " "+displaynameText);
		Log.e(TAG, " "+usernameText);
		Log.e(TAG, " "+passwordText);
		Log.e(TAG, " "+email);
		String params = "userdname=" + displaynameText +
				"&username=" + usernameText +
				"&password=" + passwordText +
				"&action=" + "signUpUser"+
				"&email=" + email+
				"&img=" + img+
				"&";
		
		JSONObject result = socketOperator.sendHttpRequestJson(params);
		int success;
		success = result.getInt("success");

		if(success==1){
			int id = result.getInt("id");
			String cStr = String.valueOf(id);
			rStr = "1:"+cStr;
		}else{
			String cStr = String.valueOf(success);
			rStr = cStr+":";
		}
		return rStr;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rStr;

		
	}
	
	@Override
	public String updateImage(String id, String img) {
		// TODO Auto-generated method stub
		/*String params = "id=" + id +
				"&action=" + "setUserPic"+
				"&img=" + img+
				"&";*/
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id",id));
        params.add(new BasicNameValuePair("action","setUserPic"));
        params.add(new BasicNameValuePair("img",img));
        socketOperator.makeHttpRequest(params);

       String result =	socketOperator.makeHttpRequest(params);		
return result;
				
	}
	
	@Override
	public JSONObject getNonFriendsList() {
		// TODO Auto-generated method stub
		String params ="id=" + FriendLocationService.logUserId +
				"&action=" + "getNonFriendList" +
				"&";
		Log.e(TAG+" NON FRIENDLIST"," "+FriendLocationService.logUserId);
				JSONObject result = socketOperator.sendHttpRequestJson(params);
				Log.e(TAG+" NON FRIENDLIST"," "+result.toString());
				return result;
	}

	@Override
	public void addNewFriendRequest(String friendUserId) {
		// TODO Auto-generated method stub
		String params = "id=" + FriendLocationService.logUserId +
				"&action=" + "addNewFriend" +
				"&friendid=" + friendUserId +
				"&";
		try {
				JSONObject result = socketOperator.sendHttpRequestJson(params);
				if(result.getInt("success") == 1){
					
						this.authenticateUser(this.username, this.password);
					
				}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e("SERVICE 439"," "+e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("SERVICE 422"," "+e.toString());
		}	
				
	}

	@Override
	public void sendFriendsReqsResponse(String friendId,
			String responseString) {
		// TODO Auto-generated method stub
		
		String params = "friendid=" + friendId +
				"&id=" + FriendLocationService.logUserId +
				"&requestResponse=" + responseString +
				"&action=" + "requestResponse"+
				"&";
		try {
				JSONObject result = socketOperator.sendHttpRequestJson(params);		
				
					if(result.getInt("success") == 1){
						
						this.authenticateUser(this.username, this.password);
					
					}
				
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Log.e("SERVICE 470"," "+e.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("SERVICE 473"," "+e.toString());
		}	
		
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void getlocation()
	{
		
		 // Do something that takes a while
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() { // This thread runs in the UI
                    @Override
                    public void run() {
                    /*	LocationManager lm = (LocationManager)FriendLocationService.this.getSystemService(Context.LOCATION_SERVICE);
                		
               		 Criteria criteria = new Criteria();
               		 String best = lm.getBestProvider(criteria, true);*/
               		//Location location = lm.getLastKnownLocation(best);
               		Location location = getLocation();
               		 if(location!=null){
               		 String lat1=String.valueOf(location.getLatitude());
               		 lattitude = lat1;
               		 Log.e("LAATTITUDE CRITERIA", " "+lat1);
               		
               		 String lng1=String.valueOf(location.getLongitude());
               		 longitude = lng1;
               		Log.e("LONGITUDE CRITERIA", " "+lat1);
               		 }               
                    }
                });
            }
        };
        new Thread(runnable).start();
    }

	public Location getLocation() {
		Location location=null;
	    try {
	    	LocationManager locationManager;
	    	
	    	final long TWO_MINUTES = 1000 * 60 * 2;
	    	locationManager = (LocationManager)FriendLocationService.this
	                .getSystemService(LOCATION_SERVICE);

	        // getting GPS status
	        boolean isGPSEnabled = locationManager
	                .isProviderEnabled(LocationManager.GPS_PROVIDER);

	        // getting network status
	        boolean isNetworkEnabled = locationManager
	                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	        if (!isGPSEnabled && !isNetworkEnabled) {
	            // no network provider is enabled
	        } else {
	            MyLocationListener mListner =new MyLocationListener();;
	            if (isNetworkEnabled) {
	                locationManager.requestLocationUpdates(
	                        LocationManager.NETWORK_PROVIDER,
	                        TWO_MINUTES,
	                        10.2f, mListner);
	                Log.d("Network", "Network Enabled");
	                if (locationManager != null) {
	                    location = locationManager
	                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                    
	                }
	            }
	            // if GPS Enabled get lat/long using GPS Services
	            if (isGPSEnabled) {
	                if (location == null) {
	                    locationManager.requestLocationUpdates(
	                            LocationManager.GPS_PROVIDER,
	                            TWO_MINUTES,
	                            12.0f, mListner);
	                    Log.d("GPS", "GPS Enabled");
	                    if (locationManager != null) {
	                        location = locationManager
	                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                        
	                    }
	                }
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return location;
	}
	private class MyLocationListener implements LocationListener 
	{
	    @Override
	    public void onLocationChanged(Location loc) {
	        if (loc != null) {
	            
	            handler.sendEmptyMessage(0);
	        }
	    }

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}  

	//...
	} 
}
