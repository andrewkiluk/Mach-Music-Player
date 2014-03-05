package com.andrewkiluk.simplemusicplayer;
 
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
 
public class ArtistsFragment extends ListFragment {
	
	public ArrayList<String> albumsList = new ArrayList<String>();
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);
        
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
    
    
}