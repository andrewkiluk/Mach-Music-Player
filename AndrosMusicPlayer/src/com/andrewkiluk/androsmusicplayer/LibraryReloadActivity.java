package com.andrewkiluk.androsmusicplayer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class LibraryReloadActivity extends Activity { 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
	 * Async Task to make http call
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
			finish();


			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}