package com.haraye.friendlocation.adapters;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.haraye.friendlocation.R;
import com.haraye.friendlocation.service.SocketOperator;

public class FriendReqListAdapter extends BaseAdapter 
{		
	class ViewHolder {
		TextView text;
		TextView textId;
		ImageView icon;
		CheckBox cb;
		ImageView friendPic;
	}
	private LayoutInflater mInflater;
	private Bitmap mOnlineIcon;
	private Bitmap mOfflineIcon;		
	public static ArrayList<String> checkedIds = new ArrayList<String>();

	//private FriendInfo[] friends = null;
	JSONArray friends = null;


	public FriendReqListAdapter(Context context) {
	
		this.mInflater = LayoutInflater.from(context);
		
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

	public View getView(final int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no need
		// to reinflate it. We only inflate a new View when the convertView supplied
		// by ListView is null.
		if (convertView == null) 
		{
			convertView = mInflater.inflate(R.layout.items_friends_req_list, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.textId = (TextView) convertView.findViewById(R.id.fr_id);
			holder.text = (TextView) convertView.findViewById(R.id.fr_name);
			holder.icon = (ImageView) convertView.findViewById(R.id.fr_icon);   
			holder.cb = (CheckBox) convertView.findViewById(R.id.fr_check); 
			holder.friendPic = (ImageView) convertView.findViewById(R.id.fr_list_image);

			convertView.setTag(holder);
		}   
		else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		
		try {
			holder.cb.setTag(this.getItem(position).getString("id"));
			holder.textId.setText(this.getItem(position).getString("id"));
			holder.text.setText(this.getItem(position).getString("displayname"));
			holder.icon.setImageBitmap(this.getItem(position).getString("status") == "online" ? mOnlineIcon : mOfflineIcon);
			AQuery aQuery = new AQuery(holder.friendPic);
			aQuery.image(SocketOperator.AUTHENTICATION_SERVER_UPLOAD_ADDRESS+this.getItem(position).getString("imgstr"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		checkedIds.clear();
		holder.cb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CheckBox cb = (CheckBox) v ;
				if(cb.isChecked() && !checkedIds.contains(cb.getTag().toString()))
				{
					checkedIds.add(cb.getTag().toString());
				}
				else if(!cb.isChecked() && checkedIds.contains(cb.getTag().toString()))
				{
					checkedIds.remove(cb.getTag().toString());
				}
			}
		});
		
		return convertView;
	}

}