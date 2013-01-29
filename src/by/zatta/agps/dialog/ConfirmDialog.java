package by.zatta.agps.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import by.zatta.agps.BaseActivity;
import by.zatta.agps.R;
import by.zatta.agps.assist.ShellProvider;
import by.zatta.agps.model.ConfItem;

public class ConfirmDialog extends DialogFragment 
	implements View.OnClickListener {
	
	static final String TAG = "ConfirmDialog";
	private TextView tvTB;
	private Button NO;
	private Button YESANDREBOOT;
	private Button YESNOREBOOT;
	private TextView headerAppName;
	private TextView headerUserName;
	private TextView buyNumberStars;
	private TextView buyYour;
	private ImageView starCounter;
	private RelativeLayout mBuyStars;
	private RelativeLayout mBuyPremium;
	private final int SCREEN_BILLING=1;
	private final int SCREEN_CONFIRM=2;
	private LinearLayout parent;
	private List<ConfItem> items;
	private CountDownTimer timer;
	private Boolean isForConfirmation;
	
	OnDonateListener donateListener;
	
	public static ConfirmDialog newInstance(List<ConfItem> list, Boolean forConfirmation) {
        ConfirmDialog f = new ConfirmDialog();
        
        Bundle args = new Bundle();
        args.putParcelableArrayList("lijst", (ArrayList<? extends Parcelable>) list);
        args.putBoolean("confirmation", forConfirmation);
        f.setArguments(args);
        
        return f;
    }
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            donateListener = (OnDonateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDonateListener");
        }
    }
	public interface OnDonateListener{
		public void onDonateListener(String sku);
	}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = getArguments().getParcelableArrayList("lijst");
        isForConfirmation = getArguments().getBoolean("confirmation", false);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
    	getDialog().setTitle(getString(R.string.ConfirmTitle));
        
    	View v = inflater.inflate(R.layout.confirm_dialog, container, false);
        parent = (LinearLayout) v.findViewById(R.id.parentViewConfirmDialog);
        
        headerAppName = (TextView) v.findViewById(R.id.headerAppName);
        headerUserName = (TextView) v.findViewById(R.id.headerUserName);
        starCounter = (ImageView) v.findViewById(R.id.ivStars);
        
        buyYour = (TextView) v.findViewById(R.id.tvBuyYour);
        buyNumberStars = (TextView) v.findViewById(R.id.tvNumberedStar);
        mBuyPremium = (RelativeLayout) v.findViewById(R.id.rlBuyPremium);
        mBuyStars = (RelativeLayout) v.findViewById(R.id.rlBuyStars);
    	mBuyStars.setOnClickListener(this);
    	mBuyPremium.setOnClickListener(this);
        
        tvTB = (TextView) v.findViewById(R.id.text);;
        NO = (Button)v.findViewById(R.id.btnNoInstall);
        YESANDREBOOT = (Button) v.findViewById(R.id.btnYesAndReboot);
        YESNOREBOOT = (Button) v.findViewById(R.id.btnYesNoReboot);
        
        YESANDREBOOT.setOnClickListener(this); 
        YESNOREBOOT.setOnClickListener(this);
        NO.setOnClickListener(this); 
        
    	if (isForConfirmation)
    		try { showTimer(seconds() * 1000); } catch (NumberFormatException e) { }
    	else{
    		getDialog().setTitle(getString(R.string.DonateTitle));
    		setScreen(SCREEN_BILLING);
    	}
    		
    		
    		
		return v;
    }
    
	@Override
	public void onDestroyView() {
		Log.d(TAG, "destroyed");
		if (timer != null) timer.cancel();
		super.onDestroyView();
	}
	
	/* Donator - no ntp - text is donated, why not use the DG server
     * Donator - ntp - text is donator, thanks for donating
     * Free - no ntp - text is why not donate and use DG servers
     * Free - ntp text is sorry, you have to be a donator to use these servers
     */
    private void setScreen(int SCREEN){
    	if (BaseActivity.mStars >= 1) {
    		headerAppName.setTextColor(getResources().getColor(R.color.star_yellow));
    		headerUserName.setTextColor(getResources().getColor(R.color.star_yellow));
    		headerUserName.setText(getString(R.string.StarredUser));
    	}
		if (BaseActivity.isPremium){
			headerAppName.setTextColor(getResources().getColor(R.color.premium_purple));
    		headerUserName.setTextColor(getResources().getColor(R.color.premium_purple));
    		headerUserName.setText(getString(R.string.PremiumUser));
			mBuyPremium.setVisibility(View.GONE);
			
		}
		if (BaseActivity.mStars == 1) {
			starCounter.setImageResource(R.drawable.star1);
			buyNumberStars.setText("Second star" );
		}
		if (BaseActivity.mStars == 2) {
			starCounter.setImageResource(R.drawable.star2);
			buyNumberStars.setText("Third star" );
		}
		if (BaseActivity.mStars == 3) {
			starCounter.setImageResource(R.drawable.star3);
			buyYour.setText("Do an extra");
			buyNumberStars.setText("Donation");
		}
		
    	switch (SCREEN){
    	
		case SCREEN_BILLING:
			parent.findViewById(R.id.screen_billing).setVisibility(View.VISIBLE);
			parent.findViewById(R.id.screen_confirm).setVisibility(View.GONE);
			parent.findViewById(R.id.screen_buttons).setVisibility(View.GONE);
			break;
			
		case SCREEN_CONFIRM:
			parent.findViewById(R.id.screen_billing).setVisibility(View.GONE);
			parent.findViewById(R.id.screen_confirm).setVisibility(View.VISIBLE);
			parent.findViewById(R.id.screen_buttons).setVisibility(View.VISIBLE);
    	}
    }
    private boolean canNTP(){
    	if (BaseActivity.mStars > 2 || BaseActivity.isPremium) return true;
        return false;
    }
    
    private boolean wantNTP(){
    	if (items.get(0).toString().contains("derekgordon")) return true;
    	return false;
    }
    
    private int seconds(){
    	int seconds = 21;
    	if (BaseActivity.mStars == 1) seconds = 11;
    	if (BaseActivity.mStars == 2) seconds = 6;
    	if (BaseActivity.mStars == 3 || BaseActivity.isPremium) seconds = 0;
    	return seconds;
    }
    
    private void showTimer(int countdownMillis) {
    	
    	  if(timer != null) timer.cancel();
    	  if (countdownMillis != 0){
    		  setScreen(SCREEN_BILLING);
    		  timer = new CountDownTimer(countdownMillis, 1000) {
    			  @Override
    			  public void onTick(long millisUntilFinished) {
    				  getDialog().setTitle(getString(R.string.Waiting)+(millisUntilFinished / 1000)+getString(R.string.seconds));
    			  }
    			  @Override
    			  public void onFinish() {
    				  getDialog().setTitle(getString(R.string.ConfirmTitle));
    				  setScreen(SCREEN_CONFIRM);
    				  setUI(canNTP(), wantNTP());;
    			  }
    		  }.start();
    	  }
    	  else {
    		  setScreen(SCREEN_CONFIRM);
    		  setUI(canNTP(), wantNTP());
    	  }
    	}
    
    private void setUI(boolean can, boolean want){
    	String text = null;
		YESANDREBOOT.setVisibility(View.VISIBLE);
		YESNOREBOOT.setVisibility(View.VISIBLE);
    	if (can && want) {
    		text  = "Thanks for supporting us! Enjoy your GPS!";
    		tvTB.setTextColor(getResources().getColor(R.color.green));
    	}
    	if (can && !want) {
    		text  = "Thanks for supporting us! You forgot to select Derek Gordon's server!";
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
    		tvTB.setTextColor(getResources().getColor(R.color.red));
    	}
		tvTB.setText(text);
    }
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()){
		case R.id.btnNoInstall:
			Toast.makeText(getActivity().getBaseContext(), "Canceled", Toast.LENGTH_LONG).show();
			break;
		case (R.id.rlBuyPremium):
			donateListener.onDonateListener(BaseActivity.SKU_PREMIUM);
			return;
		case (R.id.rlBuyStars):
			int s = BaseActivity.mStars;
			String sku = BaseActivity.SKU_EXTRA;
			if (s == 2) sku = BaseActivity.SKU_STAR_THREE;
			if (s == 1) sku = BaseActivity.SKU_STAR_TWO;
			if (s == 0) sku = BaseActivity.SKU_STAR_ONE;
			donateListener.onDonateListener(sku);
			return;
		case R.id.btnYesAndReboot:
			Toast.makeText(getActivity().getBaseContext(), "Installing and Rebooting", Toast.LENGTH_LONG).show();
			try {		
				String mSSL = create_conf();
				ShellProvider.INSTANCE.getCommandOutput("/data/data/by.zatta.agps/files/totalscript.sh install reboot " + mSSL);
			} catch (Exception e) {	}
				
			break;
		case R.id.btnYesNoReboot:
			Toast.makeText(getActivity().getBaseContext(), "Installing without Rebooting", Toast.LENGTH_LONG).show();
			try {
				String mSSL = create_conf();
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
            for (ConfItem item : items) {
            	w.append(item.toString() + '\n');
            	if (item.toString().contains("SUPL_TLS_CERT")) mSSL = "ssl";
            }
	                	        
	        w.flush();
	        w.close();
	        if (BaseActivity.DEBUG)
	        	System.out.println("Wrote file:" + conf.getName() );
	    }catch(IOException e){}
		return mSSL;
	}
	
}

