package com.andrewkiluk.simplemusicplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MusicPlayerActivity extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {
	 
    private ImageButton btnPlay;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songArtistLabel;
    private TextView songAlbumLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    private SongsManager songManager;
    private Utilities utils;
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    
    byte[] art;
    
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2b2b3b")));
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String library_location = sharedPrefs.getString("library_location", "NULL");


 
        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.playButton);
        btnNext = (ImageButton) findViewById(R.id.nextButton);
        btnPrevious = (ImageButton) findViewById(R.id.previousButton);
        btnPlaylist = (ImageButton) findViewById(R.id.playlistButton);
        btnRepeat = (ImageButton) findViewById(R.id.repeatButton);
        btnShuffle = (ImageButton) findViewById(R.id.shuffleButton);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songArtistLabel = (TextView) findViewById(R.id.songArtist);
        songAlbumLabel = (TextView) findViewById(R.id.songAlbum);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
 
        // Mediaplayer
        mp = new MediaPlayer();
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        
               
        // These create instances of classes from the other files.
        songManager = new SongsManager(library_location);
        utils = new Utilities();
 
        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important
 
        // Getting all songs list
        songsList = songManager.getPlayList();
        
        try {
        	int songIndex = 0;
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
            mp.prepare();
            
            // Used to read ID3 tags.
            MediaMetadataRetriever acr = new MediaMetadataRetriever();
            
            final ImageView albumFrame = (ImageView) findViewById(R.id.albumFrame);
            
            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            String songArtist = songsList.get(songIndex).get("songArtist");
            String songAlbum = songsList.get(songIndex).get("songAlbum");
            songTitleLabel.setText(songTitle);
            songArtistLabel.setText(songArtist);
            songAlbumLabel.setText(songAlbum);
            
            try {
            	acr.setDataSource(songsList.get(songIndex).get("songPath"));
                art = acr.getEmbeddedPicture();
                Bitmap songImage = BitmapFactory
                        .decodeByteArray(art, 0, art.length);
                albumFrame.setImageBitmap(songImage);
                

            } catch (Exception e) {
            	albumFrame.setImageResource(R.drawable.album);
            }
 
            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
 
        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // check for already playing
                if(mp.isPlaying()){
                    if(mp!=null){
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.play_button);
                    }
                }else{
                    // Resume song
                    if(mp!=null){
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.pause_button);
                    }
                }
 
            }
        });
 
              
        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if(currentSongIndex < (songsList.size() - 1)){
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                }else{
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }
                
                
 
            }
        });
 
        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                if(currentSongIndex > 0){
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                }else{
                    // play last song
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }
 
            }
        });
 
        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                if(isRepeat){
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.repeat_button_off);
                }else{
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.repeat_button_on);
                    btnShuffle.setImageResource(R.drawable.shuffle_button_off);
                }
            }
        });
 
        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                if(isShuffle){
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.shuffle_button_off);
                }else{
                    // make repeat to true
                    isShuffle= true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.shuffle_button_on);
                    btnRepeat.setImageResource(R.drawable.repeat_button_off);
                }
            }
        });
 
        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
        btnPlaylist.setOnClickListener(new View.OnClickListener() {
 
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
                startActivityForResult(i, 100);
            }
        });
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.menu, menu);
        return true;
    }
     
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_settings:
            	Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(i, 100);
                return true;
            default:
                  return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    protected void onActivityResult(int requestCode,
                                     int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
             currentSongIndex = data.getExtras().getInt("songIndex");
             // play selected song
             playSong(currentSongIndex);
        }
 
    }
    
    
 
    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
    public void playSong(int songIndex){
        // Play song
         try {
            mp.reset();
            mp.setDataSource(songsList.get(songIndex).get("songPath"));
            mp.prepare();
            mp.start();
            
            // Used to read ID3 tags.
            MediaMetadataRetriever acr = new MediaMetadataRetriever();
            
            final ImageView albumFrame = (ImageView) findViewById(R.id.albumFrame);
            
            // Displaying Song title
            String songTitle = songsList.get(songIndex).get("songTitle");
            String songArtist = songsList.get(songIndex).get("songArtist");
            String songAlbum = songsList.get(songIndex).get("songAlbum");
            songTitleLabel.setText(songTitle);
            songArtistLabel.setText(songArtist);
            songAlbumLabel.setText(songAlbum);
            
            try {
            	acr.setDataSource(songsList.get(songIndex).get("songPath"));
                art = acr.getEmbeddedPicture();
                Bitmap songImage = BitmapFactory
                        .decodeByteArray(art, 0, art.length);
                albumFrame.setImageBitmap(songImage);
                

            } catch (Exception e) {
            	albumFrame.setImageResource(R.drawable.album);
            }
 
            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.pause_button);
 
            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);
            
            
            
 
            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
 
    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }   
 
    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
           public void run() {
               long totalDuration = mp.getDuration();
               long currentDuration = mp.getCurrentPosition();
 
               // Displaying Total Duration time
               songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
               // Displaying time completed playing
               songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
 
               // Updating progress bar
               int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
               //Log.d("Progress", ""+progress);
               songProgressBar.setProgress(progress);
 
               // Running this thread after 100 milliseconds
               mHandler.postDelayed(this, 100);
           }
        };
 
    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
 
    }
 
    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
 
    /**
     * When user stops moving the progress handler
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
 
        // forward or backward to certain seconds
        mp.seekTo(currentPosition);
 
        // update timer progress again
        updateProgressBar();
    }
 
    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     * */
    @Override
    public void onCompletion(MediaPlayer arg0) {
 
        // check for repeat is ON or OFF
        if(isRepeat){
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if(isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongIndex < (songsList.size() - 1)){
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            }else{
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }
 
    @Override
     public void onDestroy(){
     super.onDestroy();
        mp.release();
     }
 
}