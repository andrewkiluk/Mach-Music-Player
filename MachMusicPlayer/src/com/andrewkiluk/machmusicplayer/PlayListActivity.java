package com.andrewkiluk.machmusicplayer;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.andrewkiluk.machmusicplayer.MusicPlayerService.LocalBinder;
import com.google.gson.Gson;



public class PlayListActivity extends FragmentActivity implements PlaylistFragment.TouchListener {
	// Songs list
	public String library_location;

	private Button button_add_songs;
	private Button button_clear_playlist;
	private Button button_playlists;

	private MusicPlayerService mService;
	private boolean mBound;

	public int pxToDp(int px){
		return (int) (px / getResources().getDisplayMetrics().density);
	}

	public void playerReset(){
		if(mBound){
			mService.reset();
		}
	}

	public void getNextSong(){
		if(mBound){
			mService.getNextSong();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		PlaylistFragment playlistfragment = new PlaylistFragment(); 
		fragmentTransaction.add(R.id.playlist_container, playlistfragment);
		fragmentTransaction.commit();

		PlayerStatus.isVisible = true;

		button_add_songs = (Button) findViewById(R.id.button_add_songs);
		button_clear_playlist = (Button) findViewById(R.id.button_clear_playlist);
		button_playlists = (Button) findViewById(R.id.button_playlists);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int wt = displaymetrics.widthPixels;

		if(pxToDp(wt) < 380){
			button_add_songs.setPadding(10,0,10,0);
			button_clear_playlist.setPadding(10,0,10,0);
			button_playlists.setPadding(10,0,10,0);
		}
		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.tabcolor)));

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		library_location = sharedPrefs.getString("library_location", "null");


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
				if(mBound){
					mService.reset();
					CurrentData.currentSong = null;
					PlayerStatus.playerReady = false;
				}
				// Playlist has been modified, reset the shuffle queue
				CurrentData.shuffleReset();

				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction(); 
				PlaylistFragment refresh = new PlaylistFragment();
				ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.machmusicplayer.PlaylistFragment"); 
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

	@Override
	protected void onPause() {
		super.onPause();
		PlayerStatus.isVisible = false;

		// Save the new player state in Shared Prefs:
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Gson gson = new Gson();
		String songJson = gson.toJson(CurrentData.currentSong);
		String playlistJson = gson.toJson(CurrentData.currentPlaylist);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("currentSong", songJson);
		editor.putString("currentPlaylist", playlistJson);
		editor.putInt("currentSongIndex", CurrentData.currentSongIndex);
		editor.putInt("currentPlaylistPosition", CurrentData.currentPlaylistPosition);
		editor.commit();

		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PlayerStatus.isVisible = true;

		// Bind to the background service controlling the MediaPlayer object
		Intent i = new Intent(getApplicationContext(), MusicPlayerService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to MusicPlayerService, cast the IBinder and get MusicPlayerService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

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
		Intent in = new Intent(getApplicationContext(),	MusicPlayerActivity.class);

		// Sending songIndex to PlayerActivity
		in.putExtra("songIndex", songIndex);
		if (PlayerOptions.isShuffle){
			// The shuffle scheme is interrupted here, so reset the history and queue.
			CurrentData.shuffleReset();
		}
		else{
			CurrentData.currentSongIndex = songIndex;
		}
		CurrentData.currentPlaylistPosition = songIndex;
		CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(songIndex);



		setResult(100, in);
		// Closing PlayListView
		finish();
	}


	@Override
	protected void onStop() {
		PlayerStatus.isVisible = false;
		super.onStop();
	}

	@Override
	protected void onActivityResult(int requestCode,
			int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == 200){ // New playlist loaded

			// Clear the old song data and load the first song of the new playlist
			CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(0);
			CurrentData.currentSongIndex = 0;
			CurrentData.currentPlaylistPosition = 0;
			mService.loadCurrentSong();

		}
		if(resultCode == 300){ // New songs added

		}

		// We do the following actions in either case:

		// Update UI
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction(); 
		PlaylistFragment refresh = new PlaylistFragment();
		ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.machmusicplayer.PlaylistFragment"); 
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