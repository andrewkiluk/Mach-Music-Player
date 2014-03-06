package com.andrewkiluk.simplemusicplayer;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
 

 
public class PlayListActivity extends FragmentActivity implements PlaylistFragment.TouchListener {
    // Songs list
    public String library_location;
    
    private Button button_add_songs;
    private Button button_clear_playlist;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist);
        
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        PlaylistFragment playlistfragment = new PlaylistFragment(); 
        fragmentTransaction.add(R.id.playlist_container, playlistfragment);
        fragmentTransaction.commit();
        
        AppStatus.isVisible = true;

        button_add_songs = (Button) findViewById(R.id.button_add_songs);
        button_clear_playlist = (Button) findViewById(R.id.button_clear_playlist);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.tabcolor)));
 
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        library_location = sharedPrefs.getString("library_location", "NULL");
        
        
        button_add_songs.setOnClickListener(new View.OnClickListener() {
        	 
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListBuilderActivity.class);
                startActivityForResult(i, 200);
                
            }
        });
        
        button_clear_playlist.setOnClickListener(new View.OnClickListener() {
       	 
            @Override
            public void onClick(View arg0) {
                LibraryInfo.clearPlaylist();
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction(); 
                PlaylistFragment refresh = new PlaylistFragment();
                ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.simplemusicplayer.PlaylistFragment"); 
                ft.commit(); 
            }
        });
        
          
        
        
    }
    
    // This method is called when the playlist fragment detects a song choice.
    @Override
    public void songPicked(int songIndex) {
    	// Starting new intent
	    Intent in = new Intent(getApplicationContext(),
	            MusicPlayerActivity.class);
	    // Sending songIndex to PlayerActivity
	    in.putExtra("songIndex", songIndex);
	    setResult(100, in);
	    // Closing PlayListView
	    finish();
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
		if(resultCode == 200){
					
			// Update UI
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction(); 
            PlaylistFragment refresh = new PlaylistFragment();
            ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.simplemusicplayer.PlaylistFragment"); 
            ft.commit(); 
		}

	}
// Useless for now, will use later to receive intents from playlist builder
//	@Override
//    protected void onActivityResult(int requestCode,
//                                     int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == 100){
//
//        }
// 
//    }
}