package com.andrewkiluk.machmusicplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.andrewkiluk.machmusicplayer.Utilities;
import com.google.gson.Gson;
import com.mobeta.android.dslv.DragSortListView;

public class PlayListAdapter extends ArrayAdapter<Song> {

	Context context; 
    int layoutResourceId;    
    ArrayList<Song> data = null;
    Utilities utils;
    
    public PlayListAdapter(Context context, int layoutResourceId, ArrayList<Song> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.utils = new Utilities();
    }
    
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        SongHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new SongHolder();
            holder.listNumber = (TextView)row.findViewById(R.id.songNumber);
            holder.title = (TextView)row.findViewById(R.id.text);
            holder.time = (TextView)row.findViewById(R.id.time);
            
            row.setTag(holder);
        }
        else
        {
            holder = (SongHolder)row.getTag();
        }
        
        Song song = data.get(position);
        holder.listNumber.setText(Integer.toString(position + 1));
        holder.title.setText(song.title());
        try{
        holder.time.setText(utils.milliSecondsToTimer(Integer.parseInt(song.duration())));
        }
        catch(NumberFormatException e){
        	holder.time.setText((song.duration()));
        }
        
        return row;
    }
    
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
		
    }
    
    static class SongHolder
    {
    	TextView listNumber;
    	TextView title;
        TextView time;
    }

}