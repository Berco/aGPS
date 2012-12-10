package by.zatta.agps.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import by.zatta.agps.BaseActivity;
import by.zatta.agps.R;
import by.zatta.agps.assist.ShellProvider;
import android.app.DialogFragment;
import android.os.Bundle;
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
	private String choises;
	
    public static ConfirmDialog newInstance(String choises) {
        ConfirmDialog f = new ConfirmDialog();
        Bundle args = new Bundle();
        args.putString("choises", choises);
        f.setArguments(args);
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        choises = getArguments().getString("choises");
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
        	
		tvTB.setTextColor(getResources().getColor(R.color.red));
		tvTB.setText("Just a temporarily confirmation text \n \n" +
				"what version we want to install, ssl or no-ssl etc like \n \n" + choises);
		
		return v;
    }
	
	@Override
	public void onClick(View v) {
		create_conf();
		switch (v.getId()){
		case R.id.btnNoInstall:
			Toast.makeText(getActivity().getBaseContext(), "Canceled", Toast.LENGTH_LONG).show();
			break;
		case R.id.btnYesAndReboot:
			Toast.makeText(getActivity().getBaseContext(), "Installing and Rebooting", Toast.LENGTH_LONG).show();
			try {				
				ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.agps/files/totalscript.sh install reboot");
			} catch (Exception e) {	}
				
			break;
		case R.id.btnYesNoReboot:
			Toast.makeText(getActivity().getBaseContext(), "Installing without Rebooting", Toast.LENGTH_LONG).show();
			try {
				ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.agps/files/totalscript.sh install no_reboot");
			} catch (Exception e) {	}
			
			break;
		}
		dismiss();
	}

	private void create_conf() {
		int a;
		String interPos;
		if (choises.contains("no_ntp")) a = R.array.standard_ntp; else a = R.array.special_ntp; 
		String[] ntp_array = getActivity().getBaseContext().getResources().getStringArray(a);
		if (choises.contains("no_ssl")) a = R.array.no_ssl; else a = R.array.use_ssl; 
		String[] ssl_array = getActivity().getBaseContext().getResources().getStringArray(a);
		if (choises.contains("no_alt")) interPos = "INTERMEDIATE_POS=1"; else interPos = "INTERMEDIATE_POS=0";  
		
		try
	    {
	    	File conf = new File(getActivity().getBaseContext().getFilesDir()+"/gps.conf");
	        conf.delete();
	        FileWriter w = new FileWriter(conf, true);
	    	
	        for (String ntp : ntp_array){
	        	w.append(ntp+'\n');
	        }
	        for (String ssl : ssl_array){
	        	w.append(ssl+'\n');
	        }
	        w.append(interPos+'\n');
	        
	        InputStream is = getResources().getAssets().open("fix_base/gps-base.conf");
            InputStreamReader ir = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(ir);

            String line;
            while ((line = br.readLine())!= null ) {
                w.append(line + '\n');
            }

            is.close();
	                	        
	        w.flush();
	        w.close();
	        if (BaseActivity.DEBUG)
	        	System.out.println("Wrote file:" + conf.getName() );
	    }catch(IOException e){}
	}

	
	
}

