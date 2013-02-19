package by.zatta.agps.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import by.zatta.agps.R;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ChangelogDialog extends DialogFragment implements View.OnClickListener{
	
	public static ChangelogDialog newInstance() {
        ChangelogDialog f = new ChangelogDialog();
        return f;
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(false);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	getDialog().setTitle("Changelog");
        View v = inflater.inflate(R.layout.firstusedialog_layout, container, false);
        
        TextView tv = (TextView) v.findViewById(R.id.tvAbout);
        Button DISMISS = (Button)v.findViewById(R.id.btnDismiss);
        Button DONTSHOW = (Button) v.findViewById(R.id.btnDontShowAgain);
        DISMISS.setOnClickListener(this);
        DONTSHOW.setOnClickListener(this);
        DONTSHOW.setVisibility(View.GONE);
        Spanned inHtmlCC = Html.fromHtml(getAboutText());
        
        tv.setText(inHtmlCC);                
		
        return v;
    }
	
	public String getAboutText(){
		InputStream is= null;
        String filename = "texts/changelog.html";
        String about="";
        try {
			is = getResources().getAssets().open(filename);
			InputStreamReader ir = new InputStreamReader(is);
	        BufferedReader br = new BufferedReader(ir);
            String line;
            while ((line = br.readLine())!= null ) {
                about = about + line;
            }
			is.close();
		} catch (IOException e) {}
				
		return about;
	}

	@Override
	public void onClick(View v) {		
		PackageInfo pinfo;
		String currentVersion;
		try {
			pinfo = getActivity().getBaseContext().getPackageManager().getPackageInfo((getActivity().getBaseContext().getPackageName()), 0);
			currentVersion =  pinfo.versionName;
		} catch (NameNotFoundException e) {
			currentVersion =  "1.1.6";
		}
			SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
	    	Editor editor = getPrefs.edit();
	    	editor.putString("oldVersion", currentVersion);
	    	editor.commit();
			
		
		dismiss();
		
	}

}
