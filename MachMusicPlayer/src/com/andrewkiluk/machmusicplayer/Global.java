package com.andrewkiluk.machmusicplayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;

 
class CurrentData {
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


class PlayerOptions	{
	// Default constructor
	PlayerOptions(){
		repeatMode = "OFF";
		isShuffle = false;
	}
	public static String repeatMode;
	public static boolean isShuffle;
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


class Song implements Comparable<Song>
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
		if(temp.indexOf('/') > 0){
			number = Integer.parseInt(temp.substring(0, temp.indexOf('/')));
		}
		else{
			number = Integer.parseInt(temp);
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

class Playlist{
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