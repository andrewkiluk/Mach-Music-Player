package com.andrewkiluk.machmusicplayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;


public class CurrentData {
	// Default constructor
	{
		currentSong = new Song("Song", "Album", "Artist", "NOPATH");
		shuffleHistory = new ArrayList<Integer>();
		currentPlaylist = new Playlist();
		shuffleQueue = new int[currentPlaylist.songs.size()];
	}

	public static Song currentSong;						// Song which is up for playing
	public static Playlist currentPlaylist;				// List of songs accessible by player controls
	public static int currentSongIndex;					// Represents the song position in the logical (possibly shuffled) playlist
	public static int currentPlaylistPosition;			// Represents the song position in the visible playlist
	public static ArrayList<Integer> shuffleHistory;	// Keeps track of which songs have been played in the current shuffle session so we can navigate back through them 
	public static int shuffleHistoryPosition;			// Keeps track of how many songs back into the shuffle history we currently are
	public static boolean shuffleHistoryFlag;			// Keeps track of whether we're in the shuffle history
	public static int shuffleQueue[];					// Holds song indices which have not yet been played in the current shuffle round
	public static boolean isInitialized;				// Indicates whether this class has been filled in upon loading

	public static void clearPlaylist(){
		currentPlaylist = new Playlist();
	}
	public static void shuffleReset(){
		shuffleHistory = new ArrayList<Integer>();
		shuffleHistoryPosition = 0;
		shuffleQueue = new int[currentPlaylist.songs.size()];
		for(int i = 0; i < currentPlaylist.songs.size(); i++){
			shuffleQueue[i] = 1;
		}
		if(PlayerOptions.isShuffle){
			CurrentData.currentSongIndex = 0;
			if(CurrentData.currentSong != null){
				// Add the current song to the shuffle history and remove it from the shuffle queue
				CurrentData.shuffleQueue[CurrentData.currentPlaylistPosition] = 0;
				CurrentData.shuffleHistory.add(CurrentData.currentPlaylistPosition);
			}
		}
	}
}


class PlayerStatus {
	public static boolean isVisible = false;
	public static boolean notification_set = false;
	public static boolean alarm_set = false;
	public static boolean playlistReset = false;
	public static boolean endReached = false;
	public static boolean playerReady = false;
	public static boolean timerReset = false;
}


class Album
{
	Album()
	{
		title = null;
		songs = new ArrayList<Song>();
		artist = null;
	}
	Album(String input)
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

class Artist
{
	Artist()
	{
		name = null;
		albums = new ArrayList<Album>();
	}
	Artist(String input)
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

class LibraryInfo
{
	LibraryInfo(){
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