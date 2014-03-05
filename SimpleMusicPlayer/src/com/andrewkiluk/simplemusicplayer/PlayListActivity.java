package com.andrewkiluk.simplemusicplayer;
 
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
 
public class PlayListActivity extends ListActivity {
    // Songs list
    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    public String library_location;
    
    private Button button_add_songs;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);
        
        AppStatus.isVisible = true;

        button_add_songs = (Button) findViewById(R.id.button_add_songs);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2b2b3b")));
 
        ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        library_location = sharedPrefs.getString("library_location", "NULL");
        
        SongsManager plm = new SongsManager(library_location);
        
        // get all songs from sdcard
        this.songsList = plm.getPlayList();
 
        // looping through playlist
        for (int i = 0; i < songsList.size(); i++) {
            // creating new HashMap
            HashMap<String, String> song = songsList.get(i);
 
            // adding HashList to ArrayList
            songsListData.add(song);
        }
 
        // Adding menuItems to ListView
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[] { "playlistSongTitle" }, new int[] {
                        R.id.songTitle });
 
        setListAdapter(adapter);
 
        // selecting single ListView item
        ListView lv = getListView();
        
        // listening to single listitem click
        lv.setOnItemClickListener(new OnItemClickListener() {
 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting listitem index
                int songIndex = position;
 
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        MusicPlayerActivity.class);
                // Sending songIndex to PlayerActivity
                in.putExtra("songIndex", songIndex);
                setResult(100, in);
                // Closing PlayListView
                finish();
            }
        });
        
        button_add_songs.setOnClickListener(new View.OnClickListener() {
        	 
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListBuilderActivity.class);
                startActivityForResult(i, 100);
            }
        });
        
        
    }
    
    
    
    @Override
	protected void onStop() {
    	AppStatus.isVisible = false;
		super.onStop();
	}



	@Override
    protected void onActivityResult(int requestCode,
                                     int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){

        }
 
    }
}