package com.andrewkiluk.machmusicplayer;

import java.util.ArrayList;

import android.app.Activity;
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




public class PlaylistManagerFragment extends ListFragment {

	public interface TouchListener {
		public void playlistPicked(int position);
	}

	int playlistPosition ;

	TouchListener onPlaylistPickedListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onPlaylistPickedListener = (TouchListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement TouchListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

		// looping through playlist
		ArrayList<String> playlistnamesList = new ArrayList<String>();
		if (LibraryInfo.isInitialized){
			for (Playlist playlist : LibraryInfo.playlists) {
				// creating new HashMap
				playlistnamesList.add(playlist.name);
			}

			// Adding menuItems to ListView
			ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
					R.layout.playlist_builder_item, playlistnamesList);
			setListAdapter(adapter);
		}

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// selecting single ListView item
		ListView lv = getListView();

		// listening to single listitem click
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				
				LibraryInfo.currentPlaylist = new Playlist(LibraryInfo.playlists.get(position).songs, "__CURRENT_PLAYLIST__"); 
				
				onPlaylistPickedListener.playlistPicked(position);

				// Send message to playlist activity that playlist has been chosen
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				playlistPosition = position;
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
				alertDialog.setTitle("Delete Playlist");
				alertDialog.setMessage("Delete current playlist?");
				// set positive button: Yes message
				alertDialog.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();

						// Remove playlist
						Playlist oldPlaylist = LibraryInfo.playlists.get(playlistPosition);
						ArrayList<Playlist> localPlaylistList = new ArrayList<Playlist> (LibraryInfo.playlists);
						for(Playlist playlist : localPlaylistList){
							if(playlist.name.equals(oldPlaylist.name)){
								LibraryInfo.playlists.remove(LibraryInfo.playlists.indexOf(playlist));
							}
						}
						
						// Update UI
						final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
			            PlaylistManagerFragment refresh = new PlaylistManagerFragment();
			            ft.replace(R.id.playlists_container, refresh, "com.andrewkiluk.androsmusicplayer.PlaylistFragment"); 
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