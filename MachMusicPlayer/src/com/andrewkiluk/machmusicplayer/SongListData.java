package com.andrewkiluk.machmusicplayer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// A class to handle an item's id and name in a ListActivity
public class SongListData {

	public int itemId;
	public String itemName;
	public Song song;
	public String songDuration;

	// constructor
	public SongListData(int itemId, String itemName, Song song, String songDuration) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.song = song;
		this.songDuration = songDuration;
	}
}
