package com.andrewkiluk.simplemusicplayer;

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

	// constructor
	public ObjectItem(int itemId, String itemName) {
		this.itemId = itemId;
		this.itemName = itemName;
	}

}


