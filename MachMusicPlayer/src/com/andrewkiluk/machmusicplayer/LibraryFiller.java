package com.andrewkiluk.machmusicplayer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class LibraryFiller {
	// SDCard Path
	String baseDir = Environment.getExternalStorageDirectory().getPath();
	String RELATIVE_MEDIA_PATH;
	String MEDIA_PATH;
	MediaMetadataRetriever mmr = new MediaMetadataRetriever();
	boolean directorySpecified = false;
	Context context;
	boolean filteringOn;
	boolean onlyAlbumArtists;
	boolean removeDuplicateSongs;


	// Constructor
	public LibraryFiller(String library_location){
		RELATIVE_MEDIA_PATH = library_location;
		MEDIA_PATH = "file://" + baseDir + RELATIVE_MEDIA_PATH;
		directorySpecified = true;
	}

	public LibraryFiller(Context context){
		this.context = context;
		directorySpecified = false;
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		filteringOn = sharedPrefs.getBoolean("filteringOn", true);
		onlyAlbumArtists = sharedPrefs.getBoolean("onlyAlbumArtists", true);
		removeDuplicateSongs = sharedPrefs.getBoolean("removeDuplicateSongs", true);
	}

	/**
	 * Function to read all mp3 files from sdcard
	 * and store the details in the LibraryInfo class.
	 * */
	public int loadLibrary(){

		File home = null;
		if(directorySpecified){
			try {
				home = new File(new URI(MEDIA_PATH));
				int test = home.listFiles().length;     // This is to test that the directory is valid.
			}
			catch (Exception e) {
				Log.d("debuggg", "Nooo :(!");
				return -1;
			} 
		}

		LibraryInfo libInfo = new LibraryInfo(); // We need to create this object in order for the static objects in the class to get initialized.
		if(directorySpecified){
			loadSongsListFromDirectory(home);
		}
		else{
			loadSongsListContentResolver();
		}

		buildLibraryFromSongsList();

		return 0;

	}

	public void buildLibraryFromSongsList(){
		boolean albumFound;
		boolean artistFound;
		for (Song song : LibraryInfo.songsList){
			artistFound = false;
			for ( Artist currentArtist : LibraryInfo.artistsList){   // Add new artists to the artists list.
				if(currentArtist.name.equals( song.artist() ) ){
					artistFound = true;
					break;
				}
			}
			if (!artistFound){
				LibraryInfo.artistsList.add( new Artist( song.artist() ) );
				artistFound = false;
			}
			// If the album is not new, add the current song to the corresponding album object.
			albumFound = false;
			for(Album currentAlbum : LibraryInfo.albumsList){
				if(currentAlbum.title.equals( song.album() ) && currentAlbum.artist.equals( song.albumArtist() ) ){
					currentAlbum.addSong(song);
					albumFound = true;
					break;
				}
			}
			if(!albumFound){   // Album was not among current albums, so create a new album object.
				Album newAlbum = new Album(song.album());
				newAlbum.addSong(song);
				newAlbum.title = song.album();
				newAlbum.artist = song.albumArtist();
				LibraryInfo.albumsList.add(newAlbum);
			}


		}
		// Now we fill in all the Album lists in the Artist objects.

		for(Album currentAlbum : LibraryInfo.albumsList){
			for ( Artist currentArtist : LibraryInfo.artistsList){
				if(currentAlbum.artist.equals( currentArtist.name ) ){
					currentArtist.addAlbum(currentAlbum);
					break;
				}
			}
		}



		//
		//  Section this off later as an option!!!!!
		//
		//
		if(onlyAlbumArtists){

			// We now remove artists which do not own albums
			ArrayList<Artist> filtered = new ArrayList<Artist>(LibraryInfo.artistsList);
			for (Artist artist : LibraryInfo.artistsList){
				if (artist.albums.isEmpty()){
					filtered.remove(artist);
				}
			}
			LibraryInfo.artistsList = filtered;

		}

		// Sort the album tracks.
		for (Album album : LibraryInfo.albumsList){
			Collections.sort(album.songs);  // This may not be working, test!
		}
		// Sort the albums for each artist.
		for (int i=0 ; i<LibraryInfo.artistsList.size() ; i++){
			Collections.sort(LibraryInfo.artistsList.get(i).albums, new Comparator<Album>(){
				public int compare(Album one, Album two) {
					return one.title.compareTo(two.title);
				}  // This may not be working, test!
			});
		}
		// Sort artists alphabetically
		Collections.sort(LibraryInfo.artistsList, new Comparator<Artist>(){
			public int compare(Artist one, Artist two) {
				return one.name.compareTo(two.name);
			}
		});
		// Sort albums alphabetically
		Collections.sort(LibraryInfo.albumsList, new Comparator<Album>(){
			public int compare(Album one, Album two) {
				if ( one.title.compareTo(two.title) != 0){
					return one.title.compareTo(two.title);
				}
				else {
					return one.artist.compareTo(two.artist);
				}
			}
		});
	}

	public void loadSongsListContentResolver(){
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor cursor = contentResolver.query(uri, null, null, null, null);
		Utilities utils = new Utilities();
		if (cursor == null) {
			Log.d("debuggg", "Oh shit!");
			return;
		} else if (!cursor.moveToFirst()) {
			// no media on the device
			Log.d("debuggg", "Nothing found!!");
			return;
		} else {
			MediaPlayer mp = new MediaPlayer();
			do {
				int column_index = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA);
				String filepath = cursor.getString(column_index);
				Log.d("debuggg", "Filepath: " + filepath);

				// Optional loop to avoid non-song media files
				if(filteringOn){

					if(filepath.contains("/notifications/")
							|| filepath.contains("/Notifications/")
							|| filepath.contains("/Ringtones/")
							|| filepath.contains("/ringtones/")
							|| filepath.contains("/Android/data")
							){
						continue;
					}
				}

				HashMap<String, String> songData = new HashMap<String, String>();
				// Populate the hashmap with ID3 info.
				mmr.setDataSource(filepath);
				if(filepath != null)
					songData.put("songPath", filepath);
				else 
					continue;
				if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null)
					songData.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
				else 
					songData.put("songTitle", "<No Title>");
				if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null)
					songData.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
				else 
					songData.put("songArtist", "<No Artist>");
				if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) != null)
					songData.put("songAlbum", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
				else 
					songData.put("songAlbum", "<No Album>");
				if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) != null)
					songData.put("songAlbumArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
				else 
					songData.put("songAlbumArtist", null);
				if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) != null)
					songData.put("trackNumber", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
				else 
					songData.put("trackNumber", "0");

				// This adds the song durations to the HashMap
				try{
					mp.reset();
					mp.setDataSource(filepath);
					mp.prepare();
					songData.put("Duration", Integer.toString(mp.getDuration()));
				}catch(IOException e){
					songData.put("Duration", "error");
				}

				// Adding each song to SongList
				LibraryInfo.songsList.add(new Song(songData));
			} while (cursor.moveToNext());

			// Sort songs alphabetically
			Collections.sort(LibraryInfo.songsList, new Comparator<Song>(){
				public int compare(Song one, Song two) {
					return one.songData.get("songTitle").compareTo(two.songData.get("songTitle"));
				}
			});

			if(removeDuplicateSongs){
				// Remove duplicate songs
				ArrayList<Song> filtered = new ArrayList<Song>(LibraryInfo.songsList);

				int numberRemoved = 0;
				for(int i = 0; i < LibraryInfo.songsList.size() - 1; i++){
					if (LibraryInfo.songsList.get(i).equals(LibraryInfo.songsList.get(i+1))){
						filtered.remove(i+1 - numberRemoved);
						numberRemoved = numberRemoved + 1;
					}
				}
				LibraryInfo.songsList = filtered;
			}
		}
	}

	public void loadSongsListFromDirectory(File currentDirectory){
		if (currentDirectory.listFiles().length > 0) {

			for (File file : currentDirectory.listFiles()) {

				// Recursively calls itself on subdirectories
				if(file.isDirectory()){
					loadSongsListFromDirectory(file);
				}
				else if (file.getName().endsWith(".mp3") || file.getName().endsWith(".MP3")){
					// Checks if it has found an mp3, then loads data for the mp3 upon success.

					HashMap<String, String> songData = new HashMap<String, String>();
					String filepath= file.getPath();


					// Populate the hashmap with ID3 info.
					mmr.setDataSource(filepath);
					if(filepath != null)
						songData.put("songPath", filepath);
					else 
						continue;
					if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null)
						songData.put("songTitle", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
					else 
						songData.put("songTitle", "<No Title>");
					if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) != null)
						songData.put("songArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
					else 
						songData.put("songArtist", "<No Artist>");
					if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) != null)
						songData.put("songAlbum", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
					else 
						songData.put("songAlbum", "<No Album>");
					if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) != null)
						songData.put("songAlbumArtist", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
					else 
						songData.put("songAlbumArtist", null);
					if(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) != null)
						songData.put("trackNumber", mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
					else 
						songData.put("trackNumber", "0");
					//					try{
					//					mp.reset();
					//					mp.setDataSource(filepath);
					//					mp.prepare();
					//				}catch(IOException e){
					//					continue;
					//				}
					//				songData.put("Duration", Integer.toString(mp.getDuration()));

					// Adding each song to SongList
					LibraryInfo.songsList.add(new Song(songData));
				}
			}
		}
	}
}
