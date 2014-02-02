package com.andrewkiluk.simplemusicplayer;
 
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
 
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();

    }
    
//    @Override
//    protected void onActivityResult(int requestCode,
//                                     int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == 100){
//             library_location = data.getExtras().getInt("songIndex");
//             SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//        String library_location = sharedPrefs.getString("library_location", "NULL");
//        }
// 
//    }
    
    
    
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.layout.settings);
        }
    }

    
}