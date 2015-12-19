package com.flyingwing.base.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseHelper extends SQLiteOpenHelper {
	
	public final static String DATABASE_TEMP_FILE_PATH = "/data/data/com.perfect.test/";
	public final static String DATABASE_PATH = "/data/data/com.perfect.test/databases/";
	private final static int DATABASE_VERSION = 1;

	private SQLiteDatabase myDataBase;
	private Context myContext;
	
	public DataBaseHelper(Context context, String name) {
		super(context, name,  null, DATABASE_VERSION);
		this.myContext = context;
	}

	public void createDataBase(Context c, String name) throws IOException {
		/*SharedPreferences share = PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor newsVer = null;
		SharedPreferences.Editor notifyVer = null;*/
		
		boolean dbExist = checkDataBase(name);

		if (dbExist) {
			// do nothing - database already exist
		} else {
			this.getReadableDatabase();
			try {
				copyDataBase(name);
				/*if(name.equals(DataBaseHelper.INFO_DATABASE_NAME)){
	        		newsVer = share.edit().putString(DataBaseHelper.INFO_DATABASE_NAME, "1.0");
	        		newsVer.commit();
				}else if(name.equals(DataBaseHelper.NOTIFICATION_DATANASE_NAME)){
					notifyVer = share.edit().putString(DataBaseHelper.NOTIFICATION_DATANASE_NAME, "1.0");
					notifyVer.commit();
				}*/
			} catch (IOException e) {
				Log.i("Error createDataBase", "" + e);
//				throw new Error("Error copying database");
			}
		}
	}

	private boolean checkDataBase(String name) {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DATABASE_PATH + name;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		} catch (SQLiteException e) {
			Log.i("Error checkDataBase", "" + e);
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	private void copyDataBase(String name) throws IOException {
		InputStream myInput = myContext.getAssets().open(name);
		String outFileName = DATABASE_PATH + name;
		OutputStream myOutput = new FileOutputStream(outFileName);
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public void openDataBase(String name) throws SQLException {
		String myPath = DATABASE_PATH + name;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	}

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}
