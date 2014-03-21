package com.andrewkiluk.machmusicplayer;

import java.util.Collections;
import java.util.Comparator;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;



public class PlayListManagerActivity extends FragmentActivity implements PlaylistManagerFragment.TouchListener {
	// Songs list
	public String library_location;
	boolean unique;

	private Button button_save_playlist;

	private void nameError(){

		new AlertDialog.Builder(PlayListManagerActivity.this)
		.setTitle("Name Conflict")
		.setMessage("Choose a name which isn't already in use.")
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist_manager);

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		PlaylistManagerFragment playlistfragment = new PlaylistManagerFragment(); 
		fragmentTransaction.add(R.id.playlists_container, playlistfragment);
		fragmentTransaction.commit();

		AppStatus.isVisible = true;

		button_save_playlist = (Button) findViewById(R.id.button_save_playlist);

		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.tabcolor)));
		
		button_save_playlist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction(); 


				// Open a dialogue box, check that the name is unique, save the playlist to LibraryInfo. 

				final EditText input = new EditText(PlayListManagerActivity.this);


				new AlertDialog.Builder(PlayListManagerActivity.this)
				.setTitle("Save Current Playlist")
				.setMessage("Enter a name for your playlist:")
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Editable value = input.getText(); 
						String newName = value.toString();
						unique = true;
						for( Playlist playlist : LibraryInfo.playlists){
							if(playlist.name.equals(newName)){
								unique = false;
							}
						}
						if(unique){

							Playlist newPlaylist = new Playlist(CurrentData.currentPlaylist.songs, newName);

							LibraryInfo.playlists.add(newPlaylist);

							// Sort playlists alphabetically
							Collections.sort(LibraryInfo.playlists, new Comparator<Playlist>(){
								public int compare(Playlist one, Playlist two) {
									return one.name.compareTo(two.name);
								}
							});
							
						}


						PlaylistManagerFragment refresh = new PlaylistManagerFragment();
						ft.replace(R.id.playlists_container, refresh, "com.andrewkiluk.machmusicplayer.PlaylistManagerFragment"); 
						ft.commit(); 
						if(!unique){
							nameError();
						}


					}

				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();

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

	// This method is called when the playlist manager fragment detects a playlist choice.
	@Override
	public void playlistPicked(int position) {

		// Starting new intent
		Intent in = new Intent(getApplicationContext(),
				PlayListActivity.class);
		// Sending playlist name to PlayerActivity
		in.putExtra("playlistName", LibraryInfo.playlists.get(position).name);
		setResult(200, in); // 200 tells the playlist activity to refresh the playlist fragment
		
		// Playlist has been modified, reset the shuffle queue
		CurrentData.shuffleReset();
		
		// Closing PlayListView
		finish();
	}


	@Override
	protected void onStop() {
		AppStatus.isVisible = false;

		// Store the current list of playlists in system settings.
		Gson gson = new Gson();
		String playlistsJson = gson.toJson(LibraryInfo.playlists);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("playlists", playlistsJson);
		editor.commit();

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
			ft.replace(R.id.playlist_container, refresh, "com.andrewkiluk.machmusicplayer.PlaylistFragment"); 
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