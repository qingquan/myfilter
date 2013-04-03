package lbs.wifiparticlefilter.dbmanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbs.wifiparticlefilter.data.Measure;
import lbs.wifiparticlefilter.data.Particle;
import lbs.wifiparticlefilter.data.Point;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper
{
	private static final String TAG = DatabaseHandler.class.getSimpleName();
	
	// Definition database
	/** Database name. */
	private static final String DATABASE_NAME = "wifiparticlefilter.db";
	/** Database version. */
	private static final int DATABASE_VERSION = 1;
	
	// Definition attributes and table WIFI
	/** id, primary key with autoincrement */
	public static final String _ID = "_id";
	/** name of the table wifi */
	public static final String TABLE_NAME_WIFI = "tbl_wifi";
	/** attribute x coordinate of the position */
	public static final String POSITION_X = "x";
	/** attribute y coordinate of the position */
	public static final String POSITION_Y = "y";
	/** attribute Received Signal Strength Indication */
	public static final String RSSI = "rssi";
	/** attribute MAC address */
	public static final String BSSID = "bssid";
	/** create table statement */
	
	private static final String TABLE_WIFI_CREATE = "CREATE TABLE "
		+ TABLE_NAME_WIFI + " (" + _ID
		+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
		+ POSITION_X + " INTEGER, "
		+ POSITION_Y + " INTEGER, "
		+ RSSI       + " INTEGER, "
		+ BSSID        + " VARCHAR(17));";
	
	/** drop statement for old table if new version available */
	private static final String TABLE_WIFI_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME_WIFI;
	
	public DatabaseHandler(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(TABLE_WIFI_CREATE);
		db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(TABLE_WIFI_DROP);
		onCreate(db);
		db.close();
	}
	
	/**
	 * inserts the given measurement into the database
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param rssi the signal strength in dBm
	 * @param bssid the access point's mac address
	 */
	public void insert(int x, int y, int rssi, String bssid)
	{
		long rowID = -1;
		
		// open Database
		SQLiteDatabase db = getWritableDatabase();
		
		try
		{
			// values to save
			ContentValues values = new ContentValues();
			values.put(POSITION_X, x);
			values.put(POSITION_Y, y);
			values.put(RSSI, rssi);
			values.put(BSSID, bssid);
			//write to db
			rowID = db.insert(TABLE_NAME_WIFI, null, values);
		}
		catch(SQLiteException e)
		{
			Log.e(TAG, "insert():", e);
		}
		finally
		{
			db.close();
			Log.d(TAG, "insert(): rowID = " + rowID);
		}
	}
	
	/**
	 * resets the table
	 */
	public void resetTable()
	{
		SQLiteDatabase db = getWritableDatabase();
		//drop existing table
		db.execSQL(TABLE_WIFI_DROP);
		//create new table
		db.execSQL(TABLE_WIFI_CREATE);
		db.close();
	}
	
	/**
	 * returns a list of particles for all measurements in db
	 * @return the list
	 */
	public List<Particle> getData()
	{
		//get cursor
		SQLiteDatabase db = getWritableDatabase();
		String[] columns = new String[]{POSITION_X, POSITION_Y, RSSI, BSSID};
		Cursor cur = db.query(TABLE_NAME_WIFI, columns, null, null, null, null, POSITION_X + " ASC, " +  POSITION_Y + " ASC");
		
		List<Particle> list = new ArrayList<Particle>();
		HashMap<Point, ArrayList<Measure>> measurements = new HashMap<Point, ArrayList<Measure>>();
		
		//get indices of coloumns
		int xIdx = cur.getColumnIndex(POSITION_X);
		int yIdx = cur.getColumnIndex(POSITION_Y);
		int rssiIdx = cur.getColumnIndex(RSSI);
		int bssidIdx = cur.getColumnIndex(BSSID);
		//run through all rows
		if(cur.moveToFirst())
		{
			while(!cur.isAfterLast())
			{
				int x = cur.getInt(xIdx);
				int y = cur.getInt(yIdx);
				int rssi = cur.getInt(rssiIdx);
				String bssid = cur.getString(bssidIdx);
				
				Point point = new Point(x, y);
				
				if(!measurements.containsKey(point))
					measurements.put(point, new ArrayList<Measure>());
				measurements.get(point).add(new Measure(bssid, rssi));
				
				cur.moveToNext();
			}
		}
		else
			Log.d(TAG, "no data in database");
		
		//build list of particles
		for(Map.Entry<Point, ArrayList<Measure>> entry : measurements.entrySet())
		    list.add(new Particle(entry.getKey(), entry.getValue()));
		//close cursor and db
		cur.close();
		db.close();
		
		return list;
	}
}