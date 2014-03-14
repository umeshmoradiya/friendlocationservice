package com.haraye.friendlocation;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.haraye.friendlocation.adapters.FriendSearchAdapter;
import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.service.FriendLocationService;
import com.haraye.friendlocation.service.SocketOperator;


public class SearchFriend extends Activity {
    // Declare Variables
    JSONObject jsonobject;
    JSONArray jsonarray;
    ListView listview;
    FriendSearchAdapter adapter;
    ProgressDialog mProgressDialog;
    ArrayList<HashMap<String, String>> arraylist;
    static String ID = "id";
    static String NAME = "displayname";
    static String PIC = "imgstr";
    private static final String LOG_TAG = "AddFriend";
    
    private IAppManager imService;
    EditText mEditText;
   
   private ServiceConnection mConnection = new ServiceConnection() {
      
		
		
		public void onServiceConnected(ComponentName className, IBinder service) {          
            imService = ((FriendLocationService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
        	imService = null;
            Toast.makeText(SearchFriend.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from listview_main.xml
        setContentView(R.layout.add_new_friend);
        
        // Locate the listview in listview_main.xml
        listview = (ListView) findViewById(R.id.listsearch);
        mEditText = (EditText) findViewById(R.id.newFriendUsername);
        // Execute DownloadJSON AsyncTask
        if(imService!=null){
        	Toast.makeText(getApplicationContext(), "SERVICE NOT NULL", 5000).show();
        }
        new DownloadJSON().execute();
       
        
        listview.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
					long arg3) {
				// TODO Auto-generated method stub
			
				Log.e("ITEM CLICK ID", " "+ adapter.getItem(position).toString());
				HashMap<String, Object> obj = (HashMap<String, Object>) adapter.getItem(position);
	           showAlertDialog(SearchFriend.this, obj, position);
				
				//String friendId = (String) obj.get("id");
	            
	           // imService.addNewFriendRequest(friendId);
	           // Log.e("ITEM CLICK ID", " "+ friendId.toString());
	            
			}
		});

        mEditText = (EditText) findViewById(R.id.newFriendUsername);
        mEditText.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {

            }

            public void beforeTextChanged(CharSequence s, int start,int count, int after)
            {

            }

            public void onTextChanged(CharSequence s, int start,int before, int count)
            {

                ArrayList<HashMap<String, String>> arrayTemplist= new ArrayList<HashMap<String,String>>();
                String searchString =mEditText.getText().toString();
                if(searchString.length()>2){
                for (int i = 0; i < arraylist.size(); i++)
                {
                    String currentString =arraylist.get(i).get(SearchFriend.NAME).toString();
                    Log.e("current Str"," "+currentString);
                    if (currentString.contains(searchString))
                    {
                        arrayTemplist.add(arraylist.get(i));
                    }
                }
                }
                adapter = new FriendSearchAdapter(SearchFriend.this, arrayTemplist);
                listview.setAdapter(adapter);
             }
            });
        }
    

    // DownloadJSON AsyncTask
    class DownloadJSON extends AsyncTask<Void, Void, ArrayList<HashMap<String, String>> > {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(SearchFriend.this);
            // Set progressdialog title
            //mProgressDialog.setTitle("Fetching the information");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected ArrayList<HashMap<String, String>>  doInBackground(Void... params) {
            // Create an array
        	 try {
        	arraylist = new ArrayList<HashMap<String, String>>();
            // Retrieve JSON Objects from the given URL address
        //   if(imService!=null){
		//	jsonobject = imService.getNonFriendsList();
        	String param ="id=" + FriendLocationService.logUserId +
    				"&action=" + "getNonFriendList" +
    				"&";
			 jsonobject = SocketOperator.makeHttpRequestJson(param);
			 Log.e(LOG_TAG, " "+ jsonobject.toString() );
            //jsonobject = imService.getNonFriendsList();
			
				if(jsonobject.getInt("nf_success")==1){
		

           
                // Locate the array name in JSON
                jsonarray = jsonobject.getJSONArray("NonFriends");

                for (int i = 0; i < jsonarray.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    jsonobject = jsonarray.getJSONObject(i);
                    // Retrive JSON Objects
                    map.put(SearchFriend.NAME, jsonobject.getString("displayname"));
                    map.put(SearchFriend.ID, jsonobject.getString("id"));
                    map.put(SearchFriend.PIC, jsonobject.getString("imgstr"));



                    // Set the JSON Objects into the array
                    arraylist.add(map);
                }
                return arraylist;
				}else{
					return null;
				}
          // }
            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        	 return null;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>>  args) {
            // Pass the results into ListViewAdapter.java
        	if(args==null){
        		listview.setAdapter(null);
                	
        	}
        	else{
            adapter = new FriendSearchAdapter(SearchFriend.this, arraylist);
            // Set the adapter to the ListView
            listview.setAdapter(adapter);
            // Close the progressdialog
            }
        	mProgressDialog.dismiss();
        	
        }
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		
		unbindService(mConnection);
				
	}

	@Override
	protected void onResume() 
	{		
		super.onResume();
		bindService(new Intent(SearchFriend.this, FriendLocationService.class), mConnection , Context.BIND_AUTO_CREATE);
	}
	
	protected void onDestroy() {
		super.onDestroy();
	};
    
    AlertDialog.Builder alertDialog;
	/*
	 * Display Alert dialog for download video 
	 * @param ctx which is context of class
	 * @param url which is url from where video downloaded
	 * @param filename which is name of file for download
	 * @downloadtype which is integer value that indicate the download method
	 * */
	public void showAlertDialog(final Context ctx, final HashMap<String, Object> obj, final int position){

		alertDialog = new AlertDialog.Builder(ctx);

		// Setting Dialog Title
		alertDialog.setTitle("Friend Request...!!!");
		
		String friendName = (String) obj.get("displayname");
	//	boolean yesNo;
		// Setting Dialog Message
		alertDialog.setMessage("Send Friend Request to "+ friendName);

		// Setting Icon to Dialog
		alertDialog.setIcon(R.drawable.ic_launcher);

		// Setting Positive "Yes" Btn
		alertDialog.setPositiveButton("YES",

				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				// Write your code here to execute after dialog
					Thread thread = new Thread() {
                @Override
                public void run() {
						String friendId = (String) obj.get("id");
	            
						imService.addNewFriendRequest(friendId);
						
					    }
			            };
			            thread.start();
			            adapter.removeItem(position);
			}
		});
		// Setting Negative "NO" Btn
		alertDialog.setNegativeButton("NO",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// Write your code here to execute after dialog

				dialog.cancel();
				
			}
		});

		

		// Showing Alert Dialog
		alertDialog.show();

	}
}