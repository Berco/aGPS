package by.zatta.agps.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import by.zatta.agps.R;
import by.zatta.agps.assist.DatabaseHelper;
import by.zatta.agps.assist.ShellProvider;
import by.zatta.agps.dialog.ChangeItemDialog;
import by.zatta.agps.dialog.ConfirmDialog;
import by.zatta.agps.dialog.SliderDialog;
import by.zatta.agps.model.ConfItem;
import by.zatta.agps.model.ConfItemListAdapter;

public class MainFragment extends ListFragment implements OnClickListener, OnItemSelectedListener {
	
	private Button INSTALL;
	private Cursor c=null;
	private Spinner mSpRegion;
	private Spinner mSpPool;
	private Spinner mSpProfile;
	DatabaseHelper myDbHelper;
	private ListView mList;
	private LinearLayout mLinLayConfigXml;
	private TextView mPeriodicText;
	private String TIME="none";
	private ConfItemListAdapter mConfAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.mainfragment_layout, container, false);
		INSTALL = (Button)v.findViewById(R.id.btnInstall);
		INSTALL.setOnClickListener(this);
		mSpRegion = (Spinner)v.findViewById(R.id.spRegion);
		mSpRegion.setOnItemSelectedListener(this);
		mSpPool = (Spinner)v.findViewById(R.id.spPool);
		mSpPool.setOnItemSelectedListener(this);
		mSpProfile = (Spinner)v.findViewById(R.id.spProfile);
		mSpProfile.setOnItemSelectedListener(this);
		mLinLayConfigXml = (LinearLayout) v.findViewById(R.id.llConfigXml);
		mLinLayConfigXml.setOnClickListener(this);
		mPeriodicText = (TextView) v.findViewById(R.id.tvPeriodicText);
		
		myDbHelper = new DatabaseHelper(getActivity().getBaseContext(),myAppCode());
		try { 
			myDbHelper.createDataBase();
        	myDbHelper.openDataBase();
        }catch(Exception e){ }
		
		return v;
	}
	
	public int myAppCode(){
		PackageInfo pinfo;
		try {
			pinfo = getActivity().getBaseContext().getPackageManager().getPackageInfo((getActivity().getBaseContext().getPackageName()), 0);
			return pinfo.versionCode;
		} catch (NameNotFoundException e) {
			return 1;
		}
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mList = (ListView) getListView();
		registerForContextMenu(mList);
		setHasOptionsMenu(true);
		if ( mConfAdapter == null){
			mConfAdapter = new ConfItemListAdapter(getActivity());
			setListAdapter(mConfAdapter);
		}
		checkForConfig();
		fillRegionSpinner();
		fillPoolSpinner("Global");
		fillProfileSpinner();
		getItemsFromDatabase();

	}

	@Override
	public void onClick(View v) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		switch(v.getId()){
		case R.id.btnInstall:
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) ft.remove(prev);
			ft.addToBackStack(null);
			DialogFragment newFragment = ConfirmDialog.newInstance(getItemsFromDatabase(), TIME , true);
			newFragment.show(ft, "dialog");
			break;
		case R.id.llConfigXml:
			Fragment previ = getFragmentManager().findFragmentByTag("dialog");
			if (previ != null) ft.remove(previ);
			ft.addToBackStack(null);
			DialogFragment newSlider = SliderDialog.newInstance(TIME);
			newSlider.show(ft, "dialog");
			break;
		}
	}
	
	private void checkForConfig() {
		String script = getActivity().getBaseContext().getFilesDir().toString()+"/totalscript.sh ";
		String check = ShellProvider.INSTANCE.getCommandOutput(script+"configexists");
		if (!check.contentEquals("")){
			SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
	        TIME = getPrefs.getString("TIME", "5");
			mPeriodicText.setText("PeriodicTimeOutSec="+TIME);
			mLinLayConfigXml.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ConfItem item = (ConfItem) getListAdapter().getItem(position);
		if (id > 4){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) ft.remove(prev);
			ft.addToBackStack(null);
			DialogFragment newFragment = ChangeItemDialog.newInstance(getItemGroup(item));
			newFragment.show(ft, "dialog");
		} else {
			Toast.makeText(getActivity().getBaseContext(), item.toString(), Toast.LENGTH_SHORT).show();
		}
	}
	

	public void fillPoolSpinner(String continent){
        List<String> labels = new ArrayList<String>();
        c= myDbHelper.getCountries(continent);
        if (c.moveToFirst()) {
            do {
                labels.add(c.getString(2));
                
            } while (c.moveToNext());
        }
        c.close();        
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPool.setAdapter(dataAdapter);
	}
	public void fillRegionSpinner(){
        List<String> labels = new ArrayList<String>();
        c= myDbHelper.getRegions();
        if(c.moveToPosition(1)) {
        	do {
        		labels.add(c.getString(0));
        	} while (c.moveToNext());
        }
        c.close();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpRegion.setAdapter(dataAdapter);
	}
	public void fillProfileSpinner(){
        List<String> labels = new ArrayList<String>();
        c=myDbHelper.query("items", null, null, null, null,null, null);       
        if (c.moveToFirst()) {
        	for (int i = 4; i < c.getColumnCount(); i++){
        		if (!c.getString(i).equals("{null}")) labels.add(c.getString(i));
        		}
        }
        c.close(); 
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpProfile.setAdapter(dataAdapter);
	}
	
	public List<ConfItem> getItemGroup(ConfItem item){
		List<ConfItem> sectionItems = new ArrayList<ConfItem>();
		String profile = myDbHelper.getColumnNameFor(mSpProfile.getSelectedItem().toString());
		String array[] = { "ITEMS","SECTION","TYPE","DISCRIPTION",profile }; 
		c=myDbHelper.query("items", array, null, null, null,null, null);
        if(c.moveToPosition(1)) {
        	do {
        		if (!c.getString(0).equals("{null}") && 
        			!c.getString(4).equals("{null}") &&
        			c.getString(1).equals(item.getSection()))
        			sectionItems.add(new ConfItem(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
        	} while (c.moveToNext());
        }
        c.close();
		return sectionItems;
	}
	
	
	public List<ConfItem> getItemsFromDatabase(){
        List<ConfItem>confItems = getFromProfileSpinner();
        for (ConfItem agps : getAgpsFromDatabase())
        	confItems.add(0, agps);
        
        confItems.add(0, getFromPoolSpinner());
       	mConfAdapter.setData(confItems);
        return confItems;
	}
	
	private List<ConfItem> getAgpsFromDatabase(){
		List<ConfItem> ntpItems = new ArrayList<ConfItem>();
		String agpsType =  "GOOGLE";
		if (mSpPool.getSelectedItem().toString().toLowerCase().contains("derek"))
			agpsType = "DEREK";
		String[] array = { "ITEMS", agpsType };
		c=myDbHelper.query("agps", array, null, null, null,null, null);
        if(c.moveToPosition(1)) {
        	do {
        		if (!c.getString(0).equals("{null}") && !c.getString(1).equals("{null}"))
        			ntpItems.add(new ConfItem(c.getString(0),null,null,null, c.getString(1)));
        	} while (c.moveToNext());
        }
        c.close();
		return ntpItems;
	}
	
	private ConfItem getFromPoolSpinner(){
        ConfItem item = new ConfItem("NTP_SERVER", null, null,null,myDbHelper.getPoolfromSpinner(mSpPool.getSelectedItem().toString()));
        return item;
	}
	
	private List<ConfItem> getFromProfileSpinner(){
		List<ConfItem> itemsList = new ArrayList<ConfItem>();
		String profile = myDbHelper.getColumnNameFor(mSpProfile.getSelectedItem().toString());
		String array[] = { "ITEMS","SECTION","TYPE","DISCRIPTION",profile }; 
		c=myDbHelper.query("items", array, null, null, null,null, null);
		if(c.moveToPosition(1)) {
			do {
				if (!c.getString(0).equals("{null}") && !c.getString(4).equals("{null}"))
					itemsList.add(new ConfItem(c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
			} while (c.moveToNext());
		}
		c.close();
		return itemsList;
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long arg3) {
		switch (parent.getId()){
		case R.id.spRegion:
	        fillPoolSpinner(parent.getItemAtPosition(position).toString());
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

	public void resortList(List<ConfItem> changedItems) {
		myDbHelper.updateItemCustomItem("ITEMS", "Custom");
		for (ConfItem item : getItemsFromDatabase())
			myDbHelper.updateItemCustomItem(item.getLabel(), item.getSetting());
		for (ConfItem item: changedItems)
			myDbHelper.updateItemCustomItem(item.getLabel(), item.getSetting());
		
		fillProfileSpinner();
		mSpProfile.setSelection(10);
	
	Toast.makeText(getActivity().getBaseContext(), "Updated custom profile", Toast.LENGTH_LONG).show();
	}

	public void updatePeriodicTimeOut(String time) {
		TIME = time;
		mPeriodicText.setText("PeriodicTimeOutSec="+TIME);
		
	}
}
