package com.andrewkiluk.machmusicplayer.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.andrewkiluk.machmusicplayer.PlayListAdapter;
import com.andrewkiluk.machmusicplayer.R;
import com.andrewkiluk.machmusicplayer.R.id;
import com.andrewkiluk.machmusicplayer.R.layout;
import com.andrewkiluk.machmusicplayer.models.CurrentData;
import com.andrewkiluk.machmusicplayer.models.PlayerStatus;
import com.mobeta.android.dslv.DSLVFragment;




public class PlaylistFragment extends DSLVFragment {
	
	public interface TouchListener {
		public void songPicked(int songIndex);
		public void playerReset();
		public void getNextSong();
	}

	int currentSongPosition;

	TouchListener onSongPickedListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onSongPickedListener = (TouchListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement onSongPickedListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

		return rootView;
	}
	
	
	
	
	@Override
	public void setListAdapter() {
//		if (LibraryInfo.isInitialized){
////			int i=0;
//			ArrayList<String> songnamesList = new ArrayList<String>();
//			for (Song song : CurrentData.currentPlaylist.songs) {
//				// creating new HashMap
//				songnamesList.add(song.title());
//			}
//			// Adding menuItems to ListView
//			adapter = new ArrayAdapter<String>(getActivity(), getItemLayout(), R.id.text, songnamesList);
//			setListAdapter(adapter);
//			
//			
//		}
		
		adapter = new PlayListAdapter(getActivity(), R.layout.list_item_handle_left, CurrentData.currentPlaylist.songs);
		setListAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// selecting single ListView item
		ListView lv = getListView();
		
		View listEntry;
		TextView numberHolder;
		for (int i = 0; i < lv.getCount(); i++) {
			listEntry = lv.getAdapter().getView(i, null, null);
	        numberHolder = (TextView) listEntry.findViewById(R.id.time);
	        numberHolder.setText(Integer.toString(i));
	    }

		// listening to single listitem click
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting listitem index
				int songIndex = position;

				onSongPickedListener.songPicked(songIndex);

				// Send message to playlist activity that song has been chosen
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				currentSongPosition = position;
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
				alertDialog.setTitle("Remove Song");
				alertDialog.setMessage("Remove song from current playlist?");
				// set positive button: Yes message
				alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();

						// Remove song
						if(CurrentData.currentPlaylistPosition == currentSongPosition){ 

							onSongPickedListener.getNextSong();
							PlayerStatus.playerReady = false;
							onSongPickedListener.playerReset();
						}
						if (CurrentData.currentPlaylistPosition >= currentSongPosition){

							// Decrement the position tracking variables to correct for the change in playlist size
							CurrentData.currentPlaylistPosition = CurrentData.currentPlaylistPosition - 1;
							CurrentData.currentSongIndex = CurrentData.currentSongIndex - 1;

						}
						CurrentData.currentPlaylist.songs.remove(currentSongPosition);

						// Playlist has been modified, reset the shuffle queue
						CurrentData.shuffleReset();

						// Update UI
						final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
						PlaylistFragment refresh = new PlaylistFragment();
						ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.androsmusicplayer.PlaylistFragment"); 
						ft.commit(); 

					}
				});
				// set negative button: No message
				alertDialog.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();


					}
				});
				alertDialog.show();

				return true;
			}
		});

		super.onActivityCreated(savedInstanceState);
	}

}