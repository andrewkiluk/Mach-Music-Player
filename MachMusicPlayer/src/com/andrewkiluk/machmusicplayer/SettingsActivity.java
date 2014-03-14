package com.andrewkiluk.machmusicplayer;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity { 
	//implements OnPreferenceClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppStatus.isVisible = true;
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new SettingsFragment()).commit();

	}

	@Override
	protected void onStop() {
		AppStatus.isVisible = false;
		super.onStop();
	}


	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.layout.settings);
		}
	}
}