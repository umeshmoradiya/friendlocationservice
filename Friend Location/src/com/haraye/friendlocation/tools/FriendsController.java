package com.haraye.friendlocation.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FriendsController {

	private static JSONArray friendsInfo = null;
	private static JSONArray userInfo = null;
	private static JSONArray friendsReqInfo = null;
	public static void setFriendsInfo(JSONArray friendInfo)
	{
		FriendsController.friendsInfo = friendInfo;
	}
	
	public static JSONArray getFriendsInfo() {
		return friendsInfo;
	}
	
	public static void setFriendsReqInfo(JSONArray friendReqInfo)
	{
		FriendsController.friendsReqInfo = friendReqInfo;
	}
	

	public static JSONArray getFriendsReqInfo() {
		return friendsReqInfo;
	}
	
	public static void setUserInfo(JSONArray userInfo)
	{
		FriendsController.userInfo = userInfo;
	}
	
	public static JSONArray getUserInfo() {
		return userInfo;
	}
	
	
	
	/*public static FriendInfo checkFriend(String username, String userKey)
	{
		FriendInfo result = null;
		if (friendsInfo != null) 
		{
			for (int i = 0; i < friendsInfo.length; i++) 
			{
				if ( friendsInfo[i].userName.equals(username) && 
					 friendsInfo[i].userKey.equals(userKey)
					)
				{
					result = friendsInfo[i];
					break;
				}				
			}			
		}		
		return result;
	}*/
	
	public static void setActiveFriendId(String friendId){
	}
	
	/*public static String getActiveFriend()
	{
		return activeFriend;
	}
*/


	public static JSONObject getFriendInfo(String id) 
	{
		JSONObject result = null;
		if (friendsInfo != null) 
		{
			for (int i = 0; i < friendsInfo.length(); i++) 
			{
				try {
					if ( friendsInfo.getJSONObject(i).getString("id").equals(id) )
					{
						result = friendsInfo.getJSONObject(i);
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



	/*public static void setUnapprovedFriendsInfo(FriendInfo[] unapprovedFriends) {
		unapprovedFriendsInfo = unapprovedFriends;		
	}*/





	/*public static FriendInfo[] getUnapprovedFriendsInfo() {
		return unapprovedFriendsInfo;
	}*/
}
