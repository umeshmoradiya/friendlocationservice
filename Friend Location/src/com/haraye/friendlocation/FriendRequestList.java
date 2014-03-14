package com.haraye.friendlocation;

import org.json.JSONArray;
import org.json.JSONException;

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

import com.haraye.friendlocation.adapters.FriendReqListAdapter;
import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.service.FriendLocationService;
import com.haraye.friendlocation.tools.FriendsController;



public class FriendRequestList extends ListActivity 
{
	private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 1;
	private IAppManager imService = null;
	private FriendReqListAdapter friendAdapter;
	
	public String ownusername = new String();
	Button freqApprove, freqDiscard;
	String idds;
	
	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.screen_friends_request);
        freqApprove = (Button)findViewById(R.id.approved);
        freqDiscard = (Button)findViewById(R.id.rejected);
        
        
        friendAdapter = new FriendReqListAdapter(this);
        
        freqApprove.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// String Ids =	FriendReqListAdapter.checkedIds.toString();
				 idds=null;
				 for(int i =0; i<FriendReqListAdapter.checkedIds.size(); i++){
					 if(idds==null){
					 idds = FriendReqListAdapter.checkedIds.get(i).toString();
					 }else{
						 idds = idds+","+FriendReqListAdapter.checkedIds.get(i).toString();
						 
					 }
				 }
				 if(idds!=null){
					 Thread thread = new Thread() {
			                @Override
			                public void run() {
					 imService.sendFriendsReqsResponse(idds, Common.FRIEND_APPROVE_STATUS);
					// finish();
				//	 Log.e("Friend Request Response 1", " "+response);
			                }
					 };
					 thread.start();
			                
			      }
				 Log.e("Checked Ids ", " "+idds );
			}
		});
        
        freqDiscard.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
			//	String Ids =	FriendReqListAdapter.checkedIds.toString();
				 idds=null;
				 for(int i =0; i<FriendReqListAdapter.checkedIds.size(); i++){
					 if(idds==null){
					 idds = FriendReqListAdapter.checkedIds.get(i).toString();
					 }else{
						 idds = idds+","+FriendReqListAdapter.checkedIds.get(i).toString();
					 }
				 }
				 if(idds!=null){
					 Thread thread = new Thread() {
			                @Override
			                public void run() {
					 imService.sendFriendsReqsResponse(idds, Common.FRIEND_IGNORE_STATUS);
			                }
					 };
					 thread.start();
			                
			     }
				 Log.e("Checked Ids ", " "+idds );
			}
		});
		
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);		

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
		bindService(new Intent(FriendRequestList.this, FriendLocationService.class), mConnection , Context.BIND_AUTO_CREATE);

		IntentFilter i = new IntentFilter();
		//i.addAction(IMService.TAKE_MESSAGE);	
		i.addAction(FriendLocationService.FRIEND_REQ_LIST_UPDATED);

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
				Intent i = new Intent(FriendRequestList.this, SearchFriend.class);
				startActivity(i);
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
				if (action.equals(FriendLocationService.FRIEND_REQ_LIST_UPDATED))
				{
					// taking friend List from broadcast
					String rawFriendList = extra.getString(Common.FRIEND_REQ_LIST);
					Log.e("TAG - Friend List", " "+ rawFriendList);
					//FriendList.this.parseFriendInfo(rawFriendList);
					FriendRequestList.this.updateData(rawFriendList, rawFriendList);
					
				}
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((FriendLocationService.IMBinder)service).getService();      
			
			JSONArray friends = FriendsController.getFriendsReqInfo(); //imService.getLastRawFriendList();
			if (friends!=null) {    			
				FriendRequestList.this.updateData(friends.toString(), null); // parseFriendInfo(friendList);
			}else{
				FriendRequestList.this.updateData(null, null);
			}
			
			
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(FriendRequestList.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	public void updateData(String friends, String unApprovedFriends)
	{
		if (friends != null) {
			try {
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
