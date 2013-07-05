package by.zatta.agps.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import by.zatta.agps.BaseActivity;
import by.zatta.agps.assist.ShellProvider;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

    public class PreCheckLoader extends AsyncTaskLoader<Boolean> {
    	private static final String TAG = "PreCheckLoader";
    	Context mContext;

        public PreCheckLoader(Context context) {
        	super(context);
        	mContext = context;
        }

        @Override public Boolean loadInBackground() {
        	ShellProvider.INSTANCE.lockToMyBusybox(Environment.getExternalStorageDirectory().getAbsolutePath());
        	if (BaseActivity.isUpdate){
        		String data_storage_root = mContext.getFilesDir().toString();
			
				InputStream is= null;
				OutputStream os = null;
						
				File h = new File(data_storage_root+"/SuplRootCert");
					if (h.exists()) h.setWritable(true, true);
					try {
						is = mContext.getResources().getAssets().open("fix_base/SuplRootCert");
						os = new FileOutputStream(data_storage_root+"/SuplRootCert");
						IOUtils.copy(is, os);
						is.close();
						os.flush();
						os.close();
						os = null;
						Log.d(TAG, "success copying certificate generic");
					} catch (IOException e) {
						Log.w(TAG, "failed copying certificate generic");
					}
					h.setExecutable(false, true);
					h.setReadable(true, true);
					h.setWritable(false, true);
				
				File i = new File(data_storage_root+"/SuplRootCertTmo");
					if (i.exists()) i.setWritable(true, true);
					try {
						is = mContext.getResources().getAssets().open("fix_base/SuplRootCertTmo");
						os = new FileOutputStream(data_storage_root+"/SuplRootCertTmo");
						IOUtils.copy(is, os);
						is.close();
						os.flush();
						os.close();
						os = null;
						Log.d(TAG, "success copying certificate tmo");
					} catch (IOException e) {
						Log.w(TAG, "failed copying certificate tmo");
					}
					i.setExecutable(false, true);
					i.setReadable(true, true);
					i.setWritable(false, true);
			
				File j = new File(data_storage_root+"/busybox");
					if (j.exists()) j.setWritable(true, true);
					try {
						is = mContext.getResources().getAssets().open("scripts/busybox");
						os = new FileOutputStream(data_storage_root+"/busybox");
						IOUtils.copy(is, os);
						is.close();
						os.flush();
						os.close();
						os = null;
						Log.d(TAG, "succes copying busybox");
					} catch (IOException e) {
						Log.w(TAG, "failed copying busybox");
					}
					j.setExecutable(true, true);
					j.setReadable(false, true);
					j.setWritable(false, true);
					
				File l = new File(data_storage_root+"/67-topntp.sh");
					if (l.exists()) l.setWritable(true, true);
					try {
						is = mContext.getResources().getAssets().open("scripts/67-topntp.sh");
						os = new FileOutputStream(data_storage_root+"/67-topntp.sh");
						IOUtils.copy(is, os);
						is.close();
						os.flush();
						os.close();
						os = null;
						Log.d(TAG, "succes copying 67-topntp.sh");
					} catch (IOException e) {
						Log.w(TAG, "failed copying 67-topntp.sh");
					}
					l.setExecutable(false, true);
					l.setReadable(true, true);
					l.setWritable(false, true);
				
				ShellProvider.INSTANCE.replaceAddon();
				ShellProvider.INSTANCE.backup();
			
		    	SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		    	Editor editor = getPrefs.edit();
		    	editor.putString("oldVersion", BaseActivity.version);
		    	editor.commit();
        	}
            return true;
        }
      	    
        @Override public void deliverResult(Boolean succes) {
            super.deliverResult(succes);
        }

        @Override protected void onStartLoading() {
            forceLoad();
        }

        @Override protected void onStopLoading() {
            cancelLoad();
        }

        @Override public void onCanceled(Boolean succes) {
            super.onCanceled(succes);
        }

        @Override protected void onReset() {
            super.onReset();
            onStopLoading();
        }
    }
