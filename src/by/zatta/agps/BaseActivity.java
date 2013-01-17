package by.zatta.agps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;

import by.zatta.agps.R;
import by.zatta.agps.assist.ShellProvider;
import by.zatta.agps.dialog.ChangeItemDialog.OnChangedListListener;
import by.zatta.agps.fragment.MainFragment;
import by.zatta.agps.fragment.PrefFragment;
import by.zatta.agps.model.ConfItem;
import by.zatta.agps.billing.BillingActivity;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BaseActivity extends Activity implements OnChangedListListener{
	public static boolean DEBUG = true;
	private int mStars;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String version = myAppVersion();
        this.setTitle(getString(R.string.app_name) + " " + version);
        
        ShellProvider.INSTANCE.isSuAvailable();
        new PlantFiles().execute();
               
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mStars = getPrefs.getInt("valueBugTrack", 1);
        String language = getPrefs.getString("languagePref", "unknown");
        if (!language.equals("unknown")) makeLocale(language);
        
        //DEBUG = getPrefs.getBoolean("enableDebugging", false);
        
        FragmentManager fm = getFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            MainFragment main = new MainFragment();
            fm.beginTransaction().add(android.R.id.content, main, "main").commit();
        }
    }
    
    @Override
	public void onChangedListListener(List<ConfItem> items) {
		Fragment list = getFragmentManager().findFragmentByTag("main");
    	((MainFragment) list).resortList(items);
		
	}
    
    @Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater blowUp = getMenuInflater();
		MenuItem item = menu.add("Star");
		item.setTitle("star");
		item.setIcon(mStars > 0 ? R.drawable.star : R.drawable.star_empty);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		blowUp.inflate(R.menu.leftclick_optionchooser, menu);
		return true;
	}
    
    public void makeLocale(String language){
        Locale locale = new Locale(language); 
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, 
        getBaseContext().getResources().getDisplayMetrics());
    }
    
    public String myAppVersion(){
		PackageInfo pinfo;
		try {
			pinfo = this.getPackageManager().getPackageInfo((this.getPackageName()), 0);
			return pinfo.versionName;
		} catch (NameNotFoundException e) {
			return " ";
		}
		
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("star")){
    		Intent i = new Intent(BaseActivity.this, BillingActivity.class);
			startActivityForResult(i,14);	
    	}else{
    		FragmentManager fm = getFragmentManager();
    		FragmentTransaction ft = fm.beginTransaction();
    		Fragment pref = getFragmentManager().findFragmentByTag("prefs");
    		if (pref == null){
    			ft.replace(android.R.id.content, new PrefFragment(), "prefs");
    			ft.addToBackStack(null);
    			ft.commit();		
    		}else{
    			ft.remove(getFragmentManager().findFragmentByTag("prefs"));
    			ft.commit();
    			fm.popBackStack();
    		}
    	}
		return false;
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			Bundle basket = data.getExtras();
			int s = basket.getInt("answer");
			mStars = s;
			invalidateOptionsMenu();
		}
	}
    
    private class PlantFiles extends AsyncTask<Void, Void, Void> {
    	
		@Override
		protected Void doInBackground(Void... arg0) {
					
			String data_storage_root = getBaseContext().getFilesDir().toString();
			
			InputStream is= null;
			OutputStream os = null;
						
			File h = new File(data_storage_root+"/SuplRootCert");
			if (!h.exists() || h.exists()){
				try {
					is = getResources().getAssets().open("fix_base/SuplRootCert");
					os = new FileOutputStream(data_storage_root+"/SuplRootCert");
					IOUtils.copy(is, os);
					is.close();
					os.flush();
					os.close();
					os = null;
				} catch (IOException e) {}
			}
			File i = new File(data_storage_root+"/totalscript.sh");
			if (!i.exists() || i.exists()){
				try {
					is = getResources().getAssets().open("scripts/totalscript.sh");
					os = new FileOutputStream(data_storage_root+"/totalscript.sh");
					IOUtils.copy(is, os);
					is.close();
					os.flush();
					os.close();
					os = null;
					ShellProvider.INSTANCE.getCommandOutput("chmod 740 "+data_storage_root+"/totalscript.sh");
				} catch (IOException e) {}
			}
			return null;
		}
		
		
	}

}
