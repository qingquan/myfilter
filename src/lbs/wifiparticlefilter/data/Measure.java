package lbs.wifiparticlefilter.data;

/**
 * Represents a wifi-measure. Object contains SSID and RSSI of a measure. 
 * 
 * @author Andreas Attenberger
 * @version 1.0
 */
public class Measure {

	
	// BSSID; complies to MAC-address
	private final String bssid;
	// RSSI; strength of the signal
	private int rssi;
	
	
	/**
	 * C'tor
	 * 
	 * @param bssid Name of the hotspot
	 * @param rssi Strength of the signal
	 */
	public Measure(String bssid, int rssi){

		this.bssid = bssid;
		this.rssi = rssi;		
	}

	
	/**
	 * Gets the strength of the wifi-signal.
	 * 
	 * @return Signalstrength as Integer
	 */
	public int getRssi() {
		return rssi;
	}

	
	/**
	 * Sets the strength of the wifi-signal.
	 * 
	 * @param rssi New signalstrength
	 */
	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	
	/**
	 * Gets the name of the hotspot.
	 * 
	 * @return Name of the hotspot
	 */
	public String getBSSID() {
		return bssid;
	}

}
