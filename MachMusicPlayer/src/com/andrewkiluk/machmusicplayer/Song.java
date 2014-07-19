package com.andrewkiluk.machmusicplayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;

 

public class Song implements Comparable<Song>
{
	public HashMap<String, String> songData;

	Song(Song oldCopy){
		HashMap<String, String> newCopy = new HashMap<String, String>();
		for (String key : oldCopy.songData.keySet()) {
			newCopy.put(key, oldCopy.songData.get(key));
		}
		this.songData = newCopy;
	}

	Song(String title, String album, String artist, String path){
		songData = new HashMap<String, String>();
		songData.put("songArtist", artist);
		songData.put("songAlbum", album);
		songData.put("songTitle", title);
		songData.put("songPath", path);
	}

	Song(HashMap<String, String> input){
		songData = input;
	}
	public int track(){
		String temp = songData.get("trackNumber");
		int number;
		try{
			if(temp.indexOf('/') > 0){
				number = Integer.parseInt(temp.substring(0, temp.indexOf('/')));
			}
			else{
				number = Integer.parseInt(temp);
			}
		} catch (NumberFormatException e){
			number = 0;
		}
		return number;
	}
	public String artist(){
		return songData.get("songArtist");
	}
	public String albumArtist(){
		if(songData.get("songAlbumArtist") != null){
			return songData.get("songAlbumArtist");
		}
		else{
			return songData.get("songArtist");
		}
	}
	public String path(){
		return songData.get("songPath");
	}
	public String duration(){
		return songData.get("Duration");
	}
	public String album(){
		return songData.get("songAlbum");
	}
	public String title(){
		return songData.get("songTitle");
	}
	public int compareTo(Song other)
	{
		return this.track() - other.track();
	}
	public boolean equals(Song other){
		if(this.title().equals(other.title() ) && this.artist().equals(other.artist() ) && this.album().equals(other.album() )){
			return true;
		}
		else{
			return false;
		}

	}
}


class TrackNumberComparator implements Comparator<Song>
{
	public int compare(Song first,
			Song second)
	{
		int firstnumber = first.track();
		int secondnumber = second.track();
		return firstnumber - secondnumber;
	}
}