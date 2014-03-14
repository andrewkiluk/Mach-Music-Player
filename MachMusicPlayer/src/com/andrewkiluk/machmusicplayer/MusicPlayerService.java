package com.andrewkiluk.machmusicplayer;

import java.util.Random;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;


public class MusicPlayerService extends Service implements OnCompletionListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {

	MediaPlayer mp = null;
	private String currentSongTitle;
	private String currentSongArtist;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder notificationBuilder;
	private BroadcastReceiver notificationBroadcastReceiver;
	
	
	private boolean playerReady = true; 

	
	private SharedPreferences sharedPrefs;




	// This stores the old playback location if the audio focus is lost.
	// It has to be a class because integers in Java are fucking stupid.
	class PositionTracker{
		private int pos;
		int get()
		{
			return pos; 
		}
		void set(int rhs){
			pos = rhs;
		}
	}
	PositionTracker oldPosition = new PositionTracker();

	private PlayerOptions po;

	public BoundServiceListener mListener;

	public AudioManager audioManager;
	private boolean hasAudioFocus = true;

	private BroadcastReceiver headphoneReceiver;

	byte[] art;
	Bitmap songImage;
	private int largeIconHeight;
	private int largeIconWidth;
	int NOTIFICATION_HIDE_MINUTES;

	public interface BoundServiceListener {
		public void changeUIforSong(boolean isPlaying);
		public void SetPlayButtonStatus(String status);
	}

	// Binder for interaction
	private final IBinder mBinder = new LocalBinder();

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

	private boolean firstRun = true;
	@Override
	public void onCreate(){

		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);

