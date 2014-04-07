package com.andrewkiluk.machmusicplayer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;


public class MusicPlayerService extends Service implements OnCompletionListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {

	// The MediaPlayer class
	MediaPlayer mp = null;

	// Keep track of current song and artist
	private String currentSongTitle;
	private String currentSongArtist;

	// For checking and storing preferences
	private SharedPreferences sharedPrefs;
	int NOTIFICATION_HIDE_MINUTES;

	// For dealing with audio focus handling
	public AudioManager audioManager;
	private boolean hasAudioFocus = true;
	private boolean shouldResume = false;

	// For pausing when headphones are unplugged
	private BroadcastReceiver headphoneReceiver;

	// For system notifications
	private NotificationCompat.Builder notificationBuilder;
	private BroadcastReceiver notificationBroadcastReceiver;

	// For album art in the system notification
	byte[] art;
	Bitmap songImage;
	Bitmap albumThumb;
	private int largeIconHeight;
	private int largeIconWidth;

	// These PendingIntents are used to take actions from the system notification 
	PendingIntent piPlay;
	PendingIntent piPause;
	PendingIntent piNext;
	PendingIntent piPrevious;
	PendingIntent clickNotificationIntent;

	// This interface allows this service to give instructions to MusicPlayerActivity when it is bound
	public interface BoundServiceListener {
		public void changeUIforSong(boolean isPlaying);
		public void SetPlayButtonStatus(String status);
	}

	// Binder for interaction
	private final IBinder mBinder = new LocalBinder();

	public BoundServiceListener mListener;

	public class LocalBinder extends Binder {
		public MusicPlayerService getService() {
			// Return this instance of LocalService so clients can call public methods
			return MusicPlayerService.this;
		}

