package com.andrewkiluk.machmusicplayer;

import com.google.gson.Gson;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;



public class PlayListActivity extends FragmentActivity implements PlaylistFragment.TouchListener {
	// Songs list
	public String library_location;

	private Button button_add_songs;
	private Button button_clear_playlist;
	private Button button_playlists;

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
		button_playlists = (Button) findViewById(R.id.button_playlists);

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
				CurrentData.clearPlaylist();
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction(); 
				PlaylistFragment refresh = new PlaylistFragment();
				ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.androsmusicplayer.PlaylistFragment"); 
				ft.commit(); 
			}
		});

		button_playlists.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), PlayListManagerActivity.class);
				startActivityForResult(i, 200);
			}
		});




	}

	/**
	 * The following two methods are to set up the Settings menu.
	 * */

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.layout.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_settings:
			Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivityForResult(i, 100);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// This method is called when the playlist fragment detects a song choice.
	@Override
	public void songPicked(int songIndex) {
		// Starting new intent
		Intent in = new Intent(getApplicationContext(),
				MusicPlayerActivity.class);
		// Sending songIndex to PlayerActivity
		in.putExtra("songIndex", songIndex);
		CurrentData.currentSongIndex = songIndex;
		CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(songIndex);
		
		// Save the new player state in Shared Prefs:
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Gson gson = new Gson();
		String songJson = gson.toJson(CurrentData.currentSong);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("currentSong", songJson);
		editor.putInt("currentSongIndex", CurrentData.currentSongIndex);
		editor.commit();


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
			ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.androsmusicplayer.PlaylistFragment"); 
			ft.commit(); 
			
			// Store the current playlist in system settings.
			Gson gson = new Gson();
			String currentPlaylistJson = gson.toJson(CurrentData.currentPlaylist);

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString("currentPlaylist", currentPlaylistJson);
			editor.commit();
			
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