		// The next few blocks load the last saved state.
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);




		// Check for last song index and timer
		int oldSongIndex = sharedPrefs.getInt("currentSongIndex", -1);

		if (oldSongIndex != -1){
			LibraryInfo.currentSongIndex = oldSongIndex;
		}
		else{
			LibraryInfo.currentSongIndex = 0;
		}
		//		if (oldSongPosition != -1){
		//			mp.seekTo(oldSongPosition);
		//		}

		if (mListener!=null){
			if(mListener !=  null){
				mListener.changeUIforSong(false);		
			}
		}



		po = new PlayerOptions();
		//		ps = new PlayerStatus();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		initializeNotificationBroadcastReceiver();
		AlarmSetup();
		HeadphoneUnplugListenerSetup();

		// Check the dimensions of notification icons.

		if(firstRun){
			Context mContext = getApplicationContext();
			Resources res = mContext.getResources();
			largeIconHeight = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
			largeIconWidth = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
			String sleep = sharedPrefs.getString("serviceSleepDelay", "NULL");
			if(sleep != "NULL"){
				NOTIFICATION_HIDE_MINUTES = Integer.parseInt(sleep);
			}
			else{
				NOTIFICATION_HIDE_MINUTES = 5;
			}
			firstRun = false;			
		}


		// Audio focus listener
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);



	}



	@Override
	public void onCompletion(MediaPlayer arg0) {

		// check for repeat is ON or OFF
		if(po.isRepeat){
			// repeat is on play same song again
			playSong();
		} else if(po.isShuffle){
			// shuffle is on - play a random song
			Random rand = new Random();
			LibraryInfo.currentSongIndex = rand.nextInt((LibraryInfo.currentPlaylist.songs.size() - 1) - 0 + 1) + 0;
			playSong();
		} else{
			// no repeat or shuffle ON - play next song
			if(LibraryInfo.currentPlaylist != null){
				if(LibraryInfo.currentSongIndex < (LibraryInfo.currentPlaylist.songs.size() - 1)){
					LibraryInfo.currentSongIndex = LibraryInfo.currentSongIndex + 1;
					playSong();

				}else{
					// play first song
					LibraryInfo.currentSongIndex = 0;
					playSong();
				}
			}
		}

		// Now tell the Activity to update the UI for the new song. 
		if (mListener!=null){
			mListener.changeUIforSong(true);			
		}

	}


	private boolean shouldResume = false;
	public void onAudioFocusChange(int focusChange) {

		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:

			// resume playback
			if (mp == null) {
				mp = new MediaPlayer();
				mp.setOnCompletionListener(this);

				mp.reset();
				String songPath = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songPath");
				try {
					mp.setDataSource(songPath);
					mp.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				} 

				mp.seekTo(oldPosition.get());
				oldPosition.set(0);


			}
			else if (!mp.isPlaying()){
				if (shouldResume){
					mp.start();
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
				oldPosition = new PositionTracker();
				oldPosition.set(mp.getCurrentPosition());
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
				if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
					if (mp.isPlaying()){
						pausePlayer();
					}
				}
			}
		};
		registerReceiver(headphoneReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY) );
	}

	public void pausePlayer()
	{
		mp.pause();
		if(mListener  != null){
			mListener.SetPlayButtonStatus("play");
		}
		createNotification(false);
		if(!AppStatus.isVisible){
			setAlarm();
		}
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
					if(po.isShuffle){
						// shuffle is on - play a random song
						Random rand = new Random();
						LibraryInfo.currentSongIndex = rand.nextInt((LibraryInfo.currentPlaylist.songs.size() - 1) - 0 + 1) + 0;
						playSong();
					} else{
						// no shuffle ON - play previous song
						if(LibraryInfo.currentSongIndex != 0){
							LibraryInfo.currentSongIndex--;
							playSong();

						}else if (LibraryInfo.currentSongIndex == 0){
							// play last song
							LibraryInfo.currentSongIndex = (LibraryInfo.currentPlaylist.songs.size() - 1);
							playSong();

						}
					}
					// Now tell the Activity to update the UI for the new song. 
					if (mListener!=null){
						mListener.changeUIforSong(true);			
					}
				}

			}
		};

		IntentFilter notificationFilter = new IntentFilter("com.andrewkiluk.notificationBroadcastReceiver.play");
		notificationFilter.addAction("com.andrewkiluk.notificationBroadcastReceiver.pause");
		notificationFilter.addAction("com.andrewkiluk.notificationBroadcastReceiver.next");
		notificationFilter.addAction("com.andrewkiluk.notificationBroadcastReceiver.previous");

		registerReceiver(notificationBroadcastReceiver, notificationFilter );
	}

	void playNext(){
		if(po.isShuffle){
			// shuffle is on - play a random song
			Random rand = new Random();
			LibraryInfo.currentSongIndex = rand.nextInt((LibraryInfo.currentPlaylist.songs.size() - 1) - 0 + 1) + 0;
			playSong();
		} else{
			// no shuffle ON - play next song
			if(LibraryInfo.currentSongIndex < (LibraryInfo.currentPlaylist.songs.size() - 1)){
				LibraryInfo.currentSongIndex = LibraryInfo.currentSongIndex + 1;
				playSong();

			}else{
				// play first song
				LibraryInfo.currentSongIndex = 0;
				playSong();
			}
		}
		// Now tell the Activity to update the UI for the new song. 
		if (mListener!=null){
			mListener.changeUIforSong(true);
		}
	}


	// The following functions are accessible to MusicPlayerActivity for communication with the UI
	public void setDataSource(String input){
		try{
			mp.setDataSource(input);
			playerReady = true;
		}catch(Exception e){

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
		}catch(Exception e){

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

	public void playSong(){
		if(LibraryInfo.currentPlaylist.songs.size() == 0){

		}
		else{
			try{
				currentSongArtist = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songArtist");
				currentSongTitle = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songTitle");
				play(LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songPath"));
				createNotification(true);
				if (mListener!=null){
					mListener.SetPlayButtonStatus("pause");
				}
			}catch(IndexOutOfBoundsException e){
				pausePlayer();
			}
		}

	}

	public void createNotification(boolean isPlaying)
	{

		int myID = 1234;

		//The intent to launch when the user clicks the expanded notification
		Intent intent = new Intent(this, MusicPlayerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

		Bitmap albumThumb;

		try {
			// Used to read ID3 tags and retrieve album art.
			MediaMetadataRetriever acr = new MediaMetadataRetriever();
			acr.setDataSource(LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songPath"));
			art = acr.getEmbeddedPicture();
			songImage = BitmapFactory
					.decodeByteArray(art, 0, art.length);
			albumThumb = Bitmap.createScaledBitmap(songImage, largeIconWidth, largeIconHeight, false);


		} catch (Exception e) {
			albumThumb = null;
			songImage = null;
		}

		// Create pending intents to send to MusicPlayerService upon button presses 
		PendingIntent piPlay = PendingIntent.getBroadcast( this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.play"),0 );
		PendingIntent piPause = PendingIntent.getBroadcast( this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.pause"),0 );
		PendingIntent piNext = PendingIntent.getBroadcast(this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.next"), 0);
		PendingIntent piPrevious = PendingIntent.getBroadcast(this, 0, new Intent("com.andrewkiluk.notificationBroadcastReceiver.previous"), 0);


		//
		//
		//
		//
		//
		//
		// Make the priority custom?????
		//
		//
		//
		//
		//
		//
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean big_notifications = sharedPrefs.getBoolean("big_notifications", true);

		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setOngoing(true)
		.setPriority(Notification.PRIORITY_HIGH)
		.setWhen(0)
		.setContentIntent(pendIntent)
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
		if (!isPlaying){
			notificationBuilder.addAction (R.drawable.ic_action_previous, "Back", piPrevious)
			.addAction (R.drawable.ic_action_play, "Play", piPlay)
			.addAction (R.drawable.ic_action_next, "Next", piNext);
		}
		if (isPlaying){
			notificationBuilder.addAction (R.drawable.ic_action_previous, "Back", piPrevious)
			.addAction (R.drawable.ic_action_pause, "Pause", piPause)
			.addAction (R.drawable.ic_action_next, "Next", piNext);
		}


		Notification notification = notificationBuilder.build();


		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(myID, notification);
		PlayerStatus.notification_set = true;
	}



	public void play(String songPath) {

		if  (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN)){
			hasAudioFocus = true;
		}
		if (hasAudioFocus) {
			mp.reset();
			try {
				mp.setDataSource(songPath);
			} catch (Exception e) {
				e.printStackTrace();
			} 

			mp.setOnPreparedListener(this);
			mp.prepareAsync(); // prepare asynchronously to not block main thread


		}

	}

	public void pause() {
		mp.pause();
	}

	public boolean isNull() {
		return mp == null;
	}

	public void create() {
		mp = new MediaPlayer();
	}

	public PlayerOptions getPlayerOptions(){
		return po;
	}

	//	public PlayerStatus getPlayerStatus(){
	//		return ps;
	//	}

	public boolean isShuffle() {
		return po.isShuffle;
	}

	public boolean isRepeat() {
		return po.isRepeat;
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
	
	public boolean isReady()
	{
		return playerReady;
	}


	/** Called when MediaPlayer is ready */
	public void onPrepared(MediaPlayer player) {
		playerReady = true;
		player.start();
	}


	public void updateCurrentSong(){
		currentSongArtist = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songArtist");
		currentSongTitle = LibraryInfo.currentPlaylist.songs.get(LibraryInfo.currentSongIndex).songData.get("songTitle");


	}



	@Override
	public void onDestroy(){
		am.cancel(alarmpi);

		unregisterReceiver(alarmReceiver);
		unregisterReceiver(headphoneReceiver);
		unregisterReceiver(notificationBroadcastReceiver);

		// Store the current playlist in system settings.
		Gson gson = new Gson();
		String currentPlaylistJson = gson.toJson(LibraryInfo.currentPlaylist);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("currentPlaylist", currentPlaylistJson);
		editor.commit();


		mp.stop();
		mp.release();
		super.onDestroy();
	}
}




class PlayerStatus {
	public static boolean notification_set = false;
	public static boolean alarm_set = false;
	public static boolean playlistReset;
}


class PlayerOptions	{
	// Default constructor
	PlayerOptions(){
		isRepeat = false;
		isShuffle = false;
	}
	public boolean isRepeat;
	public boolean isShuffle;
}

