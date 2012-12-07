package by.zatta.agps.fragment;

import by.zatta.agps.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainFragment extends Fragment implements OnClickListener {
	
	private Button INSTALL;
	
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
		return v;
	}

	public void setText(String item) {
		TextView view = (TextView) getView().findViewById(R.id.tvHeaderMainFragment);
		view.setText(item);
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(getActivity().getBaseContext(), "CLICK!! Check the settings also!", Toast.LENGTH_LONG).show();
		
	}
}
