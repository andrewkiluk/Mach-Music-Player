package com.andrewkiluk.machmusicplayer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

import com.google.gson.Gson;

public class LibraryReloadActivity extends Activity { 

	private SharedPreferences sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.x = 0;
		params.height = 100;
		params.width = 250;
		params.y = 0;
		
		

		this.getWindow().setAttributes(params);

		setContentView(R.layout.library_reloader);  

		new ReloadLibrary().execute();

	}




	/**
	 * Reload and rebuild library of songs
	 */
	private class ReloadLibrary extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(Void... arg0) {

			Playlist currentPlaylist = LibraryInfo.currentPlaylist;
			LibraryFiller libFillAll = new LibraryFiller(getApplicationContext());
			libFillAll.loadLibrary();
			LibraryInfo.currentPlaylist = currentPlaylist;
			
			Gson gson = new Gson();
			String songsListJson = gson.toJson(LibraryInfo.songsList);
			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString("songsList", songsListJson);
			editor.commit();
			finish();


			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}