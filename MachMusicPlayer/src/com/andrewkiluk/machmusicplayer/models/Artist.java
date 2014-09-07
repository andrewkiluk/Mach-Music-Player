package com.andrewkiluk.machmusicplayer.models;

import java.util.ArrayList;

public class Artist
{
	public Artist()
	{
		name = null;
		albums = new ArrayList<Album>();
	}
	public Artist(String input)
	{
		name = input;
		albums = new ArrayList<Album>();
	}
	public void addAlbum(Album album){
		albums.add(album);
	}
	public String name;
	public ArrayList<Album> albums;
}