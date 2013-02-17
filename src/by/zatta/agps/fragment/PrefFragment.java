package by.zatta.agps.fragment;

import by.zatta.agps.R;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import by.zatta.agps.assist.ShellProvider;
import by.zatta.agps.dialog.AboutDialog;

public class PrefFragment extends PreferenceFragment {
	    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen,
			Preference pref) {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();	
		Fragment about = getFragmentManager().findFragmentByTag("dialog");
		
		if (about != null) ft.remove(about);
		ft.addToBackStack(null);
		
		if (pref.getKey().contentEquals("about_app_key")){
			DialogFragment aboutFragment = AboutDialog.newInstance();
			aboutFragment.show(ft, "dialog");
			return true;
		}
		
		if (pref.getKey().contentEquals("restore_key")){
			Toast.makeText(getActivity().getApplicationContext(), "RESTORE", Toast.LENGTH_LONG).show();
			String script = getActivity().getBaseContext().getFilesDir().toString()+"/totalscript.sh ";
			String external_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
			ShellProvider.INSTANCE.getCommandOutput(script +  "restore " + external_storage);
		}
		
		return false;
	}
	
}
