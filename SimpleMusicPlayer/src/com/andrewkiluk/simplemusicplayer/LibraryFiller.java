package com.andrewkiluk.simplemusicplayer;
 
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.preference.PreferenceManager;
 
public class LibraryFiller {
    // SDCard Path
    String baseDir = Environment.getExternalStorageDirectory().getPath();
    String RELATIVE_MEDIA_PATH;
    String MEDIA_PATH;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
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
    public void loadLibrary(){
        try {
            File home = new File(new URI(MEDIA_PATH));
            int test = home.listFiles().length;    // This is to trigger an exception in case the given directory is bad.
            loadFiles(home);
            for (Song song : LibraryInfo.songsList){
            	if (!LibraryInfo.artistsList.contains(song.artist())){
            		LibraryInfo.artistsList.add(new Artist(song.artist()));
            	}
            	if (!LibraryInfo.albumsList.contains(song.album())){
            		LibraryInfo.albumsList.add(new Album(song.album()));
            	}
            }
        }
        catch (Exception e) {
            songsList = null;
        } 
        
        // Sort the albums by track number.
        for (Album album : LibraryInfo.albumsList){
        	Collections.sort(album.songs);  // This may not be working, test!
        }
        
        
        
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
                    songData.put("trackNumber", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
                    if (songData.get("trackNumber") == null){
                        songData.put("trackNumber", "0");
                    }
                    
                    // Adding each song to SongList
                    LibraryInfo.songsList.add(new Song(songData));
                }
            }
            

            // This should add enumeration.
            int i=1;
            for(HashMap<String, String> entry : songsList){
                String newName = Integer.toString(i) + ") " + entry.get("songTitle");
                entry.put("playlistSongTitle", newName);
                i++;
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
	private HashMap<String, String> songData;
	
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
		songs = null;
	}
	Album(String input)
	{
		title = input;
		songs = null;
	}
	public String title;
	public ArrayList<String> songs;
}

class Artist
{
	Artist()
	{
		name = null;
		albums = null;
	}
	Artist(String input)
	{
		name = input;
		albums = null;
	}
	public String name;
	public ArrayList<Album> albums;
}

class LibraryInfo
{
	public static ArrayList<Song> songsList;
	public static ArrayList<Artist> artistsList;
	public static ArrayList<Album> albumsList;
}