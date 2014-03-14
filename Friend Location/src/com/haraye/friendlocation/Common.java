package com.haraye.friendlocation;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class Common {

	public static String APPLICATION_NAME ="Friend Location";
	public static String IN_MOBI_PROPERTY_ID ="9bb0fb6b3cb044ab8a47eb8b2e8a6418";
	public static String AD_MOB_GOOGLE_ID ="fine-program-456";
	
	public static final String FRIEND_LIST = "friendList";
	public static final String FRIEND_REQ_LIST = "friendReqList";
	
	public static final String FRIEND_IGNORE_STATUS = "ignored";
	public static final String FRIEND_APPROVE_STATUS = "approved";
	
	public static final String MESSAGE_LIST = "messageList";
	public static final String MESSAGE_OBJECT = "messageObject";
	public static final String MESSAGE_FROM_ID = "fid";
	public static final String MESSAGE_FROM_NAME = "displayname";
	public static final String SENDT = "sendate";
	public static final String MESSAGETEXT = "message";
	
	public static void fullScreenMode(Activity ctx){
		ctx.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    ctx.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	public static String jsonToString(JSONObject jObj){
		String jStr;
		jStr = jObj.toString();
		return jStr;
	}
	
	public static JSONObject stringToJObj(String jStr){
		JSONObject jObj = null;
		try {
			jObj = new JSONObject(jStr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("Common 48"," "+e.toString());
		}
		return jObj;
	}
	
	public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
        return false;
        } else {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }   
	
}
