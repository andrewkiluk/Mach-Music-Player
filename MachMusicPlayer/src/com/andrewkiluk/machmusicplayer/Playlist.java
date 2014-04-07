package com.andrewkiluk.machmusicplayer;

import java.util.ArrayList;

 

public class Playlist{
	Playlist(){
		this.songs = new ArrayList<Song>();
		this.name = "<No name>";
	}
	Playlist(Playlist playlist){
		Playlist newCopy = new Playlist(playlist.songs, playlist.name);
		this.songs = newCopy.songs;
		this.name = newCopy.name;
	}
	Playlist(ArrayList<Song> songs, String name){
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