		public void setListener(BoundServiceListener listener) {
			mListener = listener;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate(){

		// Create the MediaPlayer
		mp = new MediaPlayer();
		mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

		// Run various setup functions
		initializeNotificationBroadcastReceiver();
		AlarmSetup();
		HeadphoneUnplugListenerSetup();

		// Check the dimensions of notification icons.
		Context mContext = getApplicationContext();
		Resources res = mContext.getResources();
		largeIconHeight = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
		largeIconWidth = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

		// Create pending intents to send to MusicPlayerService upon notification button presses 
		piPlay = PendingIntent.getBroadcast( this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.play"),0 );
		piPause = PendingIntent.getBroadcast( this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.pause"),0 );
		piNext = PendingIntent.getBroadcast(this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.next"), 0);
		piPrevious = PendingIntent.getBroadcast(this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.previous"), 0);

		//The intent to launch when the user clicks the expanded notification
		Intent intent = new Intent(this, MusicPlayerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		clickNotificationIntent = PendingIntent.getActivity(this, 0, intent, 0);

		// Load settings
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String sleep = sharedPrefs.getString("serviceSleepDelay", "null");
		if(!sleep.equals("null")){
			try{
				NOTIFICATION_HIDE_MINUTES = Integer.parseInt(sleep);
			}catch(NumberFormatException e){
				NOTIFICATION_HIDE_MINUTES = 5;
			}
		}
		else{
			NOTIFICATION_HIDE_MINUTES = 5;
		}
		PlayerOptions.isShuffle = sharedPrefs.getBoolean("isShuffle", false);
		PlayerOptions.repeatMode = sharedPrefs.getString("repeatMode", "OFF");

		// Set up the MediaPlayer with the current song and saved progress position
		mp.reset();
		if(CurrentData.currentSong != null){
			try{
				mp.setDataSource(CurrentData.currentSong.songData.get("songPath"));
				mp.prepare();
			}catch(IOException e){
				e.printStackTrace();
			}
			mp.seekTo(sharedPrefs.getInt("currentTimer", 0));

			mp.setOnCompletionListener(this);

			createNotification(false);	
		}

		// Set up audio focus listener
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

	}


	@Override
	public void onCompletion(MediaPlayer arg0) { 

		Log.d("test", PlayerOptions.repeatMode);

		// check whether repeat is ON or OFF
		if(PlayerOptions.repeatMode == "SONG"){
			// if repeat is on, play same song again
			playSong();
		} 
		else if(CurrentData.currentSongIndex == CurrentData.currentPlaylist.songs.size()-1 && PlayerOptions.repeatMode.equals("OFF")){
			// Completed playlist
			PlayerStatus.endReached = true;
			if (mListener!=null){
				mListener.SetPlayButtonStatus("play");
			}
		}
		else {
			playNext();
		}
	}

	public void onAudioFocusChange(int focusChange) {

		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:

			// resume playback
			if (mp == null) {
				mp = new MediaPlayer();
				mp.setOnCompletionListener(this);

				mp.reset();
				String songPath = CurrentData.currentSong.songData.get("songPath");
				try {
					mp.setDataSource(songPath);
					mp.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				} 

				mp.seekTo(sharedPrefs.getInt("currentTimer", 0));
				if(!shouldResume){
					mp.pause();
				}

			}
			else if (!mp.isPlaying()){
				if (shouldResume){
					mp.start();
					shouldResume = false;
				}
			}
			mp.setVolume(1.0f, 1.0f);
			hasAudioFocus = true;
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			// Lost focus for an unbounded amount of time: stop playback and release media player
			boolean playing;
			try{
				playing = mp.isPlaying();
			}catch(IllegalStateException e){
				playing = false;
			}
			if (mp != null && playing){
				mp.stop();

				// Record the current playback position
				sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putInt("currentTimer", mp.getCurrentPosition());
				editor.commit();

				if (mListener!=null){
					mListener.SetPlayButtonStatus("play");
				}
				hasAudioFocus = false;
			}
			mp.release();
			mp = null;
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			if (mp.isPlaying()){
				pausePlayer();
			}
			hasAudioFocus = false;
			shouldResume = true;
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
			if (mp.isPlaying()) mp.setVolume(0.1f, 0.1f);
			break;
		}
	}


	// The following block creates an alarm which can be called to stop this service from running in the foreground.
	// This is to save system resources if the player has been idle for long enough.
	private BroadcastReceiver alarmReceiver;
	private AlarmManager am;
	private PendingIntent alarmpi;

	private void AlarmSetup(){
		alarmReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {
				stopForeground(true);
			}
		};
		registerReceiver(alarmReceiver, new IntentFilter("com.andrewkiluk.servicealarm") );
		alarmpi = PendingIntent.getBroadcast( this, 0, new Intent("com.andrewkiluk.servicealarm"),0 );
		am = (AlarmManager)(this.getSystemService( Context.ALARM_SERVICE ));
		PlayerStatus.alarm_set = false;
	}


	// The following block sets up a listener to pause if headphones are unplugged.

	private void HeadphoneUnplugListenerSetup(){
		headphoneReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (mp.isPlaying() && AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
					pausePlayer();
				}
			}
		};
		registerReceiver(headphoneReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY) );
	}

	public void initializeNotificationBroadcastReceiver(){
		notificationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent i) {
				String action = i.getAction();
				if (action == "com.andrewkiluk.notificationBroadcastReceiver.play"){
					mp.start();
					mListener.SetPlayButtonStatus("pause");
					createNotification(true);
				}
				if (action == "com.andrewkiluk.notificationBroadcastReceiver.pause"){
					pausePlayer();

				}
				if (action == "com.andrewkiluk.notificationBroadcastReceiver.next"){
					playNext();


				}
				if (action == "com.andrewkiluk.notificationBroadcastReceiver.previous"){
					playPrevious();
				}

			}
		};

		IntentFilter notificationFilter = new IntentFilter("com.andrewkiluk.notificationBroadcastReceiver.play");
		notificationFilter.addAction("com.andrewkiluk.notificationBroadcastReceiver.pause");
		notificationFilter.addAction("com.andrewkiluk.notificationBroadcastReceiver.next");
		notificationFilter.addAction("com.andrewkiluk.notificationBroadcastReceiver.previous");

		registerReceiver(notificationBroadcastReceiver, notificationFilter );
	}
	
	public void pausePlayer()
	{
		mp.pause();
		if(mListener  != null){
			mListener.SetPlayButtonStatus("play");
		}
		updateNotification(false);
		if(!PlayerStatus.isVisible){
			setAlarm();
		}
	}

