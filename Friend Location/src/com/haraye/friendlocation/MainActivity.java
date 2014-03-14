package com.haraye.friendlocation;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.haraye.friendlocation.interfaces.IAppManager;
import com.haraye.friendlocation.service.FriendLocationService;
import com.haraye.friendlocation.service.SocketOperator;


//http://androidmapv2.blogspot.in/
//http://androidadvertise.blogspot.de/2013/10/how-to-implement-in-mobi-ads-in-android.html
public class MainActivity extends Activity implements LocationListener, LocationSource{

	private Context mCtx = MainActivity.this;
//	private String TAG = "MainActivity";
	 // Google Map
    private GoogleMap googleMap;
    private OnLocationChangedListener mListener;
    
    private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 1;
	private IAppManager imService = null;
	
//	String picUrl=SocketOperator.AUTHENTICATION_SERVER_ADDRESS+"user.png";
	
	String displayName;
    
    Button btnback, btnsubmit;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 try {
			 Bundle extras = this.getIntent().getExtras();
			 String friendObject = extras.getString("friendsObject");
			/* String strLat = extras.getString("lattitude");
			 String strLong = extras.getString("longitude");
			 displayName = extras.getString("name");
	         mLatitude = Double.parseDouble(strLat);
	         mLongitude = Double.parseDouble(strLong);*/
			 // Loading map
	            initilizeMap(friendObject);
	 
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		 
		
	}
	
	/**
     * function to load map. If map is not created it will create it for you
     * */
    @SuppressWarnings("static-access")
	private void initilizeMap(String friendObject) {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.setMapType(this.googleMap.MAP_TYPE_NORMAL);
            googleMap.setMyLocationEnabled(true);
            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(5), 2000, null);
             
            
            try{
            	JSONArray singleJson=new JSONArray(friendObject);
            for(int i=0;i<singleJson.length();i++){
 
            new AsyncTask<JSONObject, Void, MarkerOptions>() { 
            	
            	protected void onPostExecute(MarkerOptions result) { 
            		 googleMap.addMarker(result);
            		
            	}; 
            	
            	@Override protected MarkerOptions doInBackground(JSONObject... params) { 
            		
            		MarkerOptions newMarker = null;
            		try {
            		JSONObject oneObject=params[0];
            		
            		
            		Bitmap markerBitmap = null;
            		Bitmap bmImg = getUserBitmap(oneObject.getString("imgstr"));
					Bitmap back = getBackBitmap(true);
					
					markerBitmap = getMarkerBitmap(bmImg, back);
            	
            		//add marker to Map
            		double lattitude= Double.parseDouble(oneObject.getString("lattitude"));
					double longitude=Double.parseDouble(oneObject.getString("longitude"));
				
					newMarker = 	new MarkerOptions().position(new LatLng(lattitude,longitude )).title(oneObject.getString("displayname"))
         		    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
         		    // Specifies the anchor to be at a particular point in the marker image.
         		    .anchor(0.5f, 1);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		return newMarker;
            		
            		//return Utils.loadBitmap(url); 
            		
            	} 
            	
            }.execute(singleJson.getJSONObject(i));
                    
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        
            }
        }catch(JSONException e){e.printStackTrace();}
        
        }
    }
 
   

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
				Intent i = new Intent(MainActivity.this, Login.class);
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
	
	@Override
	protected void onPause() 
	{
		//unregisterReceiver(messageReceiver);		
		unbindService(mConnection);
	//	JSONArray friends = FriendsController.getFriendsInfo();
	//	initilizeMap(friends.toString());
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
			
		super.onResume();
		bindService(new Intent(mCtx, FriendLocationService.class), mConnection , Context.BIND_AUTO_CREATE);

		}
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((FriendLocationService.IMBinder)service).getService();      
			
			
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(mCtx, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	
	private Bitmap getMarkerBitmap(Bitmap foreground, Bitmap background){
		//Bitmap mBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.app_icon);
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmp = Bitmap.createBitmap(75, 90, conf);
		Canvas canvas1 = new Canvas(bmp);
		
		canvas1.drawBitmap(background, 0,0, null);
		canvas1.drawBitmap(foreground, 12,10, null);
		
		return bmp;
	}
	
	private Bitmap getUserBitmap(String mUrl){
		Bitmap bmp=null;
		URL url;
		try {
			url = new URL(SocketOperator.AUTHENTICATION_SERVER_UPLOAD_ADDRESS+mUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();   
			conn.setDoInput(true);   
			conn.connect();     
			InputStream is = conn.getInputStream();
			bmp = BitmapFactory.decodeStream(is);            			
			bmp = Bitmap.createScaledBitmap(bmp, 50, 50, true);
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		//add marker to Map
		
	
		return bmp;
		
	}
	
	private Bitmap getBackBitmap(boolean isOnline){
		Bitmap mbmp=null;
		if(isOnline){
			mbmp = BitmapFactory.decodeResource(getResources(),R.drawable.bubble_green);
		}else{
			mbmp = BitmapFactory.decodeResource(getResources(),R.drawable.bubble_orange);
		}
		mbmp = Bitmap.createScaledBitmap(mbmp, 75, 90, true);
		
		return mbmp;
		
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if( mListener != null )
	    {
	        mListener.onLocationChanged( location );

	        LatLngBounds bounds = this.googleMap.getProjection().getVisibleRegion().latLngBounds;

	        if(!bounds.contains(new LatLng(location.getLatitude(), location.getLongitude())))
	        {
	             //Move the camera to the user's location if they are off-screen!
	             googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),12.0f));
	             googleMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
	        }
	    }
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		mListener = null;
	}
	
	
}
