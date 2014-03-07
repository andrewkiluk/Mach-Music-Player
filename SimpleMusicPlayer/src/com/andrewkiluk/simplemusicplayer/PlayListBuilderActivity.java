package com.andrewkiluk.simplemusicplayer;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

public class PlayListBuilderActivity extends FragmentActivity implements
ActionBar.TabListener{

	private ViewPager viewPager;
	private PlayListBuilderAdapter mAdapter;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs = { "Songs", "Artists", "Albums" };

	private Button button_add_selected;
	private Button button_clear_selection;

	public ArrayList<Song> newSongs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist_builder);

		AppStatus.isVisible = true;

		ActionBar bar = getActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.tabcolor)));
		bar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.tabcolor)));

		// Initialization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getActionBar();
		mAdapter = new PlayListBuilderAdapter(getSupportFragmentManager());

		button_add_selected = (Button) findViewById(R.id.button_add_selected);
		button_clear_selection = (Button) findViewById(R.id.button_clear_selection);

		viewPager.setAdapter(mAdapter);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        



		// Adding Tabs
		for (String tab_name : tabs) {
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		button_add_selected.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getApplicationContext(), PlayListActivity.class);

				if (LibraryInfo.newSongs != null){

					for (Song song : LibraryInfo.newSongs){
						LibraryInfo.currentPlaylist.add(song);
					}
				}
					LibraryInfo.newSongs = new ArrayList<Song>();

				setResult(200, intent);
				// Closing PlayListBuilder
				LibraryInfo.newSongs = new ArrayList<Song>();
				finish();
			}
		});

		button_clear_selection.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LibraryInfo.newSongs = new ArrayList<Song>();
				mAdapter = new PlayListBuilderAdapter(getSupportFragmentManager());
				viewPager.setAdapter(mAdapter);


			}
		});
	}

	@Override
	protected void onStop() {
		AppStatus.isVisible = false;
		super.onStop();
	}


	@Override
	public void onBackPressed() {
		Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":"+viewPager.getCurrentItem());
		if (fragment != null) // could be null if not instantiated yet
		{
			if (fragment.getView() != null) {
				// Pop the backstack on the ChildManager if there is any. If not, close this activity as normal.
				if (!fragment.getChildFragmentManager().popBackStackImmediate()) {
					finish();
				}
			}
		}
	}


	// Holy fuck what is with this support library bullshit!!?? 

	//	@Override
	//	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	//		// TODO Auto-generated method stub
	//
	//	}
	//
	//	@Override
	//	public void onTabSelected(Tab tab, FragmentTransaction ft) {
	//		// show respective fragment view
	//		viewPager.setCurrentItem(tab.getPosition());
	//
	//	}
	//
	//	@Override
	//	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	//		// TODO Auto-generated method stub
	//
	//	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction arg1) {
		// show respective fragment view
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabSelected(Tab arg0, android.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabUnselected(Tab arg0, android.app.FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}
}