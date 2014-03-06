package com.andrewkiluk.simplemusicplayer;
 
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;


 

public class PlaylistFragment extends ListFragment {
	
	public interface TouchListener {
	    public void songPicked(int songIndex);
	}
	
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
 
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        
         // looping through playlist
        ArrayList<String> songnamesList = new ArrayList<String>();
        if (LibraryInfo.isInitialized){
        	int i=0;
        	for (Song song : LibraryInfo.currentPlaylist) {
                // creating new HashMap
                 songnamesList.add(Integer.toString(++i) + ") " + song.title());
            }
     
            // Adding menuItems to ListView
            ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.playlist_builder_item, songnamesList);
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
                // getting listitem index
                int songIndex = position;
                
                onSongPickedListener.songPicked(songIndex);

                // Send message to playlist activity that song has been chosen
            }
        });
  		super.onActivityCreated(savedInstanceState);
  	}
    
}