	void getNextSong(){
		if(PlayerOptions.isShuffle){  // shuffle is on

			int songsLeft = 0;
			for (int i = 0; i < CurrentData.currentPlaylist.songs.size(); i++){
				songsLeft += CurrentData.shuffleQueue[i];
			}

			// Make sure we have a list to choose from
			if(songsLeft == 0){
				for(int i = 0; i < CurrentData.currentPlaylist.songs.size(); i++){
					CurrentData.shuffleQueue[i] = 1;
				}
				songsLeft = CurrentData.currentPlaylist.songs.size();
			}
			if(CurrentData.currentSong == null){
				// Choose a random song from the queue to start
				Random rand = new Random();
				int nextSongIndex = rand.nextInt(songsLeft);
				CurrentData.shuffleQueue[nextSongIndex] = 0;
				CurrentData.shuffleHistory.add(nextSongIndex);
				CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(nextSongIndex);
				CurrentData.currentSongIndex = 0;
				CurrentData.currentPlaylistPosition = nextSongIndex;

			}
			if(CurrentData.shuffleHistory.isEmpty()){
				// If the shuffle queue is empty, add the current song to it and remove that song from the shuffle queue
				CurrentData.shuffleHistory.add(CurrentData.currentPlaylistPosition);
				CurrentData.shuffleQueue[CurrentData.currentPlaylistPosition] = 0;
			}
			// Check if we're in the shuffle history
			if (CurrentData.shuffleHistoryPosition > 0){

				CurrentData.shuffleHistoryPosition = CurrentData.shuffleHistoryPosition - 1;

				// Set the current song to the next one in the shuffle history
				int index = CurrentData.shuffleHistory.get(CurrentData.shuffleHistory.size() - 1 - CurrentData.shuffleHistoryPosition);
				CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(index);

			}
			else{ // Not in shuffle history

				// Choose a random song from the queue
				Random rand = new Random();
				int seed = rand.nextInt(songsLeft);
				int nextSongIndex = 0;
				int count = 0;
				do{
					while(CurrentData.shuffleQueue[nextSongIndex] == 0){
						nextSongIndex++;
					}
					count++;
					if(count < seed){
						// if we're not about to finish, skip the current songs so we can jump over the next block of zeroes.
						nextSongIndex++;
					}
				}while(count < seed);


				Song nextSong = CurrentData.currentPlaylist.songs.get(nextSongIndex);
				CurrentData.shuffleQueue[nextSongIndex] = 0;
				CurrentData.shuffleHistory.add(nextSongIndex);
				CurrentData.currentSong = nextSong;
				CurrentData.currentSongIndex = CurrentData.currentSongIndex + 1;
				CurrentData.currentPlaylistPosition = nextSongIndex;

			}
			Gson gson = new Gson();
			String shuffleHistoryJson = gson.toJson(CurrentData.shuffleHistory);
			String shuffleQueueJson = gson.toJson(CurrentData.shuffleQueue);

			SharedPreferences.Editor editor = sharedPrefs.edit();
			editor.putString("shuffleHistory", shuffleHistoryJson);
			editor.putString("shuffleQueue", shuffleQueueJson);
			editor.putInt("shuffleHistoryPosition", CurrentData.shuffleHistoryPosition);
			editor.commit();
		} // shuffle is not on - play next song
		else if(CurrentData.currentSong == null){
			CurrentData.currentSongIndex = 0;
			CurrentData.currentPlaylistPosition = 0;
			CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(0);
		}
		else if(CurrentData.currentSongIndex < (CurrentData.currentPlaylist.songs.size() - 1)){ 
			// if we're NOT at the last song of the playlist
			CurrentData.currentSongIndex = CurrentData.currentSongIndex + 1;
			CurrentData.currentPlaylistPosition = CurrentData.currentPlaylistPosition + 1;
			CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(CurrentData.currentSongIndex);

		}
		else{ 
			// if we ARE at the last song of the playlist
			// play first song
			CurrentData.currentSongIndex = 0;
			CurrentData.currentPlaylistPosition = 0;
			CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(CurrentData.currentSongIndex);
		}
	}


	void playNext(){   
		if(CurrentData.currentPlaylist.songs.isEmpty()){
			return;
		}
		if(CurrentData.currentPlaylist != null){

			getNextSong();

			playSong();
		}
		// Tell the Activity to update the UI for the new song. 
		if (mListener!=null){
			mListener.changeUIforSong(true);
		}

		// Now save the new player state in Shared Prefs:
		Gson gson = new Gson();
		String songJson = gson.toJson(CurrentData.currentSong);

		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("currentSong", songJson);
		editor.putInt("currentSongIndex", CurrentData.currentSongIndex);
		editor.putInt("currentPlaylistPosition", CurrentData.currentPlaylistPosition);
		editor.commit();
	}



