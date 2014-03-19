package com.andrewkiluk.machmusicplayer;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AlbumsFragment extends ListFragment {

	public ArrayList<Album> albumsList;
	public int artistPosition;
	public ListView lv;
	int tempAlbumPosition; // This is used to keep track of which album we have a dialogue asking about. 
//	boolean selectAllSongsInAlbumPosition[];
	// Since there's no concurrency in this situation, it's okay to have it global, and there's really no other good way to pass it. 
	public String origin;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		// "Constructor"
		artistPosition = getArguments().getInt("artistPosition");
		origin = getArguments().getString("origin");
		if (artistPosition == -1){
			albumsList = LibraryInfo.albumsList;
		}
		else{
			albumsList = LibraryInfo.artistsList.get(artistPosition).albums;
		}

//		selectAllSongsInAlbumPosition = new boolean[LibraryInfo.albumsList.size()];


		// looping through playlist
		ArrayList<String> albumNamesList = new ArrayList<String>();
		if (LibraryInfo.isInitialized){
			for (Album album : albumsList) {
				// creating new HashMap
				albumNamesList.add(album.title);
			}

			// Adding menuItems to ListView
			ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
					R.layout.playlist_builder_item, albumNamesList);
			setListAdapter(adapter);
		}
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		lv = getListView();

		// listening for song selection 
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				SongsFragment newFragment = new SongsFragment();
				Bundle args = new Bundle();
				args.putInt("artistPosition", artistPosition);
				args.putInt("albumPosition", position);
				if (origin.equals("artists")){
					args.putString("origin", "artists");
				}
				else{
					args.putString("origin", "albums");
				}
//				args.putBoolean("SelectAll", selectAllSongsInAlbumPosition[position]);
//				selectAllSongsInAlbumPosition[position] = false;
				newFragment.setArguments(args);
				FragmentTransaction transaction = getFragmentManager().beginTransaction();
				transaction.addToBackStack(null);
				transaction.replace(R.id.list_frame, newFragment, "com.andrewkiluk.androsmusicplayer.AlbumsFragment").commit(); 

			}
		});

		lv.setLongClickable(true);



		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				tempAlbumPosition = position;

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
				alertDialog.setTitle("Add Album");
				alertDialog.setMessage("Select all songs in this album?");
				// set positive button: Yes message
				alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
						ArrayList<Song> indexList;
						if(origin.equals("artists")){ // If we got here from the artists tab
							indexList = LibraryInfo.artistsList.get(artistPosition).albums.get(tempAlbumPosition).songs;
//							selectAllSongsInAlbumPosition[tempAlbumPosition] = true;
							for(int i=0 ; i<LibraryInfo.artistsList.get(artistPosition).albums.get(tempAlbumPosition).songs.size() ; i++){
								SelectionStatus.artistsListSelection[artistPosition][tempAlbumPosition][i] = true;
							}
							
						}
						else if(origin.equals("albums")){ // If we got here from the albums tab
							indexList = LibraryInfo.albumsList.get(tempAlbumPosition).songs;
//							selectAllSongsInAlbumPosition[tempAlbumPosition] = true;
							for(int i=0 ; i<LibraryInfo.albumsList.get(tempAlbumPosition).songs.size() ; i++){
								SelectionStatus.albumsListSelection[tempAlbumPosition][i] = true;
							}
						}
						else{
							indexList = new ArrayList<Song>();
						}
						
						for (Song newSong : indexList){
							ArrayList<Song>localNewSongs = new ArrayList<Song> (LibraryInfo.newSongs);

							// We first remove any songs currently selected from the newSongs queue. If we want songs our songs in this album to be unselectable
							// and just generally behave like the rest of the songs in this activity, we need to do this first.
							if(localNewSongs.contains(newSong)){
								for(Song song : localNewSongs){
									if(song.title().equals(newSong.title() ) && song.artist().equals(newSong.artist() ) && song.album().equals(newSong.album() )){
										LibraryInfo.newSongs.remove(LibraryInfo.newSongs.indexOf(song));
									}
								}
							}
							LibraryInfo.newSongs.add(newSong);   
						}

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


	}


}