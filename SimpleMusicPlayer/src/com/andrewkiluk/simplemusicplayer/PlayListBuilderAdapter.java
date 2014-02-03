package com.andrewkiluk.simplemusicplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class PlayListBuilderAdapter extends FragmentPagerAdapter {
	 
    public PlayListBuilderAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            // Top Rated fragment activity
            return new SongsFragment();
        case 1:
            // Games fragment activity
            return new ArtistsFragment();
        case 2:
            // Movies fragment activity
            return new AlbumsFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }
 
}