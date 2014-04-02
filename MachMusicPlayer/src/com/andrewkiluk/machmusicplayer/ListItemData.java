package com.andrewkiluk.machmusicplayer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// A class to handle an item's id and name in a ListActivity
public class ListItemData {

	public int itemId;
	public String itemName;
	public Song song;

	// constructor
	public ListItemData(int itemId, String itemName, Song song) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.song = song;
	}
}
