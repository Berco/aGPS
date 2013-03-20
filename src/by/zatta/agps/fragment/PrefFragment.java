package by.zatta.agps.fragment;

import by.zatta.agps.R;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;
import by.zatta.agps.assist.ShellProvider;
import by.zatta.agps.dialog.AboutDialog;

public class PrefFragment extends PreferenceFragment {
	
	private static final String TAG = "PrefFragment";
	OnLanguageListener languageListener;
	OnResetListener resetListener;
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            languageListener = (OnLanguageListener) activity;
            resetListener = (OnResetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement correct Listener");
        }
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Context context = this.getActivity().getLayoutInflater().getContext();
        setPreferenceScreen(createPreferenceHierarchy(context));
    }
	
	private PreferenceScreen createPreferenceHierarchy(Context mContext) {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(mContext);
		root.setKey("agps_preferences");
		
		PreferenceCategory launchPrefCat = new PreferenceCategory(mContext);
        launchPrefCat.setTitle(R.string.moreScreenTitle);
        root.addPreference(launchPrefCat);
		
        Preference infoScreenPref = getPreferenceManager().createPreferenceScreen(mContext);
		infoScreenPref.setTitle(R.string.AboutPrefTitle);
        infoScreenPref.setSummary(R.string.AboutPrefSummary);
        infoScreenPref.setKey("about_app_key");
        launchPrefCat.addPreference(infoScreenPref);
        
        Preference XdaPref = getPreferenceManager().createPreferenceScreen(mContext);
        XdaPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
        		.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(Uri.parse("http://forum.xda-developers.com/showthread.php?t=2198319")));
        XdaPref.setTitle(R.string.VisitXdaTitle);
        XdaPref.setSummary(R.string.VisitXdaSummary);
        XdaPref.setKey("visit_xda");        
        launchPrefCat.addPreference(XdaPref);
        
        Preference DgPref = getPreferenceManager().createPreferenceScreen(mContext);
        DgPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW)
        		.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(Uri.parse("http://derekgordon.com/android-how-tos/a-gps-worldwide-patch")));
        DgPref.setTitle(R.string.VisitDGcomTitle);
        DgPref.setSummary(R.string.VisitDVcomSummary);
        DgPref.setKey("vist_dg");        
        launchPrefCat.addPreference(DgPref);
        
        PreferenceCategory settingsPrefCat = new PreferenceCategory(mContext);
        settingsPrefCat.setTitle(R.string.SettingsCategory);
        root.addPreference(settingsPrefCat);
        
        Preference restorePref = getPreferenceManager().createPreferenceScreen(mContext);
		restorePref.setTitle(R.string.RestoreTitle);
        restorePref.setSummary(R.string.RestoreSummary);
        restorePref.setKey("restore_key");
        settingsPrefCat.addPreference(restorePref);
        
        Preference resetCustom = getPreferenceManager().createPreferenceScreen(mContext);
        resetCustom.setTitle(R.string.ResetCustomTitle);
        resetCustom.setSummary(R.string.ResetCustomSummary);
        resetCustom.setKey("reset_custom_key");
        settingsPrefCat.addPreference(resetCustom);
        
        CheckBoxPreference welcomeCheckBoxPref = new CheckBoxPreference(mContext);
        welcomeCheckBoxPref.setTitle(R.string.PrefWelcomeTitle);
        welcomeCheckBoxPref.setSummary(R.string.PrefWelcomeSummary);
        welcomeCheckBoxPref.setKey("showFirstUse");
        settingsPrefCat.addPreference(welcomeCheckBoxPref);
        
        ListPreference listPref = new ListPreference(mContext);
        listPref.setEntries(R.array.languages);
        listPref.setEntryValues(R.array.languages_short);
        listPref.setDialogTitle(R.string.LanguagePrefTitle);
        listPref.setKey("languagePref");
        listPref.setTitle(R.string.LanguagePrefTitle);
        listPref.setSummary(R.string.LanguagePrefSummary);
        settingsPrefCat.addPreference(listPref);
        
        CheckBoxPreference addonCheckBoxPref = new CheckBoxPreference(mContext);
        addonCheckBoxPref.setTitle(R.string.AddonScriptPrefTitle);
        addonCheckBoxPref.setSummary(R.string.AddonScriptPrefSummary);
        addonCheckBoxPref.setKey("enableAddonScript");
        addonCheckBoxPref.setChecked(true);
        if (ShellProvider.INSTANCE.isAddonable())
        	settingsPrefCat.addPreference(addonCheckBoxPref);
        
        CheckBoxPreference debugCheckBoxPref = new CheckBoxPreference(mContext);
        debugCheckBoxPref.setTitle(R.string.DebugPrefTitle);
        debugCheckBoxPref.setSummary(R.string.DebugPrefSummary);
        debugCheckBoxPref.setKey("enableDebugging");
        debugCheckBoxPref.setChecked(true);
        settingsPrefCat.addPreference(debugCheckBoxPref);
        
        return root;
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
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastRestore), Toast.LENGTH_LONG).show();
			ShellProvider.INSTANCE.mountRW(true);
			ShellProvider.INSTANCE.restore();
			ShellProvider.INSTANCE.mountRW(false);
			return true;
		}
		
		if (pref.getKey().contentEquals("reset_custom_key")){
			Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastReset), Toast.LENGTH_LONG).show();
			resetListener.onResetListener();
			return true;
		}
		
		if (pref.getKey().contentEquals("languagePref")){
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					languageListener.onLanguageListener(newValue.toString());
					return true;
				}	
			});			
		}
		
		if (pref.getKey().contentEquals("enableAddonScript")){
			ShellProvider.INSTANCE.mountRW(true);
			if (pref.getSharedPreferences().getBoolean("enableAddonScript", true)){
				Log.w(TAG, "enabled addon.d support");
				ShellProvider.INSTANCE.copyAddon();
			} else {
				Log.w(TAG, "disabled addon.d support");
				ShellProvider.INSTANCE.removeAddon();
			}
			ShellProvider.INSTANCE.mountRW(false);
			return true;
		}
		
		return true;
	}
	
	public interface OnLanguageListener{
		public void onLanguageListener(String language);
	}
	
	public interface OnResetListener{
		public void onResetListener();
	}
	
}
