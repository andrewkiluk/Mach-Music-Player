package com.andrewkiluk.androsmusicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

// here's our beautiful adapter
public class ArrayAdapterItem extends ArrayAdapter<ObjectItem> {

	Context mContext;
	int layoutResourceId;
	ObjectItem data[] = null;
	boolean selectedStatus[];

	public ArrayAdapterItem(Context mContext, int layoutResourceId, ObjectItem[] data) {

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
			viewHolder.textViewItem = (TextView) convertView.findViewById(R.id.label);
			viewHolder.layoutItem = (RelativeLayout) viewHolder.textViewItem.getParent();

			// store the holder with the view.
			convertView.setTag(viewHolder);
			if (selectedStatus[position] == true)
				convertView.setBackgroundResource(R.color.selected);

		}else{
			// we've just avoided calling findViewById() on resource every time
			// just use the viewHolder
			viewHolder = (ViewHolderItem) convertView.getTag();
		}

		// object item based on the position
		ObjectItem objectItem = data[position];

		// assign values if the object is not null
		if(objectItem != null) {
			// get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) values
			viewHolder.textViewItem.setText(objectItem.itemName);
			viewHolder.textViewItem.setTag(objectItem.itemId);
		}
		
//		if(selectedStatus[position]){
//            viewHolder.layoutItem.setBackgroundResource(R.color.footercolor);
//            Log.d("class",""+ LibraryInfo.newSongs.size());
//        }
//        else{
//            viewHolder.layoutItem.setBackgroundResource(R.color.selected);
//            Log.d("class",""+ LibraryInfo.newSongs.size());
//        }
//		Song newSong = data[position].song;
//		ArrayList<Song>localNewSongs = new ArrayList<Song> (LibraryInfo.newSongs);
//		if(localNewSongs.contains(newSong)){
//			for(Song song : localNewSongs){
//				if(song.title().equals(newSong.title() ) && song.artist().equals(newSong.artist() ) && song.album().equals(newSong.album() )){
//					LibraryInfo.newSongs.remove(LibraryInfo.newSongs.indexOf(song));
//				}
//			}
//		}
//		else{
//			LibraryInfo.newSongs.add(newSong);   
//		}

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
		TextView textViewItem;
		RelativeLayout layoutItem;
	}


}
