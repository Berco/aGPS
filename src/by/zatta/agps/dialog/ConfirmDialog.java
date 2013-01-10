package by.zatta.agps.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import by.zatta.agps.BaseActivity;
import by.zatta.agps.R;
import by.zatta.agps.assist.ShellProvider;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmDialog extends DialogFragment 
	implements View.OnClickListener {
	
	private TextView tvTB;
	private Button NO;
	private Button YESANDREBOOT;
	private Button YESNOREBOOT;
	private List<String> items;
	
	public static ConfirmDialog newInstance(List<String> apps) {
        ConfirmDialog f = new ConfirmDialog();
        
        Bundle args = new Bundle();
        args.putStringArrayList("lijst",  (ArrayList<String>) apps);
        f.setArguments(args);
        
        return f;
    }
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = getArguments().getStringArrayList("lijst");
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	getDialog().setTitle(getString(R.string.ConfirmTitle));
        View v = inflater.inflate(R.layout.confirm_dialog, container, false);
        
        tvTB = (TextView) v.findViewById(R.id.text);;
        
        NO = (Button)v.findViewById(R.id.btnNoInstall);
        YESANDREBOOT = (Button) v.findViewById(R.id.btnYesAndReboot);
        YESNOREBOOT = (Button) v.findViewById(R.id.btnYesNoReboot);
        
        YESANDREBOOT.setOnClickListener(this); 
        YESNOREBOOT.setOnClickListener(this);
        NO.setOnClickListener(this); 
        
        setUI(canNTP(), wantNTP());
		
		return v;
    }
    
    /* Donator - no ntp - text is donated, why not use the DG server
     * Donator - ntp - text is donator, thanks for donating
     * Free - no ntp - text is why not donate and use DG servers
     * Free - ntp text is sorry, you have to be a donator to use these servers
     */
    
    private boolean canNTP(){
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        int stars = sp.getInt("valueBugTrack", 1);
        boolean prem = sp.getBoolean("doBugTrack", false);
        if (stars > 1 || prem) return true;
        return false;
    }
    
    private boolean wantNTP(){
    	if (items.get(0).contains("derekgordon")) return true;
    	return false;
    }
    
    private void setUI(boolean can, boolean want){
    	String text = null;    	
    	if (can && want) {
    		text  = "Thanks for supporting us! Enjoy your GPS!";
    		tvTB.setTextColor(getResources().getColor(R.color.green));
    	}
    	if (can && !want) {
    		text  = "Thanks for supporting us! You forgot to select Derek Gordon's server!";
    		YESANDREBOOT.setVisibility(View.GONE);
    		YESNOREBOOT.setVisibility(View.GONE);
    		tvTB.setTextColor(getResources().getColor(R.color.green));
    	}
    	if (!can && !want) {
    		text  = "You can use the standard NTP servers but why not donate and get acces to the " +
    				"best of the best ntp server, maintained by Derek Gordon? For more information, please " +
    				"read the \"about\" (check the settings for this app)";
    		tvTB.setTextColor(getResources().getColor(R.color.ICS_blue));
    	}
    	
    	if (!can && want) {
    		text  = "No donator, sorry, you can not use the top notch DG server. Please " +
    				"read the about. Donate to get acces or feel free to use one of the local " +
    				"ntp servers and the standard google xtra.bin servers.";
    		YESANDREBOOT.setVisibility(View.GONE);
    		YESNOREBOOT.setVisibility(View.GONE);
    		tvTB.setTextColor(getResources().getColor(R.color.red));
    	}
    	
    	
		tvTB.setText(text);
    }
	
	@Override
	public void onClick(View v) {
		String mSSL = create_conf();
		
		switch (v.getId()){
		case R.id.btnNoInstall:
			Toast.makeText(getActivity().getBaseContext(), "Canceled", Toast.LENGTH_LONG).show();
			break;
		case R.id.btnYesAndReboot:
			Toast.makeText(getActivity().getBaseContext(), "Installing and Rebooting", Toast.LENGTH_LONG).show();
			try {				
				ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.agps/files/totalscript.sh install reboot " + mSSL);
			} catch (Exception e) {	}
				
			break;
		case R.id.btnYesNoReboot:
			Toast.makeText(getActivity().getBaseContext(), "Installing without Rebooting", Toast.LENGTH_LONG).show();
			try {
				ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.agps/files/totalscript.sh install no_reboot " + mSSL);
			} catch (Exception e) {	}
			
			break;
		}
		dismiss();
	}

	private String create_conf() {
		String mSSL = "no_ssl";
		try
	    {
	    	File conf = new File(getActivity().getBaseContext().getFilesDir()+"/gps.conf");
	        conf.delete();
	        FileWriter w = new FileWriter(conf, true);
	    	
	        InputStream is = getResources().getAssets().open("fix_base/gps-base.conf");
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                w.append(line + '\n');
            }
            is.close();
            for (String item : items) {
            	w.append(item + '\n');
            	if (item.contains("SUPL_TLS_CERT")) mSSL = "ssl";
            }
	                	        
	        w.flush();
	        w.close();
	        if (BaseActivity.DEBUG)
	        	System.out.println("Wrote file:" + conf.getName() );
	    }catch(IOException e){}
		return mSSL;
	}

	
	
}

