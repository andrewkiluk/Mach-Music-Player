package com.andrewkiluk.simplemusicplayer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.media.MediaMetadataRetriever;
import android.os.Environment;

public class LibraryFiller {
	// SDCard Path
	String baseDir = Environment.getExternalStorageDirectory().getPath();
	String RELATIVE_MEDIA_PATH;
	String MEDIA_PATH;
	MediaMetadataRetriever mmr = new MediaMetadataRetriever();


	// Constructor
	public LibraryFiller(String library_location){
		RELATIVE_MEDIA_PATH = library_location;
		MEDIA_PATH = "file://" + baseDir + RELATIVE_MEDIA_PATH;
	}

	/**
	 * Function to read all mp3 files from sdcard
	 * and store the details in the LibraryInfo class.
	 * */
	public int loadLibrary(){
		boolean albumFound;
		boolean artistFound;
		File home;
		try {
			home = new File(new URI(MEDIA_PATH));
			int test = home.listFiles().length;     // This is to test that the directory is valid.
		}
		catch (Exception e) {
			return -1;
		} 




		LibraryInfo libInfo = new LibraryInfo(); // We need to create this object in order for the static objects in the class to get initialized.
		loadFiles(home);


		for (Song song : LibraryInfo.songsList){
			artistFound = false;
			for ( Artist currentArtist : LibraryInfo.artistsList){   // Add new artists to the artists list.
				if(currentArtist.name.equals( song.artist() ) ){
					artistFound = true;
					break;
				}
			}
			if (!artistFound){
				LibraryInfo.artistsList.add( new Artist( song.artist() ) );
				artistFound = false;
			}
			// If the album is not new, add the current song to the corresponding album object.
			albumFound = false;
			for(Album currentAlbum : LibraryInfo.albumsList){
				if(currentAlbum.title.equals( song.album() ) && currentAlbum.artist.equals( song.albumArtist() ) ){
					currentAlbum.addSong(song);
					albumFound = true;
					break;
				}
			}
			if(!albumFound){   // Album was not among current albums, so create a new album object.
				Album newAlbum = new Album(song.album());
				newAlbum.addSong(song);
				newAlbum.title = song.album();
				newAlbum.artist = song.albumArtist();
				LibraryInfo.albumsList.add(newAlbum);
			}


		}
		// Now we fill in all the Album lists in the Artist objects.

		for(Album currentAlbum : LibraryInfo.albumsList){
			for ( Artist currentArtist : LibraryInfo.artistsList){
				if(currentAlbum.artist.equals( currentArtist.name ) ){
					currentArtist.addAlbum(currentAlbum);
					break;
				}
			}
		}


		// Sort the album tracks.
		for (Album album : LibraryInfo.albumsList){
			Collections.sort(album.songs);  // This may not be working, test!
		}
		// Sort songs alphabetically
		Collections.sort(LibraryInfo.songsList, new Comparator<Song>(){
			public int compare(Song one, Song two) {
				return one.songData.get("songTitle").compareTo(two.songData.get("songTitle"));
			}
		});
		// Sort artists alphabetically
		Collections.sort(LibraryInfo.artistsList, new Comparator<Artist>(){
			public int compare(Artist one, Artist two) {
				return one.name.compareTo(two.name);
			}
		});
		// Sort albums alphabetically
		Collections.sort(LibraryInfo.albumsList, new Comparator<Album>(){
			public int compare(Album one, Album two) {
				if ( one.title.compareTo(two.title) != 0){
					return one.title.compareTo(two.title);
				}
				else {
					return one.artist.compareTo(two.artist);
				}
			}
		});

		return 0;

	}

	public void loadFiles(File currentDirectory){
		if (currentDirectory.listFiles().length > 0) {

			for (File file : currentDirectory.listFiles()) {

				// Recursively calls itself on subdirectories
				if(file.isDirectory()){
					loadFiles(file);
				}
				else if (file.getName().endsWith(".mp3") || file.getName().endsWith(".MP3")){
					// Checks if it has found an mp3, then loads data for the mp3 upon success.

					HashMap<String, String> songData = new HashMap<String, String>();
					String filepath= file.getPath();


					// Populate the hashmap with ID3 info.
					mmr.setDataSource(filepath);
					songData.put("songPath", filepath);
					songData.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
					songData.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
					songData.put("songAlbum", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
					songData.put("songAlbumArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
					songData.put("trackNumber", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
					if (songData.get("trackNumber") == null){
						songData.put("trackNumber", "0");
					}

					// Adding each song to SongList
					LibraryInfo.songsList.add(new Song(songData));
				}
			}
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


class Song implements Comparable<Song>
{
	public HashMap<String, String> songData;

	Song(String title, String artist, String album){
		songData.put("songArtist", artist);
		songData.put("songAlbum", album);
		songData.put("songTitle", title);
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
		currentPlaylist = new ArrayList<Song>();
		isInitialized = true;
	}
	public static void clearPlaylist(){
		currentPlaylist = new ArrayList<Song>();
	}
	public static boolean isInitialized = false;
	public static ArrayList<Song> songsList;
	public static ArrayList<Song> currentPlaylist;
	public static ArrayList<Song> newSongs;
	public static ArrayList<Artist> artistsList;
	public static ArrayList<Album> albumsList;
}