	void playPrevious(){
		if(CurrentData.currentPlaylist.songs.isEmpty()){
			return;
		}
		// If we're more than 3 seconds into the song, just seek back to the beginning.
		if(mp.getCurrentPosition() > 3000){
			mp.seekTo(0);
		}
		else if(PlayerOptions.isShuffle){
			if (CurrentData.shuffleHistoryPosition < CurrentData.shuffleHistory.size() - 1){
				if (CurrentData.shuffleHistoryPosition == CurrentData.shuffleHistory.size() ){
					playSong();
				}
				else{
					CurrentData.shuffleHistoryPosition = CurrentData.shuffleHistoryPosition + 1;
					int index = CurrentData.shuffleHistory.get(CurrentData.shuffleHistory.size() - 1 - CurrentData.shuffleHistoryPosition);
					CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(index);
					playSong();
				}
			}
			else{
				// We're at the furtherest back song, do nothing
			}
		} else{
			// shuffle off - play previous song
			if(CurrentData.currentSongIndex != 0){
				CurrentData.currentSongIndex = CurrentData.currentSongIndex - 1;
				try{
					CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(CurrentData.currentSongIndex);
				}catch(IndexOutOfBoundsException e){
					e.printStackTrace();
				}
				playSong();

			}else if (CurrentData.currentSongIndex == 0){
				// play last song
				CurrentData.currentSongIndex = (CurrentData.currentPlaylist.songs.size() - 1);
				try{
					CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(CurrentData.currentSongIndex);
				}catch(IndexOutOfBoundsException e){

				}
				playSong();

			}
		}
		// Tell the Activity to update the UI for the new song. 
		if (mListener!=null){
			mListener.changeUIforSong(true);			
		}

		// Now save the new player state in Shared Prefs:
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Gson gson = new Gson();
		String songJson = gson.toJson(CurrentData.currentSong);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("currentSong", songJson);
		editor.putInt("currentSongIndex", CurrentData.currentSongIndex);
		editor.putInt("currentPlaylistPosition", CurrentData.currentPlaylistPosition);
		editor.commit();
	}

	

	// Call when you want to play CurrentData.currentSong
	// Calls play() and updates the UI.
	public void playSong(){ 
		if(CurrentData.currentSong != null){
			try{
				play();
				currentSongArtist = CurrentData.currentSong.songData.get("songArtist");
				currentSongTitle = CurrentData.currentSong.songData.get("songTitle");
				createNotification(true);
				if (mListener!=null){
					mListener.SetPlayButtonStatus("pause");
				}
			}catch(IndexOutOfBoundsException e){
				pausePlayer();
			}
		}
		else if(CurrentData.currentPlaylist.songs.size() != 0){
			CurrentData.currentSongIndex = 0;
			CurrentData.currentPlaylistPosition = 0;
			CurrentData.currentSong = CurrentData.currentPlaylist.songs.get(0);
			playSong();
		}
	}

