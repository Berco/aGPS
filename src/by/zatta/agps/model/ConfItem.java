package by.zatta.agps.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfItem implements Parcelable{    
    private String mLabel;
    private String mSetting;
    
    public ConfItem(String label, String setting) {
    	mLabel = label;
    	mSetting = setting;
    }
    
    public void setLabel(String label){
    	mLabel = label;
    }
    
    public String getLabel(){
    	return mLabel;
    }
    
    public void setSetting(String setting){
    	mSetting = setting;
    }
    
    public String getSetting(){
    	return mSetting;
    }

    @Override public String toString() {
        return mLabel + "=" + mSetting;
    }
    
    /*
     * the rest is to make it parcelable
     */

    public ConfItem(Parcel in) {
		readFromParcel(in);
	}
  
	@Override
	public int describeContents() {
		return 0;
	}
 
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mLabel);
		dest.writeString(mSetting);
	}

	private void readFromParcel(Parcel in) {
		mLabel = in.readString();
 		mSetting = in.readString();
	}
     
    @SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR =
    	new Parcelable.Creator() {
            @Override
			public ConfItem createFromParcel(Parcel in) {
                return new ConfItem(in);
            }
 
            @Override
			public ConfItem[] newArray(int size) {
                return new ConfItem[size];
            }
        };	

}
