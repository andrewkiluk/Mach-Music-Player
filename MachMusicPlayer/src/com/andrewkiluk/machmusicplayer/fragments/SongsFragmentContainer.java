package com.andrewkiluk.machmusicplayer.fragments;

import com.andrewkiluk.machmusicplayer.R;
import com.andrewkiluk.machmusicplayer.R.id;
import com.andrewkiluk.machmusicplayer.R.layout;
import com.andrewkiluk.machmusicplayer.models.LibraryInfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SongsFragmentContainer extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list_container, container, false);
		if(LibraryInfo.songsList.size() > 0){
			SongsFragment songsFragment = new SongsFragment();
			Bundle songArgs = new Bundle();
			songArgs.putInt("artistPosition", -1);
			songArgs.putInt("albumPosition", -1);
			songArgs.putString("origin", "songs");
			songsFragment.setArguments(songArgs);
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.replace(R.id.list_frame, songsFragment).commit(); 
		}


		return rootView;
	}

}