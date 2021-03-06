package by.zatta.agps.dialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import by.zatta.agps.R;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {
	
	public static AboutDialog newInstance() {
        AboutDialog f = new AboutDialog();
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
    	getDialog().setTitle(getString(R.string.AboutTitle));
        View v = inflater.inflate(R.layout.aboutdialog_layout, container, false);
        
        TextView tv = (TextView) v.findViewById(R.id.tvAbout);
               
        Spanned inHtmlCC = Html.fromHtml(getAboutText());
                
        tv.setText(inHtmlCC);
                
		return v;
    }
	public String getAboutText(){
		InputStream is= null;
		
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
		String language = getPrefs.getString("languagePref", "unknown");
        Locale locale = Locale.getDefault();
        String myLocale = locale.getLanguage();
        String filename = "texts/background_en.html";
        if (myLocale.contains("fr") || language.contains("fr"))
			filename = "texts/background_fr.html";
        if (myLocale.contains("nl") || language.contains("nl"))
			filename = "texts/background_nl.html";
        if (myLocale.contains("de") || language.contains("de"))
			filename = "texts/background_de.html";
        
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

}
