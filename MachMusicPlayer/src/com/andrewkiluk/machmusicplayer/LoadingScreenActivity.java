package com.andrewkiluk.machmusicplayer;

import java.lang.reflect.Type;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


// This activity will only run the first time the application is opened.

public class LoadingScreenActivity extends Activity {

	private SharedPreferences sharedPrefs;
	private String oldsongsListJson;
	private LibraryFiller libFill;
	boolean mediaChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		CurrentData init = new CurrentData(); // Used to initialize static fields in CurrentData

		// Load oldCursorCount to check if the media on the device has changed
		int oldCursorCount = sharedPrefs.getInt("oldCursorCount", 0);

		// Compare oldCursorCount to current to see if 
		ContentResolver contentResolver = getApplicationContext().getContentResolver();
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		if(cursor.getCount() != oldCursorCount){
			mediaChanged = true;
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putInt("oldCursorCount", cursor.getCount());
			editor.commit();
		}

		// Load old songsList
		oldsongsListJson = sharedPrefs.getString("songsList", "null");
		if (!oldsongsListJson.equals("null") && !mediaChanged){
			loadOldSettings(getApplicationContext(), sharedPrefs);
			LibraryFiller libFill = new LibraryFiller(getApplicationContext());
			Intent i = new Intent(LoadingScreenActivity.this, MusicPlayerActivity.class);
			startActivity(i);
			finish();
		}
		else{
			setContentView(R.layout.loading_screen);
			new LoadLibrary().execute();
		}
	}

	public static void loadOldSettings(Context context, SharedPreferences sharedPrefs){
		// Do all of the library setup.
		Gson gson = new Gson();
		Type listType = new TypeToken<ArrayList<Song>>() {}.getType();
		LibraryFiller libFill = new LibraryFiller(context);
		LibraryInfo initializer = new LibraryInfo();

		// Parse oldsongsListJson
		String oldsongsListJson = sharedPrefs.getString("songsList", "null");
		LibraryInfo.songsList = gson.fromJson(oldsongsListJson, listType);
		libFill.buildLibraryFromSongsList();
//		Log.d("Library", "Old library Loaded");

		// Check if there is a stored current song; if so, load it.
		String oldSongJson = sharedPrefs.getString("currentSong", "null");
		if(!oldSongJson.equals("null")){
			CurrentData.currentSong = gson.fromJson(oldSongJson, Song.class);
		}
		else{
			CurrentData.currentSong = null;
		}

		// Load old song index
		CurrentData.currentSongIndex = sharedPrefs.getInt("currentSongIndex", 0);

		// Load old playlist position
		CurrentData.currentPlaylistPosition = sharedPrefs.getInt("currentPlaylistPosition", 0);

		// Check if there is a stored playlist; if so, load it, else initialize a new one.
		String oldPlaylistJson = sharedPrefs.getString("currentPlaylist", "null");

		if (!oldPlaylistJson.equals("null")){
			CurrentData.currentPlaylist = gson.fromJson(oldPlaylistJson, Playlist.class);
//			Log.d("Library", "Old playlist Loaded:" + oldPlaylistJson);
		}
		else{
			CurrentData.currentPlaylist = new Playlist();
		}

		// Load the list of stored playlists
		String oldPlaylistsJson = sharedPrefs.getString("playlists", "null");

		if (!oldPlaylistsJson.equals("null")){
			Type playlistType = new TypeToken<ArrayList<Playlist>>() {}.getType();
			LibraryInfo.playlists = gson.fromJson(oldPlaylistsJson, playlistType);
		}
		else{
			LibraryInfo.playlists = new ArrayList<Playlist>();
		}

		// Load old shuffle status
		String shuffleHistory = sharedPrefs.getString("shuffleHistory", "null");
		Type intArrayType = new TypeToken<ArrayList<Integer>>() {}.getType();
		gson = new Gson();
		if(!shuffleHistory.equals("null") ){
			CurrentData.shuffleHistory = gson.fromJson(shuffleHistory, intArrayType);
		}
		String shuffleQueue = sharedPrefs.getString("shuffleQueue", "null");
		if(!shuffleQueue.equals("null") ){
			Type arrayType = new TypeToken<int[]>() {}.getType();
			CurrentData.shuffleQueue = gson.fromJson(shuffleQueue, arrayType);
		}
		CurrentData.shuffleHistoryPosition = sharedPrefs.getInt("shuffleHistoryPosition", 0);

		// Indicate that everything has been loaded
		CurrentData.isInitialized = true;
	}

	/**
	 * Task which scans the SD card for music files and fills in the LibraryInfo class.  
	 */
	private class LoadLibrary extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(Void... arg0) {

			Log.d("Library", "No Library Loaded");
			LibraryFiller libFillAll = new LibraryFiller(getApplicationContext());
			libFillAll.loadLibrary();
			Gson gson = new Gson();
			String songsListJson = gson.toJson(LibraryInfo.songsList);
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString("songsList", songsListJson);
			editor.commit();

			// Look up the directory to search for music from the preferences activity
			//		Log.d("Library", "No Library Loaded");
			//		String library_location = sharedPrefs.getString("library_location", "null");
			//		LibraryFiller libFill = new LibraryFiller(library_location);
			//		if(libFill.loadLibrary() == -1){
			//			Toast.makeText(getApplicationContext(), "Invalid Library folder, could not load music.",
			//					Toast.LENGTH_LONG).show();
			//		}

			// Check if there is a stored playlist; if so, load it, else initialize a new one.
			String oldPlaylistJson = sharedPrefs.getString("currentPlaylist", "null");

			if (!oldPlaylistJson.equals("null")){
				CurrentData.currentPlaylist = gson.fromJson(oldPlaylistJson, Playlist.class);
				Log.d("Library", "Playlist Loaded");
			}
			else{
				CurrentData.currentPlaylist = new Playlist();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// Close this activity and launch MusicPlayerActivity
			Intent i = new Intent(LoadingScreenActivity.this, MusicPlayerActivity.class);
			startActivity(i);
			finish();
		}
	}
}