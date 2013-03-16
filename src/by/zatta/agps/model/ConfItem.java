package by.zatta.agps.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfItem implements Parcelable{    
    private String mLabel;
    private String mSection;
    private String mType;
    private String mSetting;
    
    public ConfItem(String label, String section, String type, String setting) {
    	mLabel = label;
    	mSection = section;
    	mType = type;
    	mSetting = setting;
    }
    
    public void setLabel(String label){ mLabel = label; }
    public String getLabel(){ return mLabel; }
    
    public void setSection(String group){ mSection = group; }
    public String getSection(){ return mSection; }

    public void setType(String type){ mType = type; }
    public String getType(){ return mType; }
        
    public void setSetting(String setting){ mSetting = setting; }
    public String getSetting(){ return mSetting; }
    
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
		dest.writeString(mSection);
		dest.writeString(mType);
		dest.writeString(mSetting);
	}

	private void readFromParcel(Parcel in) {
		mLabel = in.readString();
		mSection = in.readString();
		mType = in.readString();
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
