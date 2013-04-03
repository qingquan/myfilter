package lbs.wifiparticlefilter.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lbs.wifiparticlefilter.data.Measure;
import lbs.wifiparticlefilter.data.Particle;
import lbs.wifiparticlefilter.data.Point;
import lbs.wifiparticlefilter.dbmanagement.DatabaseHandler;
import lbs.wifiparticlefilter.filter.Filter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class PositioningActivity extends Activity
{
	/** tag for debugging */
	private final static String TAG = PositioningActivity.class.getSimpleName();
	/** sleep duration, 1/scanfreq */
	public final static int SLEEP_MS = 500;
	/** number of particles */
	public final static int PARTICLES = 30;
	/** wifi manager for managing wifi connection */
	private WifiManager wifiManager;
	/** is wifi turned on */
	private boolean wifiEnabled = false;
	/** handler of database connection */
	private DatabaseHandler dbHandler;
	/** is scanning active */
	private boolean scanning = false;
	/** the draw view for drawing */
	private DrawView drawView;
	/** filter instance */
	private Filter filter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		drawView = new DrawView(this);
		setContentView(drawView);

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		checkWifi();

		dbHandler = new DatabaseHandler(this);

		//init filter
		filter = new Filter(PARTICLES, dbHandler.getData());
		filter.initFilter();
		
		drawView.setBorder(filter.getBorder());
		
		// TODO draw initialised particles
		drawParticles(filter.getParticles());
	}

	@Override
	public void onPause()
	{
		scanning = false;
		super.onPause();
	}

	@Override
	public void onStop()
	{
		scanning = false;
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.positioning, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch(item.getItemId())
		{
			case R.id.positioning_menu_start:
				if(!scanning)
					positioning();
				return true;
			case R.id.positioning_menu_stop:
				scanning = false;
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * starts one thread for signal acquisition, filtering and particle drawing
	 */
	private void positioning()
	{
		checkWifi();

		if(wifiEnabled)
		{
			scanning = true;

			Thread worker = new Thread(new Runnable() {
				public void run()
				{
					while(scanning)
					{
						Log.d(TAG, "working thread");
						HashMap<String, Integer> signals = getSignals();
						// make sure wifi already sees signals
						if(signals != null)
						{
							//filter signals and draw new particles
							drawParticles(filterSignals(signals));
						}
						try
						{
							Thread.sleep(SLEEP_MS);
						}
						catch(InterruptedException e)
						{
							Log.e(TAG, "error in scanning thread", e);
						}
					}
				}
			});
			worker.start();
		}
	}

	/**
	 * gets all currently visible signals with their strengths
	 * 
	 * @return list of all signals and thier strengths
	 */
	private HashMap<String, Integer> getSignals()
	{
		HashMap<String, Integer> res = null;

		if(wifiEnabled)
		{
			res = new HashMap<String, Integer>();
			// if scan successful
			if(wifiManager.startScan())
			{
				List<ScanResult> scanResult = wifiManager.getScanResults();
				// filter out repeating bssid's
				if(scanResult != null)
					for(ScanResult scan : scanResult)
						if(!res.containsKey(scan.BSSID))
							res.put(scan.BSSID, scan.level);
			}
		}

		return res;
	}

	/**
	 * moves the given signals to the filter and returns the new particles
	 * @param signals the signals
	 * @return the new particles
	 */
	private List<Particle> filterSignals(HashMap<String, Integer> signals)
	{
		//convert to list of measure
		List<Measure> measure = new ArrayList<Measure>();
		for(Map.Entry<String, Integer> entry : signals.entrySet())
			measure.add(new Measure(entry.getKey(), entry.getValue()));
		
		// move signals to filter
		filter.setCurrentMeasure(measure);
		//filter
		filter.estimate();
		filter.propagate();
		//get new particles
		return filter.getParticles();
	}

	/**
	 * draws the given particles on the DrawView
	 * 
	 * @param p the list of particles
	 */
	private void drawParticles(List<Particle> p)
	{
		// move particles to draw view
		drawView.setParticles(p);

		// draw, runs on ui thread
		runOnUiThread(new Runnable() {
			public void run()
			{
				drawView.drawParticles();
			}
		});
	}

	/**
	 * checks whether wifi is enabled, if not, start button is disabled and
	 * dialogue for switching on is prompted.
	 */
	private void checkWifi()
	{
		wifiEnabled = wifiManager.isWifiEnabled();

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
						scanning = false;
					}
				});

		AlertDialog dialog = builder.create();

		dialog.show();
	}
}
