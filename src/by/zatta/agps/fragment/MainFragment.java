package by.zatta.agps.fragment;

import by.zatta.agps.R;
import by.zatta.agps.dialog.ConfirmDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment implements OnClickListener {
	
	private Button INSTALL;
	private CheckBox SSL;
	private CheckBox ALT;
	private CheckBox NTP;
	private CheckBox XML;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.mainfragment_layout, container, false);
		INSTALL = (Button)v.findViewById(R.id.btnInstall);
		INSTALL.setOnClickListener(this);
		SSL = (CheckBox)v.findViewById(R.id.cbUseSSL);
		ALT = (CheckBox)v.findViewById(R.id.cbUseAlt);
		NTP = (CheckBox)v.findViewById(R.id.cbUseNtpDerek);
		XML = (CheckBox)v.findViewById(R.id.cbUseXmlTweak);
		NTP.setOnClickListener(this);
		return v;
	}

	public void setText(String item) {
		TextView view = (TextView) getView().findViewById(R.id.tvHeaderMainFragment);
		view.setText(item);
	}

	@Override
	public void onClick(View v) {
		String choises;
		if (SSL.isChecked()) choises = "ssl"; else choises = "no_ssl";
		if (ALT.isChecked()) choises = choises + " alt"; else choises = choises + " no_alt";
		if (NTP.isChecked()) choises = choises + " ntp"; else choises = choises + " no_ntp";
		if (XML.isChecked()) choises = choises + " xml"; else choises = choises + " no_xml";

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		switch(v.getId()){
		case R.id.btnInstall:			
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) ft.remove(prev);
			ft.addToBackStack(null);
			DialogFragment newFragment = ConfirmDialog.newInstance(choises);
			newFragment.show(ft, "dialog");
			break;
		case R.id.cbUseNtpDerek:
			Toast.makeText(getActivity().getBaseContext(), "PRO version only", Toast.LENGTH_LONG).show();
			// this will be a way to make this option payed.
			//NTP.setChecked(false);
			break;	
		case 13:
					
			break;
		}
	}
}
