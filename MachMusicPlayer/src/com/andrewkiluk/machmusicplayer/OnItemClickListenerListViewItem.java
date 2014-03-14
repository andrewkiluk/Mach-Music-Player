package com.andrewkiluk.machmusicplayer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/*
 * Here you can control what to do next when the user selects an item
 */
public class OnItemClickListenerListViewItem implements OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Context context = view.getContext();
        
        TextView textViewItem = ((TextView) view.findViewById(R.id.label));
        
        Log.d("List","Click a doodle doo!");
        
    }
    
}