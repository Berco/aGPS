package by.zatta.agps.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
    String DB_PATH =null;
    private static String DB_NAME = "configurations.db";
    private SQLiteDatabase myDataBase; 
    private final Context myContext;
 
    public DatabaseHelper(Context context) {
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
        DB_PATH="/data/data/"+context.getPackageName()+"/"+"databases/";
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
    		File f = new File(DB_PATH);
    		if (!f.exists()){
    			this.getReadableDatabase();
    			copyDataBase();
    		}
    }

    private void copyDataBase() throws IOException{
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
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
 
    @Override
	public synchronized void close() {
    	    if(myDataBase != null)
    		    myDataBase.close();
    	    super.close();
	}
     
	@Override
	public void onCreate(SQLiteDatabase db) {	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {	}

	public Cursor query(String table,String[] columns, String selection,String[] selectionArgs,String groupBy,String having,String orderBy){
		return myDataBase.query(table, columns, selection, null, null, null, null);
		
	}
}