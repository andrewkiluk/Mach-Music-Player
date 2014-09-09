package com.andrewkiluk.machmusicplayer.fragments;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.andrewkiluk.machmusicplayer.R;
import com.andrewkiluk.machmusicplayer.R.id;
import com.andrewkiluk.machmusicplayer.R.layout;
import com.andrewkiluk.machmusicplayer.models.Artist;
import com.andrewkiluk.machmusicplayer.models.LibraryInfo;

public class ArtistsFragment extends ListFragment {

	public interface TouchListener {
		public void artistPicked(int position);
	}

	public ArrayList<String> albumsList = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		// looping through playlist
		ArrayList<String> artistNamesList = new ArrayList<String>();
		if (LibraryInfo.isInitialized){
			for (Artist artist : LibraryInfo.artistsList) {
				// creating new HashMap
				artistNamesList.add(artist.name);
			}

			// Adding menuItems to ListView
			ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
					R.layout.playlist_builder_item, artistNamesList);
			setListAdapter(adapter);
		}




		return rootView;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView lv = getListView();

		// listening for song selection 
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				AlbumsFragment newFragment = new AlbumsFragment();
				Bundle args = new Bundle();
				args.putInt("artistPosition", position);
				args.putString("origin", "artists");
				newFragment.setArguments(args);
		        FragmentTransaction transaction = getFragmentManager().beginTransaction();
		        transaction.addToBackStack(null);
		        transaction.replace(R.id.list_frame, newFragment, "com.andrewkiluk.androsmusicplayer.AlbumsFragment").commit(); 

			}
		});

	}

}