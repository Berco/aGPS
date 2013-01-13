package by.zatta.agps.model;

import java.util.List;
import by.zatta.agps.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ConfItemListAdapter extends ArrayAdapter<ConfItem> {
    private final LayoutInflater mInflater;

    public ConfItemListAdapter(Context context) {
    	
        super(context, android.R.layout.simple_list_item_2);
    	
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<ConfItem> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

    /**
     * Populate new items in the list.
     */
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.row_conf_item, parent, false);
        } else {
            view = convertView;
        }
        
        ConfItem item = getItem(position);
              
        ((TextView)view.findViewById(R.id.text)).setText(item.toString());
        
        return view;
    }    
    
}