package com.andrewkiluk.machmusicplayer.models;



public class SelectionStatus{
	
	public static boolean songsListSelection[];
	public static boolean albumsListSelection[][];
	public static boolean artistsListSelection[][][];
	
	public SelectionStatus(){
		songsListSelection = new boolean[LibraryInfo.songsList.size()];
		int maxAlbumSongs = 0;
		for(Album album : LibraryInfo.albumsList){
			if(album.songs.size() > maxAlbumSongs){
				maxAlbumSongs = album.songs.size();
			}
		}
		int maxAlbumNumber = 0;
		for(Artist artist: LibraryInfo.artistsList){
			if(artist.albums.size() > maxAlbumNumber){
				maxAlbumNumber = artist.albums.size();
			}
		}
		albumsListSelection = new boolean[LibraryInfo.albumsList.size()][maxAlbumSongs];
		artistsListSelection = new boolean[LibraryInfo.artistsList.size()][maxAlbumNumber][maxAlbumSongs];
		
	}
	
}