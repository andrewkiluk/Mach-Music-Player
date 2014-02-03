package com.andrewkiluk.simplemusicplayer;
 
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
 
public class AlbumsFragment extends ListFragment {
	
	public ArrayList<String> albumsList = new ArrayList<String>();
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);
        
         // looping through playlist
        ArrayList<String> songnamesList = new ArrayList<String>();
        for (Song song : LibraryInfo.songsList) {
            // creating new HashMap
             songnamesList.add(song.title());
        }
 
        // Adding menuItems to ListView
        ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.playlist_item, songnamesList);
        
        setListAdapter(adapter);
         
        return rootView;
    }
    
    
}