package com.andrewkiluk.machmusicplayer;

import java.util.concurrent.atomic.AtomicBoolean;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewkiluk.machmusicplayer.MusicPlayerService.BoundServiceListener;
import com.andrewkiluk.machmusicplayer.MusicPlayerService.LocalBinder;


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
	private TextView repeat_once_text;

	private int albumArtSize;

	AtomicBoolean updateTimeThreadLock;

	// Media Player
	private  mp3Player mp;


	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();
	private Utilities utils;

	private MusicPlayerService mService;

	SharedPreferences sharedPrefs;

	boolean mBound = false; // Tells whether activity is bound to background service.
	boolean firstBind = true; // Used to detect whether to set some things up upon binding.

	long totalDuration = 0;
	long currentDuration = 0;

	byte[] art;
	private Bitmap songImage;


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
		updateTimeThreadLock = new AtomicBoolean(false);

	}

	public int pxToDp(int px){
		return (int) (px / getResources().getDisplayMetrics().density);
	}


	public int dpToPx(int dp){
		return (int) (dp * getResources().getDisplayMetrics().density);
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

		PlayerStatus.isVisible = true;

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
		repeat_once_text = (TextView)findViewById(R.id.repeat_once_text);

		// mp3Player
		mp = new mp3Player();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		utils = new Utilities();

		// Load saved preferences
		PlayerOptions.isShuffle = sharedPrefs.getBoolean("isShuffle", false);
		PlayerOptions.repeatMode = sharedPrefs.getString("repeatMode", "OFF");

		if(PlayerOptions.repeatMode.equals("OFF")){
			btnRepeat.setBackgroundResource(R.drawable.control_button);
			repeat_once_text.setTextColor(Color.argb(0,255,255,255));
		}else if(PlayerOptions.repeatMode.equals("SONG")){
			repeat_once_text.setTextColor(Color.argb(180,255,255,255));
			btnRepeat.setBackgroundResource(R.drawable.control_button_selected);
		}else{
			repeat_once_text.setTextColor(Color.argb(0,255,255,255));
			btnRepeat.setBackgroundResource(R.drawable.control_button_selected);
		}

		if(PlayerOptions.isShuffle){
			btnShuffle.setBackgroundResource(R.drawable.control_button_selected);
		}

		// Check to see if the CurrentData class needs to be loaded
		if (!CurrentData.isInitialized){
			LoadingScreenActivity.loadOldSettings(getApplicationContext(), sharedPrefs);
		}

		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this); // Important

		// Do UI setup based on screen size

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int ht = displaymetrics.heightPixels;
		int wt = displaymetrics.widthPixels;

		// Adjust width and padding for the control buttons based on screen size.

		int footerHeight = 100;
		if(pxToDp(wt) * 75 / 384 < 100){
			footerHeight = pxToDp(wt) * 75 / 384;
		}
		int spaceUnit =  dpToPx((int)(pxToDp(wt) * 20.0 / 384));
		if (spaceUnit > 30){
			spaceUnit = 30;
		}
		int paddingUnit =  dpToPx((int)(pxToDp(wt) * 8.0 / 384));
		if(paddingUnit > 10){
			paddingUnit = 10;
		}

		RelativeLayout footerView = (RelativeLayout) findViewById(R.id.player_footer);
		RelativeLayout.LayoutParams footerParams = (RelativeLayout.LayoutParams) footerView .getLayoutParams();
		footerParams.height = dpToPx(footerHeight);
		footerView .setLayoutParams(footerParams);

		btnPrevious.setPadding( (int) (1.5 * paddingUnit), paddingUnit, (int) (1.5 * paddingUnit), paddingUnit);
		btnPlay.setPadding( (int) (1.5 * paddingUnit), (int) (2.2 * paddingUnit), (int) (1.5 * paddingUnit), (int) (2.2 * paddingUnit));
		btnNext.setPadding( (int) (1.5 * paddingUnit), paddingUnit, (int) (1.5 * paddingUnit), paddingUnit);

		ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) btnNext.getLayoutParams();
		p.setMargins(spaceUnit, 0, 0, 0);

		p = (ViewGroup.MarginLayoutParams) btnPrevious.getLayoutParams();
		p.setMargins(0, 0, spaceUnit, 0);

		p = (ViewGroup.MarginLayoutParams) btnRepeat.getLayoutParams();
		p.setMargins((int) (1.5 * spaceUnit), 0, 0, 0);

		p = (ViewGroup.MarginLayoutParams) btnShuffle.getLayoutParams();
		p.setMargins(0, 0, (int) (1.5 * spaceUnit), 0);


		// Calculate StatusBar height
		int statusBarHeight = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}

		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		int actionBarHeight = 0;
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		}

		// Compute the album art size based on screen dimensions
		if(ht - statusBarHeight - actionBarHeight - dpToPx(155) > wt){
			albumArtSize = ht - statusBarHeight - actionBarHeight - dpToPx(155) ;
		}
		else{
			albumArtSize = wt; 
		}



		// Now we set the album art for the first song
		MediaMetadataRetriever acr = new MediaMetadataRetriever();
		final ImageView albumFrame = (ImageView) findViewById(R.id.albumFrame);
		try {

			// Displaying Song title
			String songTitle = CurrentData.currentSong.songData.get("songTitle");
			String songArtist = CurrentData.currentSong.songData.get("songArtist");
			String songAlbum = CurrentData.currentSong.songData.get("songAlbum");
			songTitleLabel.setText(songTitle);
			songArtistLabel.setText(songArtist);
			songAlbumLabel.setText(songAlbum);

			LayoutParams params = albumFrame.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = albumArtSize;
			params.width = albumArtSize;

			acr.setDataSource(CurrentData.currentSong.songData.get("songPath"));
			art = acr.getEmbeddedPicture();
			songImage = BitmapFactory
					.decodeByteArray(art, 0, art.length);

			songImage = Bitmap.createScaledBitmap(songImage, albumArtSize, albumArtSize, false);


			albumFrame.setImageBitmap(songImage);
		} catch (Exception e) {
			albumFrame.setImageResource(android.R.color.transparent);

			LayoutParams params = albumFrame.getLayoutParams();
			// Changes the height and width to the specified *pixels*
			params.height = albumArtSize;
			params.width = albumArtSize;
		}
		// Updating progress bar
		updateProgressBar();





		/**
		 * Play button click event.
		 * Toggles play state and updates the graphic on the play / pause button.
		 * */
		btnPlay.setOnClickListener(new View.OnClickListener() {


			@Override
			public void onClick(View arg0) {
				if(CurrentData.currentSong == null && CurrentData.currentPlaylist.songs.isEmpty()){
					return;
				}
				if(!mp.isNull()){
					if(PlayerStatus.endReached == true){
						// We've reached the end of a playlist, and repeat is off.
						// Start playing again from the beginning.
						CurrentData.currentSongIndex = 0;
						CurrentData.currentPlaylistPosition = 0;
						CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(0);
						PlayerStatus.endReached = false;
						if(mBound){
							mService.playSong();
						}
						updateSongUI(mp.isPlaying());
					}

					// check for already playing
					if(mp.isPlaying()){
						if(mBound){
							mService.pausePlayer();
						}
					}else{
						if(PlayerStatus.playerReady){
							// Resume song
							Log.d("test", "Ready");
							mp.start();
							btnPlay.setImageResource(R.drawable.ic_action_pause);
							mService.cancelAlarm();
							mService.updateNotification(true);
						}
						else{
							// Player is not in the prepared state, so we need to load a song and play it. 
							mService.playSong();
							updateSongUI(true);
							btnPlay.setImageResource(R.drawable.ic_action_pause);
							mService.cancelAlarm();
						}
					}
				}

			}
		});


		/**
		 * Next button click event.
		 * */
		btnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mBound){
					mService.playNext();
				}
			}
		});


		/**
		 * Back button click event.
		 * */
		btnPrevious.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mBound){
					mService.playPrevious();
				}
			}
		});

		/**
		 * Button Click event for Repeat button.
		 * Moves through the Repeat options.
		 * */
		btnRepeat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if(PlayerOptions.repeatMode.equals("OFF")){
					PlayerOptions.repeatMode = "PLAYLIST";
					makeToast("Repeat Current Playlist");
					btnRepeat.setBackgroundResource(R.drawable.control_button_selected);
				}else if(PlayerOptions.repeatMode.equals("PLAYLIST")){
					PlayerOptions.repeatMode = "SONG";
					makeToast("Repeat Current Song");
					repeat_once_text.setTextColor(Color.argb(180,255,255,255));
				}else{
					PlayerOptions.repeatMode = "OFF";
					makeToast("Repeat is Off");
					repeat_once_text.setTextColor(Color.argb(0,255,255,255));
					btnRepeat.setBackgroundResource(R.drawable.control_button);
				}

				CurrentData.shuffleReset();

				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString("repeatMode", PlayerOptions.repeatMode);
				editor.commit();

			}
		});

		/**
		 * Button Click event for Shuffle button.
		 * Toggles shuffle flag.
		 * */
		btnShuffle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(PlayerOptions.isShuffle){
					PlayerOptions.isShuffle = false;
					for (int i = 0; i < CurrentData.currentPlaylist.songs.size(); i++){
						if(CurrentData.currentPlaylist.songs.get(i).equals(CurrentData.currentSong)){
							CurrentData.currentSongIndex = i;
							CurrentData.currentPlaylistPosition = i;
						}
					}
					makeToast("Shuffle is OFF");
					btnShuffle.setBackgroundResource(R.drawable.control_button);
				}else{
					// Turn on shuffle
					PlayerOptions.isShuffle = true;
					CurrentData.shuffleReset();
					makeToast("Shuffle is ON");
					btnShuffle.setBackgroundResource(R.drawable.control_button_selected);
				}
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putBoolean("isShuffle", PlayerOptions.isShuffle);
				editor.commit();
			}
		});

		/**
		 * Button Click event for Play list button.
		 * Launches PlayListActivity.
		 * */
		btnPlaylist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
				startActivityForResult(i, 100);
			}
		});

	}

	// 
	/**
	 * Called when this activity is put in the background.
	 * Sets an alarm for the service to be removed from the foreground if it's not playing anything,
	 * then unbinds from the service.
	 * */
	@Override
	protected void onPause() { 
		super.onPause();
		PlayerStatus.isVisible = false;

		if (mBound) {
			if (!mp.isPlaying()){
				// Set an alarm if not playing
				mService.setAlarm();
			}
			// Unbind from the service
			unbindService(mConnection);
			mBound = false;
		}
	}

	/**
	 * Called when this activity returns from the background.
	 * */
	@Override
	protected void onResume() {
		super.onResume();
		PlayerStatus.isVisible = true;


		//		if(PlayerStatus.timerReset){
		//			currentDuration = 0;
		//			songCurrentDurationLabel.setText(String.valueOf(utils.milliSecondsToTimer(currentDuration)));			
		//			PlayerStatus.timerReset = false;
		//		}


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

			updateSongUI(mp.isPlaying());

			mService.cancelAlarm();
			if (mp.isPlaying()){
				mService.createNotification(true);
			}
			else{
				mService.createNotification(false);
			}

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
	 * Receive an intent from PlayListActivity.
	 * PlayListActivity has changed CurrentData.currentSong to something new, so have the service play that.
	 * */
	@Override
	protected void onActivityResult(int requestCode,
			int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 321){
			// Play selected song.
			mService.playSong();
			// update UI for new song
			updateSongUI(true);
		}

	}




	/**
	 * Function to update all the data in the main view for the currently playing song.
	 * The parameter isPlaying indicates whether a song is playing, and the function sets the play / pause button appropriately.
	 * We explicitly pass the player status rather than just calling mp.isPlaying() since the state in the MediaPlayer object
	 * is updated asynchronously, and mp.isPlaying() may not return the correct value without synchronization mechanisms.
	 * Since we're dealing with the UI thread, waiting for synchronization is not a good option.
	 * */
	public void updateSongUI(boolean isPlaying){
		// Play song

		String songTitle;
		String songArtist;
		String songAlbum;
		if(CurrentData.currentSong != null){
			// Displaying Song title
			songTitle = CurrentData.currentSong.songData.get("songTitle");
			songArtist = CurrentData.currentSong.songData.get("songArtist");
			songAlbum = CurrentData.currentSong.songData.get("songAlbum");
		}
		else{
			songTitle = "";
			songArtist = "";
			songAlbum = "";
		}
		songTitleLabel.setText(songTitle);
		songArtistLabel.setText(songArtist);
		songAlbumLabel.setText(songAlbum);

		new AlbumArtUpdater().execute();

		// Changing Button Image to correct image
		if (isPlaying){
			btnPlay.setImageResource(R.drawable.ic_action_pause);
		}
		else{
			btnPlay.setImageResource(R.drawable.ic_action_play);
		}


		// set Progress bar values
		songProgressBar.setProgress(0);
		songProgressBar.setMax(1000);




		// Updating progress bar
		updateProgressBar();

	}

	// Thread for updating album art
	
	private class AlbumArtUpdater extends AsyncTask<Void, Void, Integer> {
		
		protected Integer doInBackground(Void... v) {
			
			MediaMetadataRetriever acr = new MediaMetadataRetriever();
			if(CurrentData.currentSong != null){
				acr.setDataSource(CurrentData.currentSong.songData.get("songPath"));
				try{
					art = acr.getEmbeddedPicture();
					songImage = BitmapFactory
							.decodeByteArray(art, 0, art.length);
				}
				catch(Exception e){
					songImage = null;
				}
			}
			else{
				songImage = null;
			}
			return 0;
		}
		protected void onPostExecute(Integer result) {
			
			ImageView albumFrame = (ImageView) findViewById(R.id.albumFrame);
			if(songImage != null)
				albumFrame.setImageBitmap(songImage);
			else{
				albumFrame.setImageResource(android.R.color.transparent);
			}
		}
	}


	/**
	 * Update timer on seekbar.
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}   

	/**
	 * Thread for updating the time display.
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			if(mBound){

				boolean otherThread = updateTimeThreadLock.getAndSet(true);

				// First, if there is no current song, we set both labels to be blank.
				if(CurrentData.currentSong == null){
					songTotalDurationLabel.setText("");
					songCurrentDurationLabel.setText("");

					return;
				}
				// Next we find the total duration.
				if(!CurrentData.currentSong.songData.get("Duration").equals("error")){
					totalDuration = Integer.parseInt(CurrentData.currentSong.songData.get("Duration"));
				}
				else{
					totalDuration = 0;
				}
				// If we've just changed the song, we need to move the timer back to 0
				if(PlayerStatus.timerReset){

					currentDuration = 0;
					PlayerStatus.timerReset = false;
				}
				// This is the main case
				else if(PlayerStatus.playerReady && CurrentData.currentSong != null){
					try{
						currentDuration = mp.getCurrentPosition();
					}catch(NullPointerException e){

					}catch(IllegalStateException e){

					}
				}
				else{
					currentDuration = 0;
				}	

				// Displaying Total Duration time
				songTotalDurationLabel.setText(String.valueOf(utils.milliSecondsToTimer(totalDuration)));
				// Displaying time completed playing

				if(currentDuration < totalDuration || CurrentData.currentSong == null){
					songCurrentDurationLabel.setText(String.valueOf(utils.milliSecondsToTimer(currentDuration)));
				}

				// Update progress bar
				int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
				songProgressBar.setProgress(progress);

				// Reset the lock and re-run this thread after 100 milliseconds
				if(!otherThread){
					mHandler.postDelayed(resetLockAndRunTimeTask, 100);
				}
			}
		}
	};

	/**
	 * Thread which resets the updateTime lock, then runs updateTimeTask.
	 * */
	private Runnable resetLockAndRunTimeTask = new Runnable() {
		public void run() {
			updateTimeThreadLock.set(false);
			mHandler.post(mUpdateTimeTask);
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
		}, 800);

	}

	/**
	 * This class acts as a mediator between all of the front-end stuff in this activity with all the back-end stuff going on in MusicPlayerService.
	 * It just mimics the API for MediaPlayer and sends the corresponding actions to the actual MediaPlayer object in the service.
	 * */
	class mp3Player
	{

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
