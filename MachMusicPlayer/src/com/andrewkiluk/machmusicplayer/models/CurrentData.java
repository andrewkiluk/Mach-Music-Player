package com.andrewkiluk.machmusicplayer.models;

import java.util.ArrayList;


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
