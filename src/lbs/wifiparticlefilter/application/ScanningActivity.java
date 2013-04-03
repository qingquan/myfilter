package lbs.wifiparticlefilter.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbs.wifiparticlefilter.data.Measure;
import lbs.wifiparticlefilter.data.Particle;
import lbs.wifiparticlefilter.dbmanagement.DatabaseHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ScanningActivity extends Activity
{
	/** tag for debugging */
	private final static String TAG = ScanningActivity.class.getSimpleName();
	/** number of samples */
	private final static int SAMPLES = 3;
	/** wifi manager for managing wifi connection */
	private WifiManager wifiManager;
	/** is wifi turned on */
	private boolean wifiEnabled = false;
	//button for dis-/enabling
	private Button startButton;
	/** handler of database connection */
	private DatabaseHandler dbHandler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scanning);

		startButton = (Button) findViewById(R.id.buttonStart);

		dbHandler = new DatabaseHandler(this);

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		checkWifi();
	}

	/**
	 * checks whether wifi is enabled, if not, start button is disabled
	 * and dialogue for switching on is prompted. 
	 */
	private void checkWifi()
	{
		wifiEnabled = wifiManager.isWifiEnabled();
		startButton.setEnabled(wifiEnabled);

		if(!wifiEnabled)
			wifiDialogue();
	}

	/**
	 * dialogue for switching wifi on
	 */
	private void wifiDialogue()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.wifiDialogue);
		builder.setTitle(R.string.wifiDialogueTitle);
		builder.setPositiveButton(R.string.buttonYes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id)
					{

						wifiEnabled = wifiManager.setWifiEnabled(true);
						startButton.setEnabled(wifiEnabled);

						if(!wifiEnabled)
						{
							// toast if error while enabling wifi
							Context context = getApplicationContext();
							CharSequence text = getResources().getString(
									R.string.wifiDialogueTitle);
							int duration = Toast.LENGTH_LONG;
							Toast toast = Toast.makeText(context, text,
									duration);
							toast.show();
						}
					}
				});
		builder.setNegativeButton(R.string.buttonNo,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id)
					{
						wifiEnabled = false;
						startButton.setEnabled(wifiEnabled);
					}
				});

		AlertDialog dialog = builder.create();

		dialog.show();
	}

	/**
	 * validates the input arguments and kicks off the scanning
	 * progress
	 * @param v the start button
	 */
	public void validateInput(View v)
	{
		// get text fields
		EditText editX = (EditText) findViewById(R.id.editTextX);
		EditText editY = (EditText) findViewById(R.id.editTextY);
		// check if integers
		try
		{
			Integer x = Integer.parseInt(editX.getText().toString());
			Integer y = Integer.parseInt(editY.getText().toString());
			// values seem to be correct, now start scanning
			startScanning(x, y);
		}
		catch(NumberFormatException e)
		{
			Log.e(TAG, "double value error");
			// toast if wrong values
			Context context = getApplicationContext();
			CharSequence text = getResources().getString(
					R.string.errorNumberToast);
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	/**
	 * scans the available wifi signals in a seperate thread and writes all
	 * to database
	 * @param x the current x coordinate
	 * @param y the current y coordinate
	 */
	private void startScanning(final Integer x, final Integer y)
	{
		// check again...
		checkWifi();
		// get bar
		final ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar1);

		if(wifiEnabled)
		{
			// toast for scanning
			Context context = getApplicationContext();
			CharSequence text = getResources()
					.getString(R.string.waitScanToast);
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();

			Thread workerThread = new Thread(new Runnable() {
				public void run()
				{
					List<HashMap<String, Integer>> collectedMeasurements = new ArrayList<HashMap<String, Integer>>();
					
					runOnUiThread(new Runnable() {
						public void run()
						{
							// make progress bar visible
							bar.setVisibility(View.VISIBLE);
						}
					});
					
					//perform scans
					for(int i = 0; i < SAMPLES; i++)
					{
						collectedMeasurements.add(scanAccessPoints());

						try
						{
							Thread.sleep(500);
						}
						catch(InterruptedException e)
						{
							Log.e(TAG, "error while scanning", e);
						}
					}
					//write to db
					putDatabase(x, y, calcMean(collectedMeasurements));
					
					runOnUiThread(new Runnable() {
						public void run()
						{
							// make progress bar invisible
							bar.setVisibility(View.INVISIBLE);
						}
					});
				}
			});
			workerThread.start();
		}
	}

	/**
	 * scans the available signals
	 * @return HashMap of all unique access points with their signal strengths
	 */
	private HashMap<String, Integer> scanAccessPoints()
	{
		Log.d(TAG, "scannAccessPoints");

		HashMap<String, Integer> res = new HashMap<String, Integer>();

		// if scan successful
		if(wifiManager.startScan())
		{
			List<ScanResult> scanResult = wifiManager.getScanResults();
			//filter out repeating bssid's
			for(ScanResult scan : scanResult)
				if(!res.containsKey(scan.BSSID))
					res.put(scan.BSSID, scan.level);
		}
		return res;
	}
	
	/**
	 * calculates the mean value of each access point's signals
	 * @param meas the measured signal strengths
	 * @return the mean values of each access point
	 */
	private HashMap<String, Integer> calcMean(List<HashMap<String, Integer>> meas)
	{
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		HashMap<String, ArrayList<Integer>> temp = new HashMap<String, ArrayList<Integer>>();
		
		for(HashMap<String, Integer> res : meas)
		{
			for(Map.Entry<String, Integer> entry : res.entrySet())
			{
				String key = entry.getKey();
				
				if(!temp.containsKey(key))
					temp.put(key, new ArrayList<Integer>());
				temp.get(key).add(entry.getValue());
			}
		}
		
		// calc mean
		for(Map.Entry<String, ArrayList<Integer>> entry : temp.entrySet())
		{
			int signal = 0;
			int num = entry.getValue().size();
			
			for(Integer sig : entry.getValue())
				signal += sig;
			signal /= num;
			
			result.put(entry.getKey(), signal);
		}
		return result;
	}
	
	/**
	 * writes data to database
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param values the values for each seen access point
	 */
	private void putDatabase(int x, int y, HashMap<String, Integer> values)
	{
		for(Map.Entry<String, Integer> entry : values.entrySet())
			dbHandler.insert(x, y, entry.getValue(), entry.getKey());
	}

	//only for debugging remove later
	public void resetTable(View v)
	{
		Log.d(TAG, "resetting table");
		dbHandler.resetTable();
	}

	//only for debugging remove later
	public void readTable(View v)
	{
		List<Particle> list = dbHandler.getData();
		
		Log.d(TAG, "numer of particle: " + list.size());
		
		for(Particle p : list)
			for(Measure m : p.getMeasure())
				Log.d(TAG, "x: " + p.getX() + " y: " + p.getY() + " bssid: " + m.getBSSID() + " rssi: " + m.getRssi());
	}
}