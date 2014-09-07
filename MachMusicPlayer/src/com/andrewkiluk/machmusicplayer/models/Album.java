package com.andrewkiluk.machmusicplayer.models;

import java.util.ArrayList;

public class Album
{
	public Album()
	{
		title = null;
		songs = new ArrayList<Song>();
		artist = null;
	}
	public Album(String input)
	{
		title = input;
		songs = new ArrayList<Song>();
		artist = null;
	}
	public void addSong(Song song){
		songs.add(song);
	}
	public String title;
	public String artist;
	public ArrayList<Song> songs;
}