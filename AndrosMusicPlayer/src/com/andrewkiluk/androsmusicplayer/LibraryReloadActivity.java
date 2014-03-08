package com.andrewkiluk.androsmusicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class LibraryReloadActivity extends Activity { 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Library", "Library Reloaded");
		Playlist currentPlaylist = LibraryInfo.currentPlaylist;
		LibraryFiller libFillAll = new LibraryFiller(getApplicationContext());
		libFillAll.loadLibrary();
		LibraryInfo.currentPlaylist = currentPlaylist;
		finish();

	}
}