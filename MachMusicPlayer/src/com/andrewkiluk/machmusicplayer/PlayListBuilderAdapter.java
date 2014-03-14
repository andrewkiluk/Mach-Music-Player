package com.andrewkiluk.machmusicplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class PlayListBuilderAdapter extends FragmentPagerAdapter {

	private FragmentManager fm;

	public PlayListBuilderAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			// Songs fragment activity
			return new SongsFragmentContainer();
		case 1:
			// Artists fragment activity
			return new ArtistsFragmentContainer();
		case 2:
			// Albums fragment activity
			return new AlbumsFragmentContainer();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}

	
	// Following methods may not be useful....
	
	public Fragment getActiveFragment(ViewPager container, int position) {
		String name = makeFragmentName(container.getId(), position);
		return  fm.findFragmentByTag(name);
	}

	private static String makeFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}

}