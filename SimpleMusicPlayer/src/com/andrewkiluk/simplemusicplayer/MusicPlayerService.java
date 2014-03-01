package com.andrewkiluk.simplemusicplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;


public class MusicPlayerService extends Service implements OnCompletionListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {
	
	MediaPlayer mp = null;
	private int currentSongIndex = 0;
	private ArrayList<HashMap<String, String>> songsList;
	
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
	
	byte[] art;
	Bitmap songImage;
	private int largeIconHeight;
	private int largeIconWidth;
	
	public interface BoundServiceListener {
		public void songComplete(int newCurrentSongIndex);
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
	
	@Override
	public void onCreate(){
		

		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		
		po = new PlayerOptions();
		
		// Check the dimensions of notification icons.
		
		Context mContext = getApplicationContext();
		Resources res = mContext.getResources();
		largeIconHeight = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
		largeIconWidth = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
		
		// Set up a listener to pause if headphones are unplugged.
				IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
				HeadphoneUnplugListener receiver = new HeadphoneUnplugListener(mp);
				registerReceiver( receiver, intentFilter );

		// Audio focus listener
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		

	}
	
	@Override
    public void onCompletion(MediaPlayer arg0) {
		
        // check for repeat is ON or OFF
        if(po.isRepeat){
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if(po.isShuffle){
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else{
            // no repeat or shuffle ON - play next song
            if(currentSongIndex < (songsList.size() - 1)){
            	currentSongIndex = currentSongIndex + 1;
            	playSong(currentSongIndex);
                
            }else{
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
        
        // Now tell the Activity to update the UI for the new song. 
		if (mListener!=null){
			mListener.songComplete(currentSongIndex);			
		}
      
    }
	
	

	public void onAudioFocusChange(int focusChange) {
		
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// resume playback
			if (mp == null) {
				mp = new MediaPlayer();
				
				mp.reset();
				String songPath = songsList.get(currentSongIndex).get("songPath");
				try {
					mp.setDataSource(songPath);
					mp.prepare();
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
				mp.seekTo(oldPosition.get());
		
				
			}
			else if (!mp.isPlaying()){
				mp.start();
			}
			mp.setVolume(1.0f, 1.0f);
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			// Lost focus for an unbounded amount of time: stop playback and release media player
			if (mp.isPlaying()){
				mp.stop();
				oldPosition = new PositionTracker();
				oldPosition.set(mp.getCurrentPosition());
			}
			mp.release();
			mp = null;
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			if (mp.isPlaying()) mp.pause();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
			if (mp.isPlaying()) mp.setVolume(0.1f, 0.1f);
			break;
		}
	}

	public void setDataSource(String input){
		try{
			mp.setDataSource(input);
		}catch(Exception e){

		}
	}

	public void reset() {
		mp.reset();
	}

	public void prepare() {
		try{
			mp.prepare();
		}catch(Exception e){

		}
	}

	public void start() {
		mp.start();
	}
	
	public void playSong(int songIndex){
		play(songsList.get(songIndex).get("songPath"));
		createNotification(songIndex);
		currentSongIndex = songIndex;
	}
	
	public void createNotification(int songIndex)
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
			acr.setDataSource(songsList.get(songIndex).get("songPath"));
			art = acr.getEmbeddedPicture();
			songImage = BitmapFactory
					.decodeByteArray(art, 0, art.length);
			albumThumb = Bitmap.createScaledBitmap(songImage, largeIconWidth, largeIconHeight, false);
	
	
		} catch (Exception e) {
			albumThumb = null;
		}
	
		    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		    builder.setContentTitle(songsList.get(songIndex).get("songTitle")).setContentText(songsList.get(songIndex).get("songArtist"))
		           .setWhen(System.currentTimeMillis())
		           .setOngoing(true).setPriority(Notification.PRIORITY_HIGH)
		           .setContentIntent(pendIntent)
		           .setSmallIcon(R.drawable.ic_action_play)
		           .setLargeIcon(albumThumb);
		    Notification notification = builder.build();
	
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(myID, notification);
	}

	public void play(String songPath) {
		
		int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			
			
			
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
	
	public boolean isShuffle() {
		return po.isShuffle;
	}
	
	public boolean isRepeat() {
		return po.isRepeat;
	}

	public boolean isPlaying() {
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
	
	public int getCurrentSongIndex()
	{
		return currentSongIndex;
	}
	

	/** Called when MediaPlayer is ready */
	public void onPrepared(MediaPlayer player) {
		player.start();
	}


	public void updatePlayList(ArrayList<HashMap<String, String>> newSongsList, int newCurrentSongIndex){
		songsList = newSongsList;
		currentSongIndex = newCurrentSongIndex;
		
	}
	

	@Override
	public void onDestroy(){
		super.onDestroy();
		mp.release();
	}

	class HeadphoneUnplugListener extends BroadcastReceiver
	{
		private MediaPlayer mp;
		// Constructor
		public HeadphoneUnplugListener(MediaPlayer input){
			mp = input;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
				if (mp.isPlaying()){
					mp.pause();
				}
			}
		}
	}

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
