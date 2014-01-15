package com.andrewkiluk.simplemusicplayer;
 
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.widget.ImageView;
import android.widget.TextView;
 
public class SongsManager {
    // SDCard Path
    final String MEDIA_PATH = new String("/sdcard/media/audio/mp3");
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    byte[] art;
    private ImageView albumFrame;
    
    // Constructor
    public SongsManager(ImageView picture){
    	 albumFrame = picture;
    }
 
    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     * */
    public ArrayList<HashMap<String, String>> getPlayList(){
        File home = new File(MEDIA_PATH);
 
        if (home.listFiles(new FileExtensionFilter()).length > 0) {
            for (File file : home.listFiles(new FileExtensionFilter())) {
                HashMap<String, String> song = new HashMap<String, String>();
                String filepath= file.getPath();
                song.put("songPath", filepath);
               
                mmr.setDataSource(filepath);
                song.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                song.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                song.put("songAlbum", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                
                try {
                    art = mmr.getEmbeddedPicture();
                    Bitmap songImage = BitmapFactory
                            .decodeByteArray(art, 0, art.length);
                    albumFrame.setImageBitmap(songImage);
                    

                } catch (Exception e) {
                    e = null;
                }
                
                // Adding each song to SongList
                songsList.add(song);
            }
        }
        // return songs list array
        return songsList;
    }
 
    /**
     * Class to filter files which have an .mp3 extension
     * */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }
}