package by.zatta.agps.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import by.zatta.agps.R;
import by.zatta.agps.assist.DatabaseHelper;
import by.zatta.agps.dialog.ConfirmDialog;

public class MainFragment extends Fragment implements OnClickListener, OnItemSelectedListener {
	
	private Button INSTALL;
	private LinearLayout mLinLayFlashView;
	private Cursor c=null;
	private Spinner mSpRegion;
	private Spinner mSpPool;
	private Spinner mSpProfile;
	private ScrollView mScrollView;
	DatabaseHelper myDbHelper;
	
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
		mLinLayFlashView = (LinearLayout)v.findViewById(R.id.llShowConf);
		mSpRegion = (Spinner)v.findViewById(R.id.spRegion);
		mSpRegion.setOnItemSelectedListener(this);
		mSpPool = (Spinner)v.findViewById(R.id.spPool);
		mSpPool.setOnItemSelectedListener(this);
		mSpProfile = (Spinner)v.findViewById(R.id.spProfile);
		mSpProfile.setOnItemSelectedListener(this);
		mScrollView = (ScrollView)v.findViewById(R.id.scrollView1);
		mScrollView.setOnClickListener(this);
		myDbHelper = new DatabaseHelper(getActivity().getBaseContext());
		try { 
			myDbHelper.createDataBase();
        	myDbHelper.openDataBase();
        }catch(Exception e){ }
		fillRegionSpinner();
		fillPoolSpinner("europe_name");
		fillProfileSpinner();
		getItemsFromDatabase();
		
		return v;
	}

	@Override
	public void onClick(View v) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		switch(v.getId()){
		case R.id.btnInstall:
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) ft.remove(prev);
			ft.addToBackStack(null);
			DialogFragment newFragment = ConfirmDialog.newInstance(getItemsFromDatabase());
			newFragment.show(ft, "dialog");
			break;
		case R.id.scrollView1:
			Toast.makeText(getActivity().getBaseContext(), "scroll!!", Toast.LENGTH_SHORT).show();
			break;
		}
	}
	

	public void fillPoolSpinner(String region){
		String array[] = { region };        
        List<String> labels = new ArrayList<String>();
        c= myDbHelper.query("pools", array , null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
            	if (!c.getString(0).equals("{null}"))
                labels.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();        
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPool.setAdapter(dataAdapter);
        mSpPool.setSelection(1);
	}
	public void fillProfileSpinner(){
        List<String> labels = new ArrayList<String>();
        c=myDbHelper.query("items", null, null, null, null,null, null);       
        if (c.moveToFirst()) {
        	for (int i = 2; i < c.getColumnCount(); i++){
        		labels.add(c.getString(i));
        		}
        }
        c.close();        
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpProfile.setAdapter(dataAdapter);
	}
	public void fillRegionSpinner(){
		String array[] = { "world_name", "europe_name", "oceania_name",
				"north_america_name", "south_america_name", 
				"asia_name", "africa_name" };        
        List<String> labels = new ArrayList<String>();
        c= myDbHelper.query("pools", array , null, null, null, null, null);
 
        if (c.moveToFirst()) {
        	for (int i = 0; i < c.getColumnCount(); i++){
        		labels.add(c.getString(i));
        		}
        }
        c.close();        
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpRegion.setAdapter(dataAdapter);
	}
	
	
	
	public List<String> getItemsFromDatabase(){
		mLinLayFlashView.removeAllViews();           
        addFormField("gps.conf:", true);
        List<String>confItems = getFromProfileSpinner();
        for (String agps : getAgpsFromDatabase()){
        	confItems.add(0, agps);
        }
        confItems.add(0, getFromPoolSpinner());
        
        for (int i = 0; i < confItems.size(); i++){
        	addFormField(confItems.get(i), false);
        }
        return confItems;
	}
	
	private List<String> getAgpsFromDatabase(){
		List<String>agpsItems = new ArrayList<String>();
		String agpsType =  "GOOGLE";
		if (mSpPool.getSelectedItem().toString().toLowerCase().contains("derek"))
			agpsType = "DEREK";
		String[] array = { "ITEMS", agpsType };
		c=myDbHelper.query("agps", array, null, null, null,null, null);
        if(c.moveToPosition(1)) {
        	do {
        		if (!c.getString(0).equals("{null}") && !c.getString(1).equals("{null}"))
        		agpsItems.add(c.getString(0) + "=" + c.getString(1));
        	} while (c.moveToNext());
        }
		return agpsItems;
	}
	
	private String getFromPoolSpinner(){
		String fromPoolSpinner = "NTP_SERVER=";
		String nowInPoolSpinner = mSpPool.getSelectedItem().toString();  
        c=myDbHelper.query("pools", null, null, null, null,null, null);
        if(c.moveToFirst()) {
        		do {        		        		
            		for (int i = 1; i < c.getColumnCount(); i++){
            			if (c.getString(i).equals(nowInPoolSpinner))
            				fromPoolSpinner = fromPoolSpinner + c.getString(i-1);
                		}
            	} while (c.moveToNext());
        }
		return fromPoolSpinner;
	}
	
	private List<String> getFromProfileSpinner(){
		List<String> profileList = new ArrayList<String>();
		String profile = mSpProfile.getSelectedItem().toString().toUpperCase().replace(".", "");
		String array[] = { "ITEMS", profile }; 
		c=myDbHelper.query("items", array, null, null, null,null, null);
        if(c.moveToPosition(2)) {
        	do {
        		if (!c.getString(0).equals("{null}") && !c.getString(1).equals("{null}"))
        		profileList.add(c.getString(0) + "=" + c.getString(1));
        	} while (c.moveToNext());
        }		
		return profileList;
	}
	
	private void addFormField(String label, Boolean isLabel) {
		TextView tvLabel = new TextView(getActivity().getBaseContext());
		tvLabel.setLayoutParams(getDefaultParams(isLabel));
		tvLabel.setText(label);
		tvLabel.setHorizontallyScrolling(false);
		mLinLayFlashView.addView(tvLabel);	
	}

	private LayoutParams getDefaultParams(boolean isLabel) {
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		if (isLabel) {
			params.topMargin = 10;
		}else
			params.leftMargin = 10;
		return params;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long arg3) {
		switch (parent.getId()){
		case R.id.spRegion:
	        String pool = "world_name";
	        if (parent.getItemAtPosition(position).toString().equals("Europe")) pool = "europe_name";
	        if (parent.getItemAtPosition(position).toString().equals("Oceania")) pool = "oceania_name";
	        if (parent.getItemAtPosition(position).toString().equals("North America")) pool = "north_america_name";
	        if (parent.getItemAtPosition(position).toString().equals("South America")) pool = "south_america_name";
	        if (parent.getItemAtPosition(position).toString().equals("Asia")) pool = "asia_name";
	        if (parent.getItemAtPosition(position).toString().equals("Africa")) pool = "africa_name";
	        fillPoolSpinner(pool);
			break;
		case R.id.spPool:
	        getItemsFromDatabase();
			break;
		case R.id.spProfile:
			getItemsFromDatabase();
			break;
		}		
	}
	@Override public void onNothingSelected(AdapterView<?>arg0){}
}
