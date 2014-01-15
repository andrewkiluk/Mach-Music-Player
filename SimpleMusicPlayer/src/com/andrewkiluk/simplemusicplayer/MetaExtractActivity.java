package com.andrewkiluk.simplemusicplayer;
 
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
 
public class MetaExtractActivity extends Activity {
    ImageView album_art;
    TextView album, artist, genre;
 
    MediaMetadataRetriever metaRetriver;
    byte[] art;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInit();
 
        // Album_art retrieval code //
 
        metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource("/sdcard/audio.mp3");
        try {
            art = metaRetriver.getEmbeddedPicture();
            Bitmap songImage = BitmapFactory
                    .decodeByteArray(art, 0, art.length);
            album_art.setImageBitmap(songImage);
            album.setText(metaRetriver
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            artist.setText(metaRetriver
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            genre.setText(metaRetriver
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE));
        } catch (Exception e) {
            album_art.setBackgroundColor(Color.GRAY);
            album.setText("Unknown Album");
            artist.setText("Unknown Artist");
            genre.setText("Unknown Genre");
        }
 
    }
 
    // Fetch Id's form xml
 
    public void getInit() {
 
        album_art = (ImageView) findViewById(R.id.albumFrame);
        album = (TextView) findViewById(R.id.songAlbum);
        artist = (TextView) findViewById(R.id.songArtist);
 
    }
}