package com.andrewkiluk.simplemusicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class LibraryReloadActivity extends Activity { 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Library", "Library Reloaded");
		ArrayList<Song> currentPlaylist = LibraryInfo.currentPlaylist;
		LibraryFiller libFillAll = new LibraryFiller(getApplicationContext());
		libFillAll.loadLibrary();
		LibraryInfo.currentPlaylist = currentPlaylist;
		finish();

	}
}