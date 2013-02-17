package by.zatta.agps.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import by.zatta.agps.model.ConfItem;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{
    String DB_PATH =null;
    private static String DB_NAME = "configurations.db";
    private SQLiteDatabase myDataBase;
    private int mAppCode;
    private final Context myContext;
 
    public DatabaseHelper(Context context, int appCode) {
    	super(context, DB_NAME, null, appCode);
    	Log.i("Constructing", "databaseHelper");
        this.myContext = context;
        DB_PATH="/data/data/"+context.getPackageName()+"/"+"databases/";
        mAppCode = appCode;
    }
        
    /**
     * The database used here is filled with information from:
     * http://support.ntp.org/bin/view/Servers/NTPPoolServers
     * 
     * However, it is not per se that the most near by server is the
     * best one to use.
     * 
     * Derek Gordons NTP server is reliable and updated the most regularly
     * 
     * The items list is made out of all versions of the know gps tweaks on
     * XDA developers by Derek Gordon.
     **/
    
    
    public void createDataBase() throws IOException{
    	
        if(databaseExists()){
            // By calling this method here onUpgrade will be called on a
            // writable database, but only if the version number has been increased
        	// after first creatin the version is 0, second launch the version is
        	// three. This time the oncreate is called but not the onupgrade.
        	Log.i("Database existed", "getWritebleDatabase top be called");
            this.getWritableDatabase();
        }else{
            //By calling this method an empty database will be created into the default system path
            //of the application so we will be able to overwrite that database with our database.
        	Log.i("Database did not exist", "getReadableDatabase top be called");
        	this.getReadableDatabase();
        	copyDataBase();
        }
    }
    
    private boolean databaseExists(){
    	File f = new File(DB_PATH+DB_NAME);
		if (f.exists()) return true; 
		else return false;
		
    }

    private void copyDataBase() throws IOException{
    	File f = new File(DB_PATH+DB_NAME);
    	f.delete();
    	Log.d("Database", "copyDataBase");
    	try {
			InputStream myInput = myContext.getResources().getAssets().open("fix_base/"+DB_NAME);
			String outFileName = DB_PATH + DB_NAME;
			OutputStream myOutput = new FileOutputStream(outFileName);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer))>0){
				myOutput.write(buffer, 0, length);
			}
			myOutput.flush();
			myOutput.close();
			myInput.close();
		} catch (Exception e) {
			throw new Error ("troubles copying");
		} 
    }
 
    public void openDataBase() throws SQLException{
        String myPath = DB_PATH + DB_NAME;
        Log.i("opening database", "start opening");
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    	myDataBase.execSQL("PRAGMA user_version="+Integer.toString(mAppCode)+";");	
    	Log.i("opened database version",Integer.toString(myDataBase.getVersion()));
    }
 
    @Override
	public synchronized void close() {
    	Log.i("onclose", "closing");
    	    if(myDataBase != null)
    		    myDataBase.close();
    	    super.close();
	}
     
	@Override
	public void onCreate(SQLiteDatabase db) {	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		try{
			Log.i("database", "upgrading old=" + Integer.toString(oldVersion)+"->" + "new="+Integer.toString(newVersion) );
			List<ConfItem> sectionItems = new ArrayList<ConfItem>();
			Cursor c = db.query("items", new String[]{"ITEMS", "CUSTOM"}, null, null, null, null, null);
			if(c.moveToPosition(0)) {
				do {
					sectionItems.add(new ConfItem(c.getString(0), null, null, null, c.getString(1)));
				} while (c.moveToNext());
			}
			c.close(); 
			
			try { copyDataBase(); } catch (IOException e) { }
			db.close();
			openDataBase();
			
			for (int i = 0; i < sectionItems.size(); i++){
				myDataBase.execSQL("UPDATE items SET CUSTOM='"+sectionItems.get(i).getSetting()+"' WHERE ITEMS='"+sectionItems.get(i).getLabel()+"'");
			}
			
			// testing line:
			//myDataBase.execSQL("UPDATE items SET CUSTOM='"+"VODAFONE NL"+"' WHERE ITEMS='"+"CURRENT_CARRIER"+"'");
		}catch (Exception e){
			Log.i("database", "upgrade old=" + Integer.toString(oldVersion)+"->" + "new="+Integer.toString(newVersion)+" failed." );
		}
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
	
	public Cursor query(String table,String[] columns, String selection,String[] selectionArgs,String groupBy,String having,String orderBy){
		return myDataBase.query(table, columns, selection, null, null, null, null);
		
	}
	
	public List<String> getRegionsAltSecond(){
		List<String> regions = new ArrayList<String>();
		String array[] = { "CONTINENT" }; 
		String region = "";
		Cursor c = myDataBase.query("pools", array, null, null, null, null, null);
		/* These two ways of getting distinct values worked on Android 4.2 but fail on 4.0 S)
		 * return myDataBase.query(true, "pools", array, null, null, null, null, null, null);
		 * return myDataBase.rawQuery("SELECT DISTINCT CONTINENT FROM pools", null);
		 */
		if(c.moveToPosition(1)) {
        	do {
        		if (!region.equals(c.getString(0))) regions.add(c.getString(0));
        		region = c.getString(0);
        	} while (c.moveToNext());
        }
		return regions;
	}
	
	public Cursor getCountries(String continent){
		return myDataBase.rawQuery("SELECT * FROM pools WHERE CONTINENT='"+continent+"'", null);
	}
	
	public String getPoolfromSpinner(String country){
		Cursor  c = myDataBase.rawQuery("SELECT * FROM pools WHERE COUNTRY='"+country+"'", null);
		c.moveToFirst();
		return c.getString(1);
		
	}
	
	public String getColumnNameFor(String profile){
		int colNum=0;
		String colName = "GOOGLE";
        Cursor c=myDataBase.query("items", null, null, null, null,null, null);       
        if (c.moveToFirst()) {
        	for (int i = 0; i < c.getColumnCount(); i++){
        		if (c.getString(i).equals(profile)) colNum=i;
        		}
        }
		c = myDataBase.rawQuery("PRAGMA table_info(items)", null);
		int counter=0;
		if ( c.moveToFirst() ) {
		    do {
		    	if (counter == colNum) colName = c.getString(1);
		    	counter++;
		    } while (c.moveToNext());
		}
		c.close();
		return colName;
	}
	
	public void updateItemCustomItem(String label, String setting){
		myDataBase.execSQL("UPDATE items SET CUSTOM='"+setting+"' WHERE ITEMS='"+label+"'");
		
	}
	
}