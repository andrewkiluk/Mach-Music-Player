package com.andrewkiluk.androsmusicplayer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

//another class to handle item's id and name
public class ObjectItem {

	public int itemId;
	public String itemName;
	public Song song;

	// constructor
	public ObjectItem(int itemId, String itemName, Song song) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.song = song;
	}

}


