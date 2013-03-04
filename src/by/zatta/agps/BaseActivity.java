package by.zatta.agps;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import by.zatta.agps.R;
import by.zatta.agps.dialog.ConfirmDialog;
import by.zatta.agps.dialog.ChangeItemDialog.OnChangedListListener;
import by.zatta.agps.dialog.ConfirmDialog.OnDonateListener;
import by.zatta.agps.dialog.SliderDialog.OnPeriodicChangeListener;
import by.zatta.agps.fragment.MainFragment;
import by.zatta.agps.fragment.PrefFragment;
import by.zatta.agps.fragment.PrefFragment.OnLanguageListener;
import by.zatta.agps.model.ConfItem;
import by.zatta.agps.billing.IabHelper;
import by.zatta.agps.billing.IabResult;
import by.zatta.agps.billing.Inventory;
import by.zatta.agps.billing.Purchase;
import by.zatta.agps.assist.PreCheckLoader;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity implements OnChangedListListener, OnDonateListener, OnPeriodicChangeListener, OnLanguageListener, LoaderManager.LoaderCallbacks<Boolean>{
	static final String TAG = "BaseActivity";
	public static boolean DEBUG = true;
	public static boolean isUpdate = false;
	public static int mStars;
	public static boolean isPremium;
	IabHelper mHelper;
	static final int RC_REQUEST = 10001;
	public static String version;
	public static final String SKU_PREMIUM = "premium";
	public static final String SKU_EXTRA = "extra_donation_two";
	public static final String SKU_STAR_ONE = "first_star";
	public static final String SKU_STAR_TWO = "second_star";
	public static final String SKU_STAR_THREE = "third_star";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        version = myAppVersion();
        this.setTitle(getString(R.string.app_name) + " " + version);
        
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String language = getPrefs.getString("languagePref", "unknown");
        String oldVersion = getPrefs.getString("oldVersion", "0.0");
        if (!oldVersion.equals(version)) 
        	isUpdate = true;
        if (!language.equals("unknown")) makeLocale(language);
        DEBUG = getPrefs.getBoolean("enableDebugging", true);
    
        FragmentManager fm = getFragmentManager();
    	if (fm.findFragmentById(android.R.id.content) == null) {
            MainFragment main = new MainFragment();
            fm.beginTransaction().add(android.R.id.content, main, "main").commit();
            getLoaderManager().initLoader(0, null, this);
        }
        
        mHelper = new IabHelper(this);
        mHelper.enableDebugLogging(false);
        mHelper.startSetup(setupListener);
        
    }
    @Override
	public PreCheckLoader onCreateLoader(int arg0, Bundle arg1) {
    	return new PreCheckLoader(this);
	}
    
    @Override
	public void onLoaderReset(Loader<Boolean> arg0) {
		// TODO Auto-generated method stub
	}
    @Override
	public void onLoadFinished(Loader<Boolean> arg0, Boolean arg1) {
    	Fragment list = getFragmentManager().findFragmentByTag("main");
    	((MainFragment) list).showContent();
    	
	}

    
    @Override
    public void onDestroy() {
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
        super.onDestroy();
    }
    
    @Override
	public void onChangedListListener(List<ConfItem> items) {
		Fragment list = getFragmentManager().findFragmentByTag("main");
    	((MainFragment) list).resortList(items);
	}
    
    @Override
	public void onPeriodicListener(String time) {
    	Toast.makeText(this, time, Toast.LENGTH_LONG).show();
    	Fragment list = getFragmentManager().findFragmentByTag("main");
    	((MainFragment) list).updatePeriodicTimeOut(time);
	}
    
    @Override
	public void onDonateListener(String sku) {
    	mHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener);	
	}
    
    @Override
	public void onLanguageListener(String language) {
    	Log.w(TAG, language + " Listenener");
    	makeLocale(language);
    	FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		fm.popBackStack();		
		ft.replace(android.R.id.content, new PrefFragment(), "prefs");
		ft.addToBackStack(null);
		ft.commit();
	}
    
    @Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem star = menu.add("Star");
		star.setTitle("star");
		star.setIcon(mStars > 0 ? R.drawable.star : R.drawable.star_empty);
		star.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		MenuItem prefs = menu.add("Settings");
		prefs.setTitle("settings");
		prefs.setIcon(R.drawable.settings);
		prefs.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		return true;
	}
    
    public void makeLocale(String language){
    	Log.w(TAG, language + " makeLocale");
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
    	FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		if (item.getTitle().equals("star")){			
    			DialogFragment newFragment = ConfirmDialog.newInstance(new ArrayList<ConfItem>(), "just billing", false);
    			newFragment.show(ft, "dialog");    		
    	}else{
    		
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

    IabHelper.OnIabSetupFinishedListener setupListener = new IabHelper.OnIabSetupFinishedListener() {
        public void onIabSetupFinished(IabResult result) {
            Log.d(TAG, "Setup finished.");
            if (!result.isSuccess()) return;
            Log.d(TAG, "Setup successful. Querying inventory.");
            mHelper.queryInventoryAsync(mGotInventoryListener);
        }
    };
    
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) return;
            Log.d(TAG, "Query inventory was successful.");

            isPremium = inventory.hasPurchase(SKU_PREMIUM);
            if (inventory.hasPurchase(SKU_STAR_ONE)) mStars=1;
            if (inventory.hasPurchase(SKU_STAR_TWO)) mStars=2;
            if (inventory.hasPurchase(SKU_STAR_THREE)) mStars=3;
            if (isPremium) mStars=3;            
            
            invalidateOptionsMenu();
            Log.d(TAG, "User is " + (isPremium ? "PREMIUM" : "NOT PREMIUM") + " and has " + Integer.toString(mStars) + " stars.");

            if (inventory.hasPurchase(SKU_EXTRA)) {
                Log.d(TAG, "Still owning extra donation. consuming it");
                mHelper.consumeAsync(inventory.getPurchase(SKU_EXTRA), mConsumeFinishedListener);
                return;
            }
        }
    };
    
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) return;
            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_EXTRA))
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            else if (purchase.getSku().equals(SKU_PREMIUM)) isPremium = true;
            else if (purchase.getSku().equals(SKU_STAR_ONE)) mStars=1;
            else if (purchase.getSku().equals(SKU_STAR_TWO)) mStars=2;
            else if (purchase.getSku().equals(SKU_STAR_THREE)) mStars=3;
            if (isPremium) mStars = 3;
            
            invalidateOptionsMenu();
            Log.d(TAG, "Purchase conplete, purchased: " + purchase.getSku()); 
        }
    };
    
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            
            //Only for extra donations
            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
            }
            else {
            	Log.d(TAG, "Consumption failed. Provisioning.");
            }
            
            Log.d(TAG, "End consumption flow.");
        }
    };
   
}
