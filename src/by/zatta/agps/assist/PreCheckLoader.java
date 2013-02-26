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

    public class PreCheckLoader extends AsyncTaskLoader<Boolean> {
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
				if (!h.exists() || h.exists()){
					try {
						is = mContext.getResources().getAssets().open("fix_base/SuplRootCert");
						os = new FileOutputStream(data_storage_root+"/SuplRootCert");
						IOUtils.copy(is, os);
						is.close();
						os.flush();
						os.close();
						os = null;
					} catch (IOException e) {}
				}
			
				File j = new File(data_storage_root+"/busybox");
				if (!j.exists() || j.exists()){
					try {
						is = mContext.getResources().getAssets().open("scripts/busybox");
						os = new FileOutputStream(data_storage_root+"/busybox");
						IOUtils.copy(is, os);
						is.close();
						os.flush();
						os.close();
						os = null;
					} catch (IOException e) {}
					j.setExecutable(true, true);
					j.setReadable(true, true);
					j.setWritable(false, true);
				}
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
