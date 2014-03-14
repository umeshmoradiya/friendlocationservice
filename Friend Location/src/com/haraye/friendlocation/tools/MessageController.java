package com.haraye.friendlocation.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/*
 * This class can store friendInfo and check userkey and username combination 
 * according to its stored data
 */
public class MessageController 
{
	
	private static JSONArray messagesInfo = null;
	
	public static void setMessagesInfo(JSONArray messageInfo)
	{
		MessageController.messagesInfo = messageInfo;
	}
	
	
	
	public static JSONObject getMessageInfo(String id) 
	{
		JSONObject result = null;
		if (messagesInfo != null) 
		{
			for (int i = 0; i < messagesInfo.length(); i++) 
			{
				try {
					if ( messagesInfo.getJSONObject(i).getString("id").equals(id) )
					{
						result = messagesInfo.getJSONObject(i);
						break;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}			
		}		
		return result;
	}






	public static JSONArray getMessagesInfo() {
		return messagesInfo;
	}



	
	
	

}
