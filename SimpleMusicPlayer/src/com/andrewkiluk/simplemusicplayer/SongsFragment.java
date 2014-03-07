package com.andrewkiluk.simplemusicplayer;

import java.util.ArrayList;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SongsFragment extends ListFragment {

	private ListView lv;
	boolean selectedStatus[];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		int artistPosition = getArguments().getInt("artistPosition");
		int albumPosition = getArguments().getInt("albumPosition");
		if (artistPosition  == -1){ //   Means that we're getting here from the main songs list
			Log.d("debuggg","artistPosition: "+Integer.toString(artistPosition)+ " albumPosition: " + Integer.toString(albumPosition));
			songsList = LibraryInfo.songsList;
		}

		else if(artistPosition  == -10){ //   Means that we're getting here from the albums menu
			Log.d("debuggg","artistPosition: "+Integer.toString(artistPosition)+ " albumPosition: " + Integer.toString(albumPosition));
			songsList = LibraryInfo.albumsList.get(albumPosition).songs;
		}
		else{
			songsList = LibraryInfo.artistsList.get(artistPosition).albums.get(albumPosition).songs;
		}

		selectedStatus = new boolean[songsList.size()]; 

		View rootView = inflater.inflate(R.layout.fragment_list, container, false);





		ObjectItem[] ObjectItemData = new ObjectItem[songsList.size()];

		// looping through playlist
		ArrayList<String> songnamesList = new ArrayList<String>();
		int i=0;
		if (LibraryInfo.isInitialized){
			for (Song song : songsList) {
				// creating new HashMap
				ObjectItemData[i] = new ObjectItem(i, song.title());
				i++;
			}


			ArrayAdapterItem adapter = new ArrayAdapterItem(getActivity(), R.layout.playlist_builder_item, ObjectItemData);



//			// looping through playlist
//			ArrayList<String> songnamesList = new ArrayList<String>();
//			if (LibraryInfo.isInitialized){
//				for (Song song : songsList) {
//					// creating new HashMap
//					songnamesList.add(song.title());
//				}

				// Adding menuItems to ListView


				//			ListAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.playlist_builder_item, songnamesList);
				setListAdapter(adapter);
			}


			return rootView;
		}


		private ArrayList<Song> songsList;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			lv = getListView();

			// listening for song selection 
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					int viewPosition = position - lv.getFirstVisiblePosition();
					Log.d("debug","First: "+Integer.toString(lv.getFirstVisiblePosition())+ " Pressed: " + Integer.toString(position));
					if(selectedStatus[position]){
						selectedStatus[position] = false;
						View currentEntry = lv.getChildAt(viewPosition);
						currentEntry.setBackgroundResource(R.color.footercolor);
					}
					else{
						selectedStatus[position] = true;
						View currentEntry = lv.getChildAt(viewPosition);
						currentEntry.setBackgroundResource(R.color.selected);
					}
					Song newSong = songsList.get(position);
					ArrayList<Song>localNewSongs = new ArrayList<Song> (LibraryInfo.newSongs);
					if(localNewSongs.contains(newSong)){
						for(Song song : localNewSongs){
							if(song.title().equals(newSong.title() ) && song.artist().equals(newSong.artist() ) && song.album().equals(newSong.album() )){
								LibraryInfo.newSongs.remove(LibraryInfo.newSongs.indexOf(song));
							}
						}
					}
					else{
						LibraryInfo.newSongs.add(newSong);   
					}

				}
			});

		}
	}