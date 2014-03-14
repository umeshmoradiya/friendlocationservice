package com.haraye.friendlocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.haraye.friendlocation.adapters.FriendListAdapter;
import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.service.FriendLocationService;
import com.haraye.friendlocation.tools.FriendsController;



public class FriendList extends ListActivity 
{
	private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 1;
	private IAppManager imService = null;
	private FriendListAdapter friendAdapter;
	
	private String friendObject;
	public String ownusername = new String();
	Button freq, onMap;
	
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_friends);
        
		friendAdapter = new FriendListAdapter(this);
		
		freq = (Button)findViewById(R.id.friendreq);
		freq.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(FriendList.this, FriendRequestList.class);																
				startActivity(i);	
			}
		});
		
		onMap = (Button)findViewById(R.id.onmap);
		onMap.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(FriendList.this, MainActivity.class);																
				i.putExtra("friendsObject", friendObject);
				startActivity(i);	
			}
		});
		
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);		
		try {
		
		
		Intent i = new Intent(this, Messaging.class);
		JSONObject friend = friendAdapter.getItem(position);
		i.putExtra("friendObject", friend.toString());
		startActivity(i);
		
		Log.e("Friend Id"," "+friend.getString("id"));
		Log.e("Friend Display Name"," "+friend.getString("displayname"));
		Log.e("Friend Email"," "+friend.getString("email"));
		Log.e("Friend Status"," "+friend.getString("status"));
		Log.e("Friend Lattitude"," "+friend.getString("lattitude"));
		Log.e("Friend Longitude"," "+friend.getString("longitude"));
		Log.e("Friend Image"," "+friend.getString("imgstr"));
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Intent i = new Intent(this, Messaging.class);
		FriendInfo friend = friendAdapter.getItem(position);
		i.putExtra(FriendInfo.USERNAME, friend.userName);
		i.putExtra(FriendInfo.PORT, friend.port);
		i.putExtra(FriendInfo.IP, friend.ip);		
		startActivity(i);*/
	}




	@Override
	protected void onPause() 
	{
		unregisterReceiver(messageReceiver);		
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
			
		super.onResume();
		bindService(new Intent(FriendList.this, FriendLocationService.class), mConnection , Context.BIND_AUTO_CREATE);

		IntentFilter i = new IntentFilter();
		//i.addAction(IMService.TAKE_MESSAGE);	
		i.addAction(FriendLocationService.FRIEND_LIST_UPDATED);

		registerReceiver(messageReceiver, i);			
		

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		

		menu.add(0, ADD_NEW_FRIEND_ID, 0, R.string.add_new_friend);
		
		menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);		
		
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	  
			case ADD_NEW_FRIEND_ID:
			{
				Intent i = new Intent(FriendList.this, SearchFriend.class);
				startActivity(i);
				return true;
			}		
			case EXIT_APP_ID:
			{
				imService.exit();
				finish();
				return true;
			}			
		}

		return super.onMenuItemSelected(featureId, item);		
	}	
	
	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("Broadcast receiver ", "received a message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();
				if (action.equals(FriendLocationService.FRIEND_LIST_UPDATED))
				{
					// taking friend List from broadcast
					String rawFriendList = extra.getString(Common.FRIEND_LIST);
					Log.e("TAG - Friend List", " "+ rawFriendList);
					//FriendList.this.parseFriendInfo(rawFriendList);
					FriendList.this.updateData(rawFriendList, rawFriendList);
					
				}
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((FriendLocationService.IMBinder)service).getService();      
			
			JSONArray friends = FriendsController.getFriendsInfo(); //imService.getLastRawFriendList();
			if (friends!=null) {    			
				FriendList.this.updateData(friends.toString(), null); // parseFriendInfo(friendList);
			}else{
				FriendList.this.updateData(null, null);
			}
			
			
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(FriendList.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	public void updateData(String friends, String unApprovedFriends)
	{
		if (friends != null) {
			try {
				friendObject=friends;
				friendAdapter.setFriendList(new JSONArray(friends));
				setListAdapter(friendAdapter);	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
						
		}else{
			setListAdapter(null);
		}				
		
		
	}



	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
	
		
		
	}
}
