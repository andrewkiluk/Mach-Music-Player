package com.andrewkiluk.machmusicplayer;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class SongsFragment extends ListFragment {

	private ListView lv;
	SongArrayAdapter adapter;
	public String origin;
	public int artistPosition;
	public int albumPosition;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		artistPosition = getArguments().getInt("artistPosition");
		albumPosition = getArguments().getInt("albumPosition");
		origin = getArguments().getString("origin");
		if (origin.equals("songs")){ //   Means that we're getting here from the main songs list
			songsList = LibraryInfo.songsList;
		}

		else if(origin.equals("albums")){ //   Means that we're getting here from the albums menu
			songsList = LibraryInfo.albumsList.get(albumPosition).songs;
		}
		else{ // Means that we're getting here from the artists menu
			songsList = LibraryInfo.artistsList.get(artistPosition).albums.get(albumPosition).songs;
		}


		View rootView = inflater.inflate(R.layout.fragment_list, container, false);





		SongListData[] ObjectItemData = new SongListData[songsList.size()];

		// looping through playlist
		int i=0;
		if (LibraryInfo.isInitialized){
			Utilities utils = new Utilities();
			for (Song song : songsList) {
				ObjectItemData[i] = new SongListData(i, song.title(), song, utils.milliSecondsToTimer(Integer.parseInt(song.duration()))); 
				i++;
			}

			adapter = new SongArrayAdapter(getActivity(), R.layout.list_view_row_item, ObjectItemData);

			// If we got here from an albums list and the album was selected, set the UI to have all songs begin as selected.

			if(origin.equals("artists")){  // We're getting here from an artists fragment
				for(i=0; i< LibraryInfo.artistsList.get(artistPosition).albums.get(albumPosition).songs.size(); i++){
					if (SelectionStatus.artistsListSelection[artistPosition][albumPosition][i] == true){
						adapter.selectedStatus[i] = true;						
					}
				}
			}
			if(origin.equals("albums")){  // We're getting here from an albums fragment
				for(i=0; i< LibraryInfo.albumsList.get(albumPosition).songs.size(); i++){
					if (SelectionStatus.albumsListSelection[albumPosition][i] == true){
						adapter.selectedStatus[i] = true;						
					}
				}
			}
			if(origin.equals("songs")){  // We're getting here from an songs fragment (it gets destroyed if we go all the way to the right!)
				for(i=0; i< LibraryInfo.songsList.size(); i++){
					if (SelectionStatus.songsListSelection[i] == true){
						adapter.selectedStatus[i] = true;						
					}
				}
			}

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
				if(adapter.selectedStatus[position]){
					adapter.selectedStatus[position] = false;
					view.setBackgroundResource(R.color.footercolor);

					if(origin.equals("artists")){  // We're getting here from an artists fragment
						SelectionStatus.artistsListSelection[artistPosition][albumPosition][position] = false;
					}
					if(origin.equals("albums")){  // We're getting here from an albums fragment
						SelectionStatus.albumsListSelection[albumPosition][position] = false;
					}
					if (origin.equals("songs")){
						SelectionStatus.songsListSelection[position] = false;
					}


				}
				else{
					adapter.selectedStatus[position] = true;
					view.setBackgroundResource(R.color.selected);

					if(origin.equals("artists")){  // We're getting here from an artists fragment
						SelectionStatus.artistsListSelection[artistPosition][albumPosition][position] = true;
					}
					if(origin.equals("albums")){  // We're getting here from an albums fragment
						SelectionStatus.albumsListSelection[albumPosition][position] = true;
					}
					if (origin.equals("songs")){ // We're getting here by swiping left to the songs fragment
						SelectionStatus.songsListSelection[position] = true;
					}


				}
				Song newSong = songsList.get(position);
				ArrayList<Song>localNewSongs = new ArrayList<Song> (LibraryInfo.newSongs);

				// if it's in the add queue, remove it, otherwise add it to the add queue.
				if(localNewSongs.contains(newSong)){
					for(Song song : localNewSongs){
						if(song.equals(newSong)){
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