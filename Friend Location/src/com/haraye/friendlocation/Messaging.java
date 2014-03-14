package com.haraye.friendlocation;


import java.io.UnsupportedEncodingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.service.FriendLocationService;
import com.haraye.friendlocation.tools.FriendsController;
import com.haraye.friendlocation.tools.LocalStorageHandler;



public class Messaging extends Activity {

	private static final int MESSAGE_CANNOT_BE_SENT = 0;
	public String username;
	private EditText messageText;
	private EditText messageHistoryText;
	private Button sendMessageButton;
	private IAppManager imService;
	private LocalStorageHandler localstoragehandler; 
	private Cursor dbCursor;
	private ImageView locateOnMap;
	private String ID = "id";
	private String FRIEND_OBJECT = "friendObject";
	private String friendObject;
	private String DISPLAYNAME = "displayname";
	JSONObject friendJson = new JSONObject();
	JSONObject userJson;
	
	private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((FriendLocationService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(Messaging.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.messaging_screen); //messaging_screen);
				
		messageHistoryText = (EditText) findViewById(R.id.messageHistory);
		
		messageText = (EditText) findViewById(R.id.message);
		
		locateOnMap = (ImageView)findViewById(R.id.location);
		
		messageText.requestFocus();			
		
		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		
		Bundle extras = this.getIntent().getExtras();
		
		try {
			//friendObject = extras.getString(FRIEND_OBJECT);
			friendJson = new JSONObject(extras.getString(FRIEND_OBJECT));
			JSONArray jArrayObject = new JSONArray();
			friendObject = jArrayObject.put(friendJson).toString();
			userJson = FriendsController.getUserInfo().getJSONObject(0);
			String msg = extras.getString("message");
			
		 
			Log.e("Messaging friendJson", " "+ friendJson.getString(ID));
			Log.e("Messaging friendJson11", " "+ userJson.getString("id"));
		
		
		setTitle("Messaging with " + friendJson.getString(DISPLAYNAME));
		
		
	//	EditText friendUserName = (EditText) findViewById(R.id.friendUserName);
	//	friendUserName.setText(friend.userName);
		
		
		localstoragehandler = new LocalStorageHandler(this);
		dbCursor = localstoragehandler.get(friendJson.getString(ID), userJson.getString("id"));
		
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
		dbCursor.moveToFirst();
		    while ((!dbCursor.isAfterLast())&&noOfScorer<dbCursor.getCount()) 
		    {
		        noOfScorer++;

				this.appendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(5));
		        dbCursor.moveToNext();
		    }
		}
		localstoragehandler.close();
		
		if (msg != null) 
		{
			
			
				this.appendToMessageHistory(friendJson.getString(DISPLAYNAME) , msg);
				((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friendJson.getString(ID)+msg).hashCode());
			
		}
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		
		locateOnMap.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Messaging.this, MainActivity.class);
				i.putExtra("friendsObject", friendObject);				
				startActivity(i);
				
			}
		});
		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			CharSequence message;
			Handler handler = new Handler();
			public void onClick(View arg0) {
				message = messageText.getText();
				if (message.length()>0) 
				{		
					try {
					appendToMessageHistory(userJson.getString("displayname"), message.toString());
					
					//localstoragehandler.insert(imService.getUsername(), friend.userName, message.toString());
					
						localstoragehandler.insert(userJson.getString("displayname"), userJson.getString("id"),friendJson.getString(DISPLAYNAME),friendJson.getString(ID), message.toString());
					
								
					messageText.setText("");
					Thread thread = new Thread(){					
						public void run() {
							try {
								if (imService.sendMessage(userJson.getString("id"), friendJson.getString(ID), message.toString()) == null)
								{
									
									handler.post(new Runnable(){	

										public void run() {
											
									        Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

											
											//showDialog(MESSAGE_CANNOT_BE_SENT);										
										}
										
									});
								}
							} catch (UnsupportedEncodingException e) {
								Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

								e.printStackTrace();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}						
					};
					thread.start();
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}			
				}
				
			}});
		
		messageText.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if (keyCode == 66){
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
			
			
		});
				
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		int message = -1;
		switch (id)
		{
		case MESSAGE_CANNOT_BE_SENT:
			message = R.string.message_cannot_be_sent;
		break;
		}
		
		if (message == -1)
		{
			return null;
		}
		else
		{
			return new AlertDialog.Builder(Messaging.this)       
			.setMessage(message)
			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked OK so do some stuff */
				}
			})        
			.create();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		
		FriendsController.setActiveFriendId(null);
		
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(Messaging.this, FriendLocationService.class), mConnection , Context.BIND_AUTO_CREATE);
				
		IntentFilter i = new IntentFilter();
		i.addAction(FriendLocationService.MESSAGE_LIST_UPDATED);
		
		registerReceiver(messageReceiver, i);
		
		try {
			FriendsController.setActiveFriendId(friendJson.getString(ID));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
	}
	
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		
			Bundle extra = intent.getExtras();
			try {
			String messageObject = extra.getString(Common.MESSAGE_LIST);
			JSONArray messageJSObj = new JSONArray(messageObject);
			
			String senderId = messageJSObj.getJSONObject(0).getString(Common.MESSAGE_FROM_ID);
			String senderName = messageJSObj.getJSONObject(0).getString(Common.MESSAGE_FROM_NAME);
			String receiverId = extra.getString(userJson.getString("id"));
			String receiverName = extra.getString(userJson.getString("displayname"));
					
			String message = messageJSObj.getJSONObject(0).getString(Common.MESSAGETEXT);
			
			
			if (senderId != null && message != null)
			{
				if (friendJson.getString(ID).equals(senderId)) {
					appendToMessageHistory(senderName, message);
					localstoragehandler.insert(senderId,senderName,receiverId,receiverName, message);
					
				}
				else {
					if (message.length() > 15) {
						message = message.substring(0, 15);
					}
					Toast.makeText(Messaging.this,  senderName + " says '"+
													message + "'",
													Toast.LENGTH_SHORT).show();		
				}
			}			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	};
	private MessageReceiver messageReceiver = new MessageReceiver();
	
	public  void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
			
			Log.e("Message Receiver 1"," "+username);
			Log.e("Message Receiver 2"," "+message);
			messageHistoryText.append(username + ":\n");								
			messageHistoryText.append(message + "\n");
		}
	}
	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if (localstoragehandler != null) {
	    	localstoragehandler.close();
	    }
	    if (dbCursor != null) {
	    	dbCursor.close();
	    }
	}
	

}
