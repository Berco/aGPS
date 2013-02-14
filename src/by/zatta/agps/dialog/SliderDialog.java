package by.zatta.agps.dialog;

import by.zatta.agps.R;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SliderDialog extends DialogFragment implements OnClickListener, OnSeekBarChangeListener{
	
	private int periodic_timeout;
	private String TIME;
	OnPeriodicChangeListener periodicChangeListener;
	
	public static SliderDialog newInstance(String time) {
        SliderDialog f = new SliderDialog();
        Bundle args = new Bundle();
        args.putString("tijd", time);
        f.setArguments(args);
        return f;
    }
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            periodicChangeListener = (OnPeriodicChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnPeriodicChangeListener");
        }
    }
	public interface OnPeriodicChangeListener{
		public void onPeriodicListener(String time);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TIME = getArguments().getString("tijd");
        periodic_timeout = Integer.valueOf(TIME);
        setStyle(DialogFragment.STYLE_NORMAL, 0);
        setRetainInstance(false);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sliderdialog_layout, container, false);
        
        SeekBar dpiSB = (SeekBar) view.findViewById(R.id.sbDPIslider);
        dpiSB.setOnSeekBarChangeListener(this);
		TextView explain = (TextView) view.findViewById(R.id.tvConfigExplanation);
		Button abutton = (Button) view.findViewById(R.id.btnCancelChangeConfig);
		Button rbutton = (Button) view.findViewById(R.id.btnDoChangeConfig);
		abutton.setOnClickListener(this);
		rbutton.setOnClickListener(this);
		
		getDialog().setTitle("PeriodicTimeOutsec="+TIME);
		dpiSB.setProgress(prog_value(periodic_timeout));
		
		explain.setText("This value in the file /system/etc/gps/gpsconfig.xml " +
				"should improve the accuracy while moving. It can be that this " +
				"is causing some battery drain.");
        
        return view;
    }
	
	private int prog_value(int dpi2) {
		int val = 7-((40-dpi2)/5);
		return val;
	}

	private int dpi_value(int a) {
		int val = 40-a*5;
		return val;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnCancelChangeConfig:
			break;
		case R.id.btnDoChangeConfig:
			periodicChangeListener.onPeriodicListener(TIME);
		}
		dismiss();		
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
	TIME = String.valueOf(periodic_timeout = dpi_value(7-progress));
	getDialog().setTitle("PeriodicTimeOutsec="+TIME);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }
	

}
