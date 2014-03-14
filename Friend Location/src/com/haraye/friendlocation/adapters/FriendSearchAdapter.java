package com.haraye.friendlocation.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.haraye.friendlocation.R;
import com.haraye.friendlocation.service.SocketOperator;

public class FriendSearchAdapter extends BaseAdapter 
{		
	class ViewHolder {
		TextView text;
		TextView textId;
		ImageView friendPic;
	}
	private LayoutInflater mInflater;
	public ArrayList<HashMap<String, String>> data;

	


	public FriendSearchAdapter(Context context, ArrayList<HashMap<String, String>> friend) {
	
		this.mInflater = LayoutInflater.from(context);
		this.data=friend;
		
	}

	 public int getCount() {
	        return data.size();
	    }
	 public void removeItem(int position){
		 
		 data.remove(position);
		 this.notifyDataSetChanged();
	 }
	    public Object getItem(int position) {
	        return data.get(position);
	    }
	 
	    public long getItemId(int position) {
	        return position;
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
			convertView = mInflater.inflate(R.layout.items_friends_search_list, null);

			// Creates a ViewHolder and store references to the two children views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.textId = (TextView) convertView.findViewById(R.id.fs_id);
			holder.text = (TextView) convertView.findViewById(R.id.fs_name);
			holder.friendPic = (ImageView) convertView.findViewById(R.id.fs_list_image);
			holder.friendPic.setFocusable(false);

			convertView.setTag(holder);
		}   
		else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		
		HashMap<String, String> song = new HashMap<String, String>();
		song = data.get(position);
		holder.textId.setText(song.get("id").toString());
		holder.text.setText(song.get("displayname").toString());
		AQuery aQuery = new AQuery(holder.friendPic);
		aQuery.image(SocketOperator.AUTHENTICATION_SERVER_UPLOAD_ADDRESS+song.get("imgstr").toString());
		
		return convertView;
	}

}