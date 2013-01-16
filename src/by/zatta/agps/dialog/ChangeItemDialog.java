package by.zatta.agps.dialog;

import java.util.ArrayList;
import java.util.List;

import by.zatta.agps.R;
import by.zatta.agps.model.ConfItem;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeItemDialog extends DialogFragment implements OnClickListener{
	
	private List<ConfItem> items;
	private LinearLayout mLinLay;
	private Button mBtnCancel;
	private Button mBtnApply;
	
	public static ChangeItemDialog newInstance(List<ConfItem> list) {
        ChangeItemDialog f = new ChangeItemDialog();
        
        Bundle args = new Bundle();
        args.putParcelableArrayList("lijst", (ArrayList<? extends Parcelable>) list);
        f.setArguments(args);
        
        return f;
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        items = getArguments().getParcelableArrayList("lijst");
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(false);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	getDialog().setTitle("Make changes");
        View v = inflater.inflate(R.layout.change_item_dialog_layout, container, false);
        TextView tv = (TextView) v.findViewById(R.id.tvGroupDiscribtion);
        mLinLay = (LinearLayout) v.findViewById(R.id.llButtons);
        mBtnCancel = (Button)v.findViewById(R.id.btnCancelChange);
        mBtnApply = (Button) v.findViewById(R.id.btnDoChange);
        mBtnCancel.setOnClickListener(this);
        mBtnApply.setOnClickListener(this);
         
        tv.setText(items.get(0).getDiscription());
        
        for (ConfItem item : items){
        	createRelText(item);
        }
        
        return v;
    }
		
	private void createRelText(ConfItem item){
		View aV = getAppropriateView(item);
		
		TextView tV = new TextView(getActivity().getBaseContext());
		TableRow.LayoutParams paramsTv = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
		tV.setLayoutParams(paramsTv);
		tV.setTextColor(Color.BLACK);
		tV.setMaxLines(1);
		tV.setText(item.getLabel());
		
		LinearLayout mLinLayChild = new LinearLayout(getActivity().getBaseContext());
		LinearLayout.LayoutParams paramsLl = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mLinLayChild.setLayoutParams(paramsLl);
		mLinLayChild.addView(tV);
		mLinLayChild.addView(aV);
		
		mLinLay.addView(mLinLayChild);
	}
	
	private View getAppropriateView(ConfItem item){
		String type = item.getType();
		if (type.equals("text") || type.equals("integer")){
			EditText ed = new EditText(getActivity().getBaseContext());
			TableRow.LayoutParams paramsEt = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0);
			ed.setLayoutParams(paramsEt);		
			ed.setTextColor(Color.BLACK);
			if (type.equals("integer")) ed.setInputType(InputType.TYPE_CLASS_NUMBER);
			if (type.equals("text")) ed.setInputType(InputType.TYPE_CLASS_TEXT);
			ed.setHint(item.getSetting());
			return ed;
		}else{
			Switch sw = new Switch(getActivity().getBaseContext());
			TableRow.LayoutParams paramsSw = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0);
			sw.setLayoutParams(paramsSw);
	        sw.setTextOn("True");
	        sw.setTextOff("False");
	        if (item.getSetting().equals("TRUE")) sw.setChecked(true);
	        return sw;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnCancelChange:
			break;
		case R.id.btnDoChange:
			Toast.makeText(getActivity().getBaseContext(), "To be implemented", Toast.LENGTH_LONG).show();
			break;
		}
		dismiss();		
	}
}
