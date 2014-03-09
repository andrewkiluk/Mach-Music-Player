package com.andrewkiluk.androsmusicplayer;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewkiluk.androsmusicplayer.MusicPlayerService.BoundServiceListener;
import com.andrewkiluk.androsmusicplayer.MusicPlayerService.LocalBinder;


public class MusicPlayerActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

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
	private  mp3Player mp;


	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();
	private Utilities utils;

	private MusicPlayerService mService;

	boolean mBound = false; // Tells whether activity is bound to background service.
	boolean firstBind = true; // Used to detect whether to set some things up upon binding.

	byte[] art;


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mBound){
			// Bind to MusicPlayerService
			Intent intent = new Intent(this, MusicPlayerService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Stop MusicPlayerService
		Intent i = new Intent(this, MusicPlayerService.class);
		stopService(i);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) { 
			// Activity was brought to front and not created, 
			// Thus finishing this will get us to the last viewed activity 
			finish(); 
			return; 
		} 
		setContentView(R.layout.player);

		AppStatus.isVisible = true;

		// Start the background service controlling the MediaPlayer object
		Intent i = new Intent(getApplicationContext(), MusicPlayerService.class);
		startService(i);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);

		// Change the action bar color        
		ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.tabcolor)));



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

		// mp3Player
		mp = new mp3Player();

		utils = new Utilities();

		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this); // Important


		// Do UI setup for the first item of the playlist
		try {

			int songIndex = 0;

			// Used to read ID3 tags.
			MediaMetadataRetriever acr = new MediaMetadataRetriever();

			final ImageView albumFrame = (ImageView) findViewById(R.id.albumFrame);

			// Displaying Song title
			String songTitle = LibraryInfo.currentPlaylist.songs.get(songIndex).songData.get("songTitle");
			String songArtist = LibraryInfo.currentPlaylist.songs.get(songIndex).songData.get("songArtist");
			String songAlbum = LibraryInfo.currentPlaylist.songs.get(songIndex).songData.get("songAlbum");
			songTitleLabel.setText(songTitle);
			songArtistLabel.setText(songArtist);
			songAlbumLabel.setText(songAlbum);

			try {
				acr.setDataSource(LibraryInfo.currentPlaylist.songs.get(songIndex).songData.get("songPath"));
				art = acr.getEmbeddedPicture();
				Bitmap songImage = BitmapFactory
						.decodeByteArray(art, 0, art.length);
				albumFrame.setImageBitmap(songImage);


			} catch (Exception e) {
				albumFrame.setImageResource(R.drawable.album);
			}

			// Updating progress bar
			updateProgressBar();
		} catch (Exception e) {
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
					if(!mp.isNull()){
						mp.pause();
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.ic_action_play);
						mService.createNotification(false);

					}
				}else{
					// Resume song
					if(!mp.isNull()){
						mp.start();
						// Changing button image to pause button
						btnPlay.setImageResource(R.drawable.ic_action_pause);
						mService.cancelAlarm();
						mService.createNotification(true);
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
				if(mBound){
					mService.playNext();
				}
			}
		});
		
		//
		//
		//
		//
		//
		//
		//  Should probably create a stack when shuffle is pressed and push songs when completed????
		//
		//
		//
		//
		//
		//
		//
		//
		
		/**
		 * Back button click event
		 * Plays previous song by currentSongIndex - 1
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mp.getCurrentPosition() > 3000){
					mService.playSong();
					updateSongUI(true);
				}else{
					if(LibraryInfo.currentSongIndex > 0){
						LibraryInfo.currentSongIndex = LibraryInfo.currentSongIndex - 1;
						mService.playSong();
						updateSongUI(true);

					}else{
						// play last song
						LibraryInfo.currentSongIndex = LibraryInfo.currentPlaylist.songs.size() - 1;
						mService.playSong();
						updateSongUI(true);

					}
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
				if(mService.getPlayerOptions().isRepeat){
					mService.getPlayerOptions().isRepeat = false;
					makeToast("Repeat is OFF");
					btnRepeat.setBackgroundResource(R.drawable.control_button);
				}else{
					// make repeat to true
					mService.getPlayerOptions().isRepeat = true;
					makeToast("Repeat is ON");
					// make shuffle to false
					mService.getPlayerOptions().isShuffle = false;
					btnRepeat.setBackgroundResource(R.drawable.control_button_selected);
					btnShuffle.setBackgroundResource(R.drawable.control_button);
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
				if(mService.getPlayerOptions().isShuffle){
					mService.getPlayerOptions().isShuffle = false;
					makeToast("Shuffle is OFF");
					btnShuffle.setBackgroundResource(R.drawable.control_button);
				}else{
					// make repeat to true
					mService.getPlayerOptions().isShuffle = true;
					makeToast("Shuffle is ON");

					// make shuffle to false
					mService.getPlayerOptions().isRepeat = false;
					btnShuffle.setBackgroundResource(R.drawable.control_button_selected);
					btnRepeat.setBackgroundResource(R.drawable.control_button);
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
	protected void onStop() {
		super.onStop();
		AppStatus.isVisible = false;
		// Set an alarm if not playing
		if (!mp.isPlaying()){
			if (mBound){
				mService.setAlarm();
			}
		}
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}


	}

	/**
	 * Establish a connection with the background service.
	 * */

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to MusicPlayerService, cast the IBinder and get MusicPlayerService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;

			binder.setListener(new BoundServiceListener() {

				@Override
				public void changeUIforSong(boolean isPlaying) {
					updateSongUI(isPlaying);

				}

				public void SetPlayButtonStatus(String status) {
					if (status == "pause"){
						btnPlay.setImageResource(R.drawable.ic_action_pause);
					}
					if (status == "play"){
						btnPlay.setImageResource(R.drawable.ic_action_play);
					}

				}

			});


			// Okay, now we can do the setup stuff that requires the MediaPlayer class to exist.

			try {
				if(firstBind){
					int songIndex = 0;
					mp.reset();
					mp.setDataSource(LibraryInfo.currentPlaylist.songs.get(songIndex).songData.get("songPath"));
					mp.prepare();

					mService.updateCurrentSong();

					mService.createNotification(false);

					firstBind = false;
				}
			}catch (Exception e) {

			}			

			mService.cancelAlarm();
			if (mp.isPlaying()){
				mService.createNotification(true);
			}
			else{
				mService.createNotification(false);
			}

