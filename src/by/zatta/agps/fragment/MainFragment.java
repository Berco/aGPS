package by.zatta.agps.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
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
import by.zatta.agps.BaseActivity;
import by.zatta.agps.R;
import by.zatta.agps.assist.DatabaseHelper;
import by.zatta.agps.assist.ShellProvider;
import by.zatta.agps.dialog.ChangeItemDialog;
import by.zatta.agps.dialog.ChangelogDialog;
import by.zatta.agps.dialog.ConfirmDialog;
import by.zatta.agps.dialog.FirstUseDialog;
import by.zatta.agps.dialog.SliderDialog;
import by.zatta.agps.model.ConfItem;
import by.zatta.agps.model.ConfItemListAdapter;

public class MainFragment extends ListFragment implements OnClickListener, OnItemSelectedListener {
	
	private Button INSTALL;
	private Cursor c=null;
	private Spinner mSpRegion;
	private Spinner mSpPool;
	private Spinner mSpProfile;
	private Spinner mSpSupl;
	DatabaseHelper myDbHelper;
	private ListView mList;
	private LinearLayout mLinLayConfigXml;
	private LinearLayout mLinLayPreCheck;
	private Boolean showPreCheck=true;
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
		mSpSupl = (Spinner)v.findViewById(R.id.spSupl);
		mSpSupl.setOnItemSelectedListener(this);
		mLinLayConfigXml = (LinearLayout) v.findViewById(R.id.llConfigXml);
		mLinLayConfigXml.setOnClickListener(this);
		mPeriodicText = (TextView) v.findViewById(R.id.tvPeriodicText);
		mLinLayPreCheck = (LinearLayout) v.findViewById(R.id.llLoadingPreCheck);
		
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
			
			SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
			Boolean firstUse = getPrefs.getBoolean("showFirstUse", true);
			if (BaseActivity.isUpdate) 
				showChangelog();
			if (firstUse) 
				showFirstUse();
		}
		fillRegionSpinner();
		fillPoolSpinner("Global");
		fillProfileSpinner();
		fillSuplSpinner();
		getItemsFromDatabase();
		if(!showPreCheck) showContent();
	}

	@Override
	public void onClick(View v) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		switch(v.getId()){
		case R.id.btnInstall:
			ft.addToBackStack(null);
			DialogFragment newFragment = ConfirmDialog.newInstance(getItemsFromDatabase(), TIME , true);
			newFragment.show(ft, "dialog");
			break;
		case R.id.llConfigXml:
			ft.addToBackStack(null);
			DialogFragment newSlider = SliderDialog.newInstance(TIME);
			newSlider.show(ft, "dialog");
			break;
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ConfItem item;
		 if (id > 4) item = (ConfItem) getListAdapter().getItem(position);
		 else if (id <= 4 && id > 0) item = (ConfItem) getListAdapter().getItem(2);
		 else item = (ConfItem) getListAdapter().getItem(0);
			
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.addToBackStack(null);
				DialogFragment newFragment = ChangeItemDialog.newInstance(getItemGroup(item));
				newFragment.show(ft, "dialog");
	}
	
	public void showFirstUse(){	
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		DialogFragment newFragment = FirstUseDialog.newInstance();
		newFragment.show(ft, "dialog");
	}
	
	public void showChangelog(){	
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		DialogFragment newFragment = ChangelogDialog.newInstance();
		newFragment.show(ft, "dialog");
	}
	
	// Four methods to fill the spinners
	public void fillPoolSpinner(String continent){  
        List<String> labels = myDbHelper.getPools(continent);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPool.setAdapter(dataAdapter);
	}
	public void fillRegionSpinner(){
        List<String> labels = myDbHelper.getRegions();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpRegion.setAdapter(dataAdapter);
	}
	public void fillProfileSpinner(){
		List<String> labels = myDbHelper.getProfiles();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpProfile.setAdapter(dataAdapter);
	}
	public void fillSuplSpinner(){
        List<String> labels = myDbHelper.getSupls();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(),android.R.layout.simple_spinner_item, labels);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpSupl.setAdapter(dataAdapter);
	}
	
	// method to pass get a list for the ChangeItemDialogFragment
	public List<ConfItem> getItemGroup(ConfItem item){
		List<ConfItem> sectionItems = new ArrayList<ConfItem>();
		if (item.getSection().contains("supl") || item.getSection().contains("server") || item.getSection().contains("agps")) {
			sectionItems.add(item);
			return sectionItems;
		}
			
		String profile = myDbHelper.getColumnNameFor(mSpProfile.getSelectedItem().toString(), "items");
		String array[] = { "ITEMS","SECTION","TYPE",profile }; 
		c=myDbHelper.query("items", array, null, null, null,null, null);
        if(c.moveToPosition(1)) {
        	do {
        		if (!c.getString(0).equals("{null}") && 
        			!c.getString(3).equals("{null}") &&
        			c.getString(1).equals(item.getSection()))
        			sectionItems.add(new ConfItem(c.getString(0), c.getString(1), c.getString(2), c.getString(3)));
        	} while (c.moveToNext());
        }
        c.close();
		return sectionItems;
	}
	
	// Getting it all together
	public List<ConfItem> getItemsFromDatabase(){
        List<ConfItem>confItems = getFromProfileSpinner();
        for (ConfItem supl : getFromSuplSpinner())
        	confItems.add(10, supl);
        for (ConfItem agps : getAgpsFromDatabase())
        	confItems.add(0, agps);
        confItems.add(0, getFromPoolSpinner());
       	mConfAdapter.setData(confItems);
        return confItems;
	}
	
	// called from getItemsFromDatabase()
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
        			ntpItems.add(new ConfItem(c.getString(0),"agps","text", c.getString(1)));
        	} while (c.moveToNext());
        }
        c.close();
		return ntpItems;
	}
	
	// called from getItemsFromDatabase()
	private ConfItem getFromPoolSpinner(){
        ConfItem item = new ConfItem("NTP_SERVER", "server", "text" ,myDbHelper.getPoolfromSpinner(mSpPool.getSelectedItem().toString()));
        return item;
	}
	
	// called from getItemsFromDatabase()
	private List<ConfItem> getFromProfileSpinner(){
		List<ConfItem> itemsList = new ArrayList<ConfItem>();
		String profile = myDbHelper.getColumnNameFor(mSpProfile.getSelectedItem().toString(), "items");
		String array[] = { "ITEMS","SECTION","TYPE",profile }; 
		c=myDbHelper.query("items", array, null, null, null,null, null);
		if(c.moveToPosition(1)) {
			do {
				if (!c.getString(0).equals("{null}") && !c.getString(3).equals("{null}"))
					itemsList.add(new ConfItem(c.getString(0), c.getString(1), c.getString(2), c.getString(3)));
			} while (c.moveToNext());
		}
		c.close();
		return itemsList;
	}
	
	// called from getItemsFromDatabase()
	private List<ConfItem> getFromSuplSpinner(){
		List<ConfItem> itemsList = new ArrayList<ConfItem>();
		String profile = myDbHelper.getColumnNameFor(mSpSupl.getSelectedItem().toString(), "supl");
		String array[] = { "ITEMS","SECTION","TYPE",profile }; 
		c=myDbHelper.query("supl", array, null, null, null,null, null);
		if(c.moveToPosition(1)) {
			do {
				if (!c.getString(0).equals("{null}") && !c.getString(3).equals("{null}"))
					itemsList.add(new ConfItem(c.getString(0), c.getString(1), c.getString(2), c.getString(3)));
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
		case R.id.spSupl:
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
		mSpProfile.setSelection(6);
	
	Toast.makeText(getActivity().getBaseContext(), getString(R.string.toastUpdated), Toast.LENGTH_LONG).show();
	}

	public void updatePeriodicTimeOut(String time) {
		TIME = time;
		mPeriodicText.setText("PeriodicTimeOutSec="+TIME);
	}

	public void showContent() {
		if (ShellProvider.INSTANCE.isConfigPresent()){
			SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
	        TIME = getPrefs.getString("TIME", "5");
			mPeriodicText.setText("PeriodicTimeOutSec="+TIME);
			mLinLayConfigXml.setVisibility(View.VISIBLE);
		}
		showPreCheck=false;
		mLinLayPreCheck.setVisibility(View.GONE);
	}

	public void onResetCustomProfile() {
		myDbHelper.resetCustomProfile();
		mSpProfile.setSelection(0);
	}
}