	// Called by the playSong function, does the MediaPlayer mechanics
	public void play() { 

		if  (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN)){
			hasAudioFocus = true;
		}
		if (hasAudioFocus) {
			mp.reset();
			try {
				mp.setDataSource(CurrentData.currentSong.songData.get("songPath"));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} 
			mp.setOnPreparedListener(this);
			mp.prepareAsync(); // prepare asynchronously to not block main thread
		}
	}

	// Loads CurrentData.currentSong into the MediaPlayer class.
	public void loadCurrentSong(){
		mp.reset();
		try {
			mp.setDataSource(CurrentData.currentSong.songData.get("songPath"));
			PlayerStatus.playerReady = false;
			PlayerStatus.timerReset = true;
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	// Creates a new system notification
	public void createNotification(boolean isPlaying)
	{

		updateCurrentSong();

		try {
			// Used to read ID3 tags and retrieve album art.
			MediaMetadataRetriever acr = new MediaMetadataRetriever();
			acr.setDataSource(CurrentData.currentSong.songData.get("songPath"));

			art = acr.getEmbeddedPicture();
			songImage = BitmapFactory
					.decodeByteArray(art, 0, art.length);
			albumThumb = Bitmap.createScaledBitmap(songImage, largeIconWidth, largeIconHeight, false);


		} catch (Exception e) {
			albumThumb = null;
			songImage = null;
		}
		updateNotification(isPlaying);

	}

	// Updates existing system notification
	public void updateNotification(boolean isPlaying) {
		final int notificationID = 1;

		updateCurrentSong();

		notificationBuilder = new NotificationCompat.Builder(this);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		final boolean localIsPlaying = isPlaying;

		class setNotification implements Runnable {
			public void run() {

				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

				boolean big_notifications = sharedPrefs.getBoolean("big_notifications", true);

				notificationBuilder.setOngoing(true)
				.setPriority(Notification.PRIORITY_HIGH)
				.setWhen(0)
				.setContentIntent(clickNotificationIntent)
				.setContentTitle(currentSongTitle)
				.setContentText(currentSongArtist)
				.setSmallIcon(R.drawable.ic_action_play);
				if (big_notifications && songImage != null){
					notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
					.setSummaryText(currentSongArtist)
					.bigPicture(songImage));
				}
				else{
					notificationBuilder.setLargeIcon(albumThumb);
				}
				if (!localIsPlaying){
					notificationBuilder.addAction (R.drawable.ic_action_previous, "Back", piPrevious)
					.addAction (R.drawable.ic_action_play, "Play", piPlay)
					.addAction (R.drawable.ic_action_next, "Next", piNext);
				}
				if (localIsPlaying){
					notificationBuilder.addAction (R.drawable.ic_action_previous, "Back", piPrevious)
					.addAction (R.drawable.ic_action_pause, "Pause", piPause)
					.addAction (R.drawable.ic_action_next, "Next", piNext);
				}


				Notification notification = notificationBuilder.build();


				notification.flags |= Notification.FLAG_NO_CLEAR;
				startForeground(notificationID, notification);
				PlayerStatus.notification_set = true;	


			}
		};

		setNotification setNot = new setNotification();

		setNot.run();

	}

	// Called when MediaPlayer is ready to actually play a song.
	public void onPrepared(MediaPlayer player) {
		PlayerStatus.playerReady = true;
		player.start();
		mp.setOnCompletionListener(this);
	}

	// Update information about the current song
	public void updateCurrentSong(){ 

		if(CurrentData.currentSong != null){
			currentSongArtist = CurrentData.currentSong.songData.get("songArtist");
			currentSongTitle = CurrentData.currentSong.songData.get("songTitle");
		}
		else{
			currentSongArtist = " ";
			currentSongTitle = " ";	
		}


	}

	// The following few functions are an interface that we expose to MusicPlayerActivity
	public void pause() {
		mp.pause();
	}

	public boolean isNull() {
		return mp == null;
	}

	public void create() {
		mp = new MediaPlayer();
	}

	public boolean isPlaying() {
		if (mp == null)
			return false;
		else
			return mp.isPlaying();
	}

	public void seekTo(int currentPosition) {
		mp.seekTo(currentPosition);
	}

	public void release() {
		mp.release();
	}

	public int getDuration()
	{
		return mp.getDuration();
	}

	public int getCurrentPosition()
	{
		return mp.getCurrentPosition();
	}

	public void setDataSource(String input){
		try{
			mp.setDataSource(input);
			PlayerStatus.playerReady = true;
		}catch(IOException e){

		}
	}

	public void reset() {
		mp.reset();
	}

	public void setAlarm() {
		// Set an alarm to stop running in foreground after NOTIFICATION_HIDE_MINUTES minutes.
		am.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * 60 * NOTIFICATION_HIDE_MINUTES, alarmpi ); 
		PlayerStatus.alarm_set = true;
	}

	public void cancelAlarm() {
		// Cancel the alarm from setAlarm().
		am.cancel(alarmpi);
		PlayerStatus.alarm_set = false;
	}

	public void prepare() {
		try{
			mp.prepare();
		}catch(IOException e){

		}
	}

	public void start() {
		if  (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN)){
			hasAudioFocus = true;
		}
		if (true) {
			mp.start();
		}
	}



	@Override
	public boolean onUnbind(Intent intent) {

		return super.onUnbind(intent);
	}


	@Override
	public void onDestroy(){
		am.cancel(alarmpi);

		unregisterReceiver(alarmReceiver);
		unregisterReceiver(headphoneReceiver);
		unregisterReceiver(notificationBroadcastReceiver);

		// Store the current song progress in system settings.

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putInt("currentTimer", mp.getCurrentPosition());
		Log.d("test", "destroyed at " + mp.getCurrentPosition());
		editor.commit();

		mp.stop();
		mp.release();
		super.onDestroy();
	}
}
