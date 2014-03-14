package com.haraye.friendlocation.adapters;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.android.maps.GeoPoint;
import com.haraye.friendlocation.R;
import com.haraye.friendlocation.service.SocketOperator;

public class FriendListAdapter extends BaseAdapter 
{		
	class ViewHolder {
		TextView text;
		TextView textId;
		TextView location;
		ImageView icon;
		ImageView friendPic;
	}
	private LayoutInflater mInflater;
	private Bitmap mOnlineIcon;
	private Bitmap mOfflineIcon;		
	private Context mContext;
	//private FriendInfo[] friends = null;
	JSONArray friends = null;


	public FriendListAdapter(Context context) {
	
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.online);
		mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.offline);

	}

	public void setFriendList(JSONArray friends)
	{
		this.friends = friends;
	}


	public int getCount() {		

		return friends.length();
	}
	

	public JSONObject getItem(int position) {			

		JSONObject c = null;
		try {
			c = friends.getJSONObject(position);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return c;
	}

	public long getItemId(int position) {

		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.
		if (convertView == null) 
		{
			convertView = mInflater.inflate(R.layout.items_friends_list, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.textId = (TextView) convertView.findViewById(R.id.fl_id);
			holder.location = (TextView) convertView.findViewById(R.id.fl_location);
			holder.text = (TextView) convertView.findViewById(R.id.fl_name);
			holder.icon = (ImageView) convertView.findViewById(R.id.fl_icon);  
			holder.friendPic = (ImageView) convertView.findViewById(R.id.fl_list_image);

			convertView.setTag(holder);
		}   
		else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		
		try {
			holder.textId.setText(this.getItem(position).getString("id"));
			holder.text.setText(this.getItem(position).getString("displayname"));
		//	holder.location.setText(locationFromLatLong("50.8179826","12.9305875"));
			holder.location.setText(locationFromLatLong(this.getItem(position).getString("lattitude"), this.getItem(position).getString("longitude")));
			holder.icon.setImageBitmap(this.getItem(position).getString("status") == "online" ? mOnlineIcon : mOfflineIcon);
			AQuery aQuery = new AQuery(holder.friendPic);
			aQuery.image(SocketOperator.AUTHENTICATION_SERVER_UPLOAD_ADDRESS+this.getItem(position).getString("imgstr"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return convertView;
	}
	
	public String locationFromLatLong(String lattitudeStr, String longitudeStr){
		String location = "Not Specified";
		//add marker to Map
		double lattitude= Double.parseDouble(lattitudeStr);
		double longitude=Double.parseDouble(longitudeStr);
		GeoPoint point = new GeoPoint(
                (int) (lattitude * 1E6), 
                (int) (longitude * 1E6));
		location = ConvertPointToLocation(point);
		
		return location;
	}
	
	public String ConvertPointToLocation(GeoPoint point) {   
        String address = "";
        Geocoder geoCoder = new Geocoder(
        		this.mContext, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(
                point.getLatitudeE6()  / 1E6, 
                point.getLongitudeE6() / 1E6, 1);

            if (addresses.size() > 0) {
                for (int index = 0; index < addresses.get(0).getMaxAddressLineIndex(); index++)
                    address += addresses.get(0).getAddressLine(index) + " ";
            }
        }
        catch (IOException e) {                
            e.printStackTrace();
        }   

        return address;
    }

}