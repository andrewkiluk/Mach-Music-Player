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

public class ArtistsFragmentContainer extends Fragment {

	private ArtistsFragment artistsFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list_container, container, false);
		
		artistsFragment = new ArtistsFragment();
		Bundle artistArgs = new Bundle();
		artistArgs.putString("origin", "albums");
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.list_frame, artistsFragment).commit(); 



		return rootView;
	}

}