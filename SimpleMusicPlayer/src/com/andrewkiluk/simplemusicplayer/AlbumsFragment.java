package com.andrewkiluk.simplemusicplayer;
 
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
 
public class AlbumsFragment extends ListFragment {

	  public ArrayList<Album> albumsList;
	  public int artistPosition; // This flag means that the album was not reached by the artists menu, but directly, so don't find position through artists list.
	  
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        
        // "Constructor"
        artistPosition = getArguments().getInt("artistPosition");
		  if (artistPosition == -1){
			  artistPosition = -10;   // Reset to indicate later that we're not getting here from the artists menu. 
			  albumsList = LibraryInfo.albumsList;
		  }
		  else{
			  albumsList = LibraryInfo.artistsList.get(artistPosition).albums;
		  }
		  
        
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

		ListView lv = getListView();

		// listening for song selection 
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				SongsFragment newFragment = new SongsFragment();
				Bundle args = new Bundle();
				args.putInt("artistPosition", artistPosition);
				args.putInt("albumPosition", position);
				newFragment.setArguments(args);
		        FragmentTransaction transaction = getFragmentManager().beginTransaction();
		        transaction.addToBackStack(null);
		        transaction.replace(R.id.list_frame, newFragment, "com.andrewkiluk.simplemusicplayer.AlbumsFragment").commit(); 

			}
		});

	}
    
    
}