package lbs.wifiparticlefilter.application;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private final static String TAG = MainActivity.class.getSimpleName();
	
	//TODO: comments
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    //launch new scanning activity
    public void startScanning(View v)
    {
    	//TODO
    	Intent i = new Intent(MainActivity.this, ScanningActivity.class);
    	startActivity(i);
    }
    
    //launch new positioning activity
    public void startPositioning(View v)
    {
    	//TODO
    	Intent i = new Intent(MainActivity.this, PositioningActivity.class);
    	startActivity(i);
    }
}