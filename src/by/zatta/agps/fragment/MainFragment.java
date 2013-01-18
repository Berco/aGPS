package by.zatta.agps.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import by.zatta.agps.BaseActivity;
import by.zatta.agps.R;
import by.zatta.agps.assist.DatabaseHelper;
import by.zatta.agps.dialog.ChangeItemDialog;
import by.zatta.agps.dialog.ConfirmDialog;
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
		myDbHelper = new DatabaseHelper(getActivity().getBaseContext());
		try { 
			myDbHelper.createDataBase();
        	myDbHelper.openDataBase();
        }catch(Exception e){ }
		return v;
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
		myDbHelper.doesCustomProfileExist();  //TODO remove this line when the database has the custom column hardcoded
		fillRegionSpinner();
		fillPoolSpinner("world_name");
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
			DialogFragment newFragment = ConfirmDialog.newInstance(getItemsFromDatabase());
			newFragment.show(ft, "dialog");
			break;
		case R.id.scrollView1:
			Toast.makeText(getActivity().getBaseContext(), "scroll!!", Toast.LENGTH_SHORT).show();
			break;
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
        	for (int i = 4; i < c.getColumnCount(); i++){
        		if (!c.getString(i).equals("{null}")) labels.add(c.getString(i));
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
	
	public List<ConfItem> getItemGroup(ConfItem item){
		List<ConfItem> sectionItems = new ArrayList<ConfItem>();
		String profile = mSpProfile.getSelectedItem().toString().toUpperCase().replace(".", "").replace(" ", "");
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
		ConfItem item = new ConfItem("NTP_SERVER", null, null,null,null);
		String nowInPoolSpinner = mSpPool.getSelectedItem().toString();  
        c=myDbHelper.query("pools", null, null, null, null,null, null);
        if(c.moveToFirst()) {
        		do {        		        		
            		for (int i = 1; i < c.getColumnCount(); i++){
            			if (c.getString(i).equals(nowInPoolSpinner))
            				item.setSetting(c.getString(i-1));
                		}
            	} while (c.moveToNext());
        }
        c.close();
        return item;
	}
	
	private List<ConfItem> getFromProfileSpinner(){
		List<ConfItem> itemsList = new ArrayList<ConfItem>();
		String profile = mSpProfile.getSelectedItem().toString().toUpperCase().replace(".", "").replace(" ", "");
		
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

	public void resortList(List<ConfItem> changedItems) {
		if (BaseActivity.DEBUG){
			String controle=null;
			for (ConfItem item : changedItems){
	        	if (controle != null) controle = controle+item.getSetting();
	        	else controle = item.getSetting();
	        }
	        Log.i("MainFragment", controle + " was changed");
		}
		myDbHelper.updateItemCustomItem("ITEMS", "Custom");
		for (ConfItem item : getItemsFromDatabase())
			myDbHelper.updateItemCustomItem(item.getLabel(), item.getSetting());
		for (ConfItem item: changedItems)
			myDbHelper.updateItemCustomItem(item.getLabel(), item.getSetting());
		
		fillProfileSpinner();
		mSpProfile.setSelection(3);
	
	Toast.makeText(getActivity().getBaseContext(), "Updated custom profile", Toast.LENGTH_LONG).show();
	}
}
