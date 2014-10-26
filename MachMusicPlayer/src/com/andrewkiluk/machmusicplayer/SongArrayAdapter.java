package com.andrewkiluk.machmusicplayer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

// here's our beautiful adapter
public class SongArrayAdapter extends ArrayAdapter<SongListData> {

	public Context mContext;
	public int layoutResourceId;
	public SongListData data[] = null;
	public boolean selectedStatus[];

	public SongArrayAdapter(Context mContext, int layoutResourceId, SongListData[] data) {

		super(mContext, layoutResourceId, data);

		this.selectedStatus = new boolean[data.length]; 
		this.layoutResourceId = layoutResourceId;
		this.mContext = mContext;
		this.data = data;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolderItem viewHolder;

		/*
		 * The convertView argument is essentially a "ScrapView" as described is Lucas post 
		 * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
		 * It will have a non-null value when ListView is asking you recycle the row layout. 
		 * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
		 */
		if(convertView==null){

			// inflate the layout
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(layoutResourceId, parent, false);

			// set up the ViewHolder
			viewHolder = new ViewHolderItem();
			viewHolder.textViewLabel = (TextView) convertView.findViewById(R.id.label);
			viewHolder.textViewTime = (TextView) convertView.findViewById(R.id.time);
			viewHolder.layoutItem = (LinearLayout) viewHolder.textViewLabel.getParent();

			// store the holder with the view.
			convertView.setTag(viewHolder);

		}else{
			// we've just avoided calling findViewById() on resource every time
			// just use the viewHolder
			viewHolder = (ViewHolderItem) convertView.getTag();
		}
		
		if (selectedStatus[position] == true){
			convertView.setBackgroundResource(R.color.selected);
		} else{
			convertView.setBackgroundResource(R.color.footercolor);
		}

		// object item based on the position
		SongListData objectItem = data[position];

		// assign values if the object is not null
		if(objectItem != null) {
			// get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) values
			viewHolder.textViewLabel.setText(objectItem.itemName);
			viewHolder.textViewTime.setText(objectItem.songDuration);
			viewHolder.textViewLabel.setTag(objectItem.itemId);
		}
		
		return convertView;

	}
	
	@Override

	public int getViewTypeCount() {                 

	    return getCount();
	}

	@Override
	public int getItemViewType(int position) {

	    return position;
	}

	static class ViewHolderItem {
		TextView textViewLabel;
		TextView textViewTime;
		LinearLayout layoutItem;
	}


}