//			PlayerStatus ps = mService.getPlayerStatus();
			PlayerStatus.notification_set = true;
			PlayerStatus.alarm_set = false;


		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	/**
	 * The following two methods are to set up the Settings menu.
	 * */

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
			LibraryInfo.currentSongIndex = data.getExtras().getInt("songIndex");
			// update UI for new song
			updateSongUI(true);
			// Update data in MusicPlayerService and play selected song.
			mService.playSong();

		}

	}




	/**
	 * Function to play a song
	 * @param songIndex - index of song
	 * */
	public void updateSongUI(boolean isPlaying){
		// Play song
		try {

			// Used to read ID3 tags.
			MediaMetadataRetriever acr = new MediaMetadataRetriever();

			final ImageView albumFrame = (ImageView) findViewById(R.id.albumFrame);

			// Displaying Song title
			String songTitle = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songTitle");
			String songArtist = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songArtist");
			String songAlbum = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songAlbum");
			songTitleLabel.setText(songTitle);
			songArtistLabel.setText(songArtist);
			songAlbumLabel.setText(songAlbum);

			try {
				acr.setDataSource(LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songPath"));
				art = acr.getEmbeddedPicture();
				Bitmap songImage = BitmapFactory
						.decodeByteArray(art, 0, art.length);
				albumFrame.setImageBitmap(songImage);


			} catch (Exception e) {
				albumFrame.setImageResource(R.drawable.album);
			}

			// Changing Button Image to correct image
			if (isPlaying){
				btnPlay.setImageResource(R.drawable.ic_action_pause);
			}
			else{
				btnPlay.setImageResource(R.drawable.ic_action_play);
			}


			// set Progress bar values
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);




			// Updating progress bar
			updateProgressBar();
		} catch (Exception e) {

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
			try{

				long totalDuration = mp.getDuration();
				long currentDuration = mp.getCurrentPosition();

				// Displaying Total Duration time
				songTotalDurationLabel.setText(String.valueOf(utils.milliSecondsToTimer(totalDuration)));
				// Displaying time completed playing
				songCurrentDurationLabel.setText(String.valueOf(utils.milliSecondsToTimer(currentDuration)));

				// Updating progress bar
				int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
				//Log.d("Progress", ""+progress);
				songProgressBar.setProgress(progress);

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, 100);
			}catch(NullPointerException e){

			}catch(IllegalStateException e){

			}

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
		try{
			mHandler.removeCallbacks(mUpdateTimeTask);
			int totalDuration = mp.getDuration();
			int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

			// forward or backward to certain seconds
			mp.seekTo(currentPosition);

			// update timer progress again
			updateProgressBar();
		} catch(IllegalStateException e){

		}

	}

	public void makeToast(String string){
		final Toast toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
		toast.show();
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				toast.cancel(); 
			}
		}, 500);

	}


	class mp3Player
	{

		// This class acts as a mediator between all of the front-end stuff in this activity with all the back-end stuff going on in MusicPlayerService.
		// It just mimics the API for MediaPlayer and sends the corresponding actions to the actual MediaPlayer object in the service.

		public boolean isNull() {
			if (mBound) {
				return mService.isNull();
			}
			else return true;
		}

		public void prepare()
		{
			if (mBound) {
				mService.prepare();	            
			}
		}

		public void reset()
		{
			if (mBound) {
				mService.reset();	            
			}
		}

		public void setDataSource(String input)
		{
			if (mBound) {
				mService.setDataSource(input);	            
			}
		}

		public void start()
		{
			if (mBound) {
				mService.start();	            
			}
		}

		public void play(String songPath)
		{
			if (mBound) {
				mService.play(songPath);	            
			}
		}

		public boolean isPlaying()
		{
			if (mBound) {
				return mService.isPlaying();	            
			}
			else return false;
		}

		public void pause()
		{
			if (mBound) {
				mService.pause();	            
			}
		}

		public void release()
		{
			if (mBound) {
				mService.release();	            
			}
		}

		public void seekTo(int currentPosition)
		{
			if (mBound) {
				mService.seekTo(currentPosition);	            
			}
		}
		public int getDuration()
		{
			if (mBound) {
				return mService.getDuration();	            
			}
			else{
				return 0;
			}
		}
		public int getCurrentPosition()
		{
			if (mBound) {
				return mService.getCurrentPosition();	            
			}
			else{
				return 0;
			}
		}



	}


}

class AppStatus {
	public static boolean isVisible;
}













