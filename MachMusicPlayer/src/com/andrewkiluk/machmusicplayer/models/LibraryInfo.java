package com.andrewkiluk.machmusicplayer.models;

import java.util.ArrayList;

public class LibraryInfo
{
	public LibraryInfo(){
		songsList = new ArrayList<Song>();
		newSongs = new ArrayList<Song>();
		artistsList = new ArrayList<Artist>();
		albumsList = new ArrayList<Album>();
		isInitialized = true;
		currentSongIndex = 0;
		currentPlaylist = new Playlist(new ArrayList<Song>(), "__CURRENT_PLAYLIST__");
		playlists = new ArrayList<Playlist>();
	}
	public static void clearPlaylist(){
		currentPlaylist = new Playlist();
	}
	public static boolean isInitialized = false;
	public static ArrayList<Song> songsList;
	public static Playlist currentPlaylist;
	public static int currentSongIndex;
	public static ArrayList<Playlist> playlists;
	public static ArrayList<Song> newSongs;
	public static ArrayList<Artist> artistsList;
	public static ArrayList<Album> albumsList;
}