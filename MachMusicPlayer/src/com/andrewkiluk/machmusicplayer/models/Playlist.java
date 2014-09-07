package com.andrewkiluk.machmusicplayer.models;

import java.util.ArrayList;

public class Playlist{
	public Playlist(){
		this.songs = new ArrayList<Song>();
		this.name = "<No name>";
	}
	public Playlist(Playlist playlist){
		Playlist newCopy = new Playlist(playlist.songs, playlist.name);
		this.songs = newCopy.songs;
		this.name = newCopy.name;
	}
	public Playlist(ArrayList<Song> songs, String name){
		ArrayList<Song> newSongs = new ArrayList<Song>();
		for(Song song  : songs){
			Song newSong = new Song(song);
			newSongs.add(newSong);
		}
		this.songs = newSongs;
		this.name = name;
	}

	public ArrayList<Song> songs;
	public String name;
}