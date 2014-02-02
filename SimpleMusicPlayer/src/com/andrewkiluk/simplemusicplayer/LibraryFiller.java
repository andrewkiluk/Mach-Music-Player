package com.andrewkiluk.simplemusicplayer;
 
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
 
public class LibraryFiller {
    // SDCard Path
	String baseDir = Environment.getExternalStorageDirectory().getPath();
	String RELATIVE_MEDIA_PATH;
    String MEDIA_PATH;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private Set<String> artistsList = new TreeSet<String>();
    private Set<String> albumsList = new TreeSet<String>();
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    
    // Constructor
    public LibraryFiller(){
    	RELATIVE_MEDIA_PATH = "/media/audio/mp3";// Should be "@string/music_directory", but this crashes it...!?
    	MEDIA_PATH = baseDir + RELATIVE_MEDIA_PATH;
    }
 
    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(){
        File home = new File(MEDIA_PATH);
        
        loadFiles(home);

        // return songs list array
        return songsList;
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
	            	
            		HashMap<String, String> song = new HashMap<String, String>();
	                String filepath= file.getPath();
	                song.put("songPath", filepath);
	                
	                mmr.setDataSource(filepath);
	                String songArtist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
	                String songAlbum = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
	                song.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
	                song.put("songArtist", songArtist);
	                song.put("songAlbum", songAlbum);
	                artistsList.add(songArtist);
	                albumsList.add(songAlbum);
	                
	                // Adding each song to SongList
	                songsList.add(song);
            	}
            }
        }
    }
    
}