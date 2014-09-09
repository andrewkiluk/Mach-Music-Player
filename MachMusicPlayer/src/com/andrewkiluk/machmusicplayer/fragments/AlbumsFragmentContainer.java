package com.andrewkiluk.machmusicplayer.fragments;

import com.andrewkiluk.machmusicplayer.R;
import com.andrewkiluk.machmusicplayer.R.id;
import com.andrewkiluk.machmusicplayer.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AlbumsFragmentContainer extends Fragment {

	public AlbumsFragment albumsFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list_container, container, false);

		albumsFragment = new AlbumsFragment();
		Bundle albumArgs = new Bundle();
		albumArgs.putInt("artistPosition", -1);
		albumArgs.putString("origin", "albums");
		albumsFragment.setArguments(albumArgs);
		
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.list_frame, albumsFragment).commit(); 



		return rootView;
	}

}