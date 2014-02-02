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
 
public class SongsManager {
    // SDCard Path
    String baseDir = Environment.getExternalStorageDirectory().getPath();
    String RELATIVE_MEDIA_PATH;
    String MEDIA_PATH;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    
    
   
 // Constructor
    public SongsManager(){
        RELATIVE_MEDIA_PATH = "/amazonmp3/Daft_Punk";
        MEDIA_PATH = "file://" + baseDir + RELATIVE_MEDIA_PATH;
    }
    
 // Constructor
    public SongsManager(String library_location){
        RELATIVE_MEDIA_PATH = library_location;//"/amazonmp3/Daft_Punk";
        MEDIA_PATH = "file://" + baseDir + RELATIVE_MEDIA_PATH;
    }
    
    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(){
        try {
            File home = new File(new URI(MEDIA_PATH));
            loadFiles(home);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        } 
        
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
                    
                    
                    // Populate the hashmap with ID3 info.
                    mmr.setDataSource(filepath);
                    song.put("songPath", filepath);
                    song.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                    song.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                    song.put("songAlbum", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                    song.put("trackNumber", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
                    if (song.get("trackNumber") == null){
                        song.put("trackNumber", "0");
                    }
                    
                    // Adding each song to SongList
                    songsList.add(song);
                }
            }
            // Sort the list by track number.
            Collections.sort(songsList, new TrackComparator("trackNumber"));

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

class TrackComparator implements Comparator<Map<String, String>>
{
    private final String key;

    public TrackComparator(String key)
    {
        this.key = key;
    }

    public int compare(Map<String, String> first,
                       Map<String, String> second)
    {
        String firstnumber = first.get(key);
        String secondnumber = second.get(key);
        int firstValue;
        int secondValue;
        if(firstnumber.indexOf('/') > 0){
             firstValue = Integer.parseInt(firstnumber.substring(0, firstnumber.indexOf('/')));
        }
        else{
             firstValue = Integer.parseInt(firstnumber);
        }
        if(secondnumber.indexOf('/') > 0){
             secondValue = Integer.parseInt(secondnumber.substring(0, secondnumber.indexOf('/')));
        }
        else{
             secondValue = Integer.parseInt(secondnumber);
        }
        return firstValue - secondValue;
    }
}