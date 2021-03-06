package fr.ece.ostis.network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-30
 */
public class WifiNetworkManager extends NetworkManager{
	
	
	/** Log tag. */
	protected static final String mTag = "WifiNetworkManager";
	
	
	/** TODO */
	protected static final int mWifiTimeout = 5000;
	
	
	/** TODO */
	protected static final int mWifiTimeoutSteps = 500;
	
	
	/** TODO */
	protected Context mContext = null;
	
	
	/** TODO */
	protected ConnectivityManager mConnectivityManager = null;
	
	
	/** TODO */
	protected WifiManager mWifiManager = null;

	
	/** TODO */
	protected WifiLock mWifiLock = null;
	
	
	/** TODO */
	protected ArrayList<OnWifiScanResultsUpdatedListener> mWifiScanResultsCallbacks = new ArrayList<OnWifiScanResultsUpdatedListener>();
	
	
	/*
	 * Constants
	 */
	public static final String KEY_NETWORK_BSSID = "BSSID";
	public static final String KEY_NETWORK_SSID = "SSID";
	public static final String KEY_NETWORK_LEVEL = "LEVEL";
	
	
	/**
	 * TODO
	 * @param context
	 */
	public WifiNetworkManager(Context context){
		
		// Retrieve context
		mContext = context;
		
		// Get connectivity manager
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// Get wifi manager
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		
		// Create and register wifi scan results intent receiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
			public void onReceive(Context c, Intent i){
				WifiNetworkManager.this.onWifiScanResultsUpdated();
			}
		};
		mContext.registerReceiver(broadcastReceiver, intentFilter);
		
	}
	
	
	/**
	 * TODO
	 */
	public void acquireWifiLock(){
		if(mWifiLock == null) mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "Wifi Wakelock");
		if(mWifiLock.isHeld() != true) mWifiLock.acquire();
	}
	
	
	/**
	 * TODO
	 */
	public void releaseWifiLock(){
		if(mWifiLock != null && mWifiLock.isHeld()) mWifiLock.release();
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public boolean isWifiEnabled(){
		return mWifiManager.isWifiEnabled();
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public boolean isWifiConnected(){
		NetworkInfo wifiInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifiInfo.isConnected();
	}
	
	
	/**
	 * TODO
	 */
	public void enableWifiAsynchronous(){
		setWifiEnabled(true);
	}
	
	
	/**
	 * TODO
	 */
	public void disableWifiAsynchronous(){
		setWifiEnabled(false);
	}
	
	
	/**
	 * TODO
	 * @param enabled
	 */
	protected void setWifiEnabled(boolean enabled){
		mWifiManager.setWifiEnabled(enabled);
	}
	
	
	/**
	 * Waits for mWifiTimeout seconds until wifi is enabled.
	 * @throws TimeoutException if wifi is not enabled at the end of time.
	 */
	public void waitForWifiEnabled() throws TimeoutException{
		
		for(int i = 0; i < mWifiTimeout; i += mWifiTimeoutSteps){
			if(getWifiState() == WifiManager.WIFI_STATE_ENABLED) return;
			try{
				Thread.sleep(mWifiTimeoutSteps);
			}catch(Exception e){
				Log.w(mTag, e.getMessage());
			}
		}
		
		throw new TimeoutException("Wifi enable timed out.");
		
	}
	
	
	/**
	 * Waits for mWifiTimeout seconds until wifi is disabled.
	 * @throws TimeoutException if wifi is not disabled at the end of time.
	 */
	public void waitForWifiDisabled() throws TimeoutException{
		
		for(int i = 0; i < mWifiTimeout; i += mWifiTimeoutSteps){
			if(getWifiState() == WifiManager.WIFI_STATE_DISABLED) return;
			try{
				Thread.sleep(mWifiTimeoutSteps);
			}catch(Exception e){
				Log.w(mTag, e.getMessage());
			}
		}
		
		throw new TimeoutException("Wifi disable timed out.");
		
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public int getWifiState(){
		try{
			return mWifiManager.getWifiState();
		}catch(Exception e){
			Log.w(mTag, e);
			return WifiManager.WIFI_STATE_UNKNOWN;
		}
	}
	
	
	/**
	 * TODO
	 * @throws Exception
	 */
	public void startWifiScan(OnWifiScanResultsUpdatedListener callback) throws Exception{
		
		// Log
		Log.i(mTag, "Starting wifi scan.");
		
		// Ensure that wifi has been enabled.
		if(isWifiEnabled() != true) throw new WifiNotEnabledException("Wifi has not been enabled.");
		
		// Set callback
		mWifiScanResultsCallbacks.add(callback);		
		
		// Start scan
		if(mWifiManager.startScan() != true) throw new Exception("Wifi scan failed.");
		
	}
	
	
	/**
	 * TODO
	 */
	protected void onWifiScanResultsUpdated(){
		Log.i(mTag, "Wifi scan results available.");
		
		List<ScanResult> wifiResults = mWifiManager.getScanResults();
		
		/*for (ScanResult network: wifiResults){
			Log.d(mTag, "Wifi network : [" + network.BSSID + "] " + network.SSID);
		}*/
		
		for (OnWifiScanResultsUpdatedListener callback: mWifiScanResultsCallbacks)
			if(callback != null) callback.onWifiScanResultsUpdated(wifiResults);
		
		mWifiScanResultsCallbacks.clear();
	}
	
	
	/**
	 * TODO
	 * @param ssid
	 * @throws Exception 
	 */
	public void connectToOpenNetwork(String ssid) throws Exception{
		
		// Log
		Log.i(mTag, "Connecting to wifi network " + ssid + ".");
		
		// Create configuration
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + ssid + "\"";
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		
		// Add configuration to wifi manager
		int networkId = mWifiManager.addNetwork(config);
		if(networkId < 0) throw new Exception("Failed adding the new network.");
		
		// Enable it
		mWifiManager.disconnect();
		mWifiManager.enableNetwork(networkId, true);
		mWifiManager.reconnect();
		
	}
	
	/**
	 * TODO
	 * @param dns
	 */
	public void setWifiDns(String dns){
		Log.i(mTag, "Setting wifi dns to " + dns);
		android.provider.Settings.System.putString(mContext.getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS1, dns);
		android.provider.Settings.System.putString(mContext.getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS2, dns);
	}
	
	
	/**
	 * TODO
	 * @return
	 * @throws Exception 
	 * @see http://stackoverflow.com/questions/8811315/how-to-get-current-wifi-connection-info-in-android
	 */
	public String getCurrentSsid() throws Exception{
		
		// Ensure wifi is enabled and connected
		if(!isWifiEnabled()) throw new WifiNotEnabledException();
		if(!isWifiConnected()) throw new WifiNotConnectedException();
		
		// Retrieve info
		WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
		
		// Return if not null or throw exception
		if(connectionInfo != null){
			String retrievedSsid = "";
			
			// Strip quotes
			if(connectionInfo.getSSID().startsWith("\"") && connectionInfo.getSSID().endsWith("\"")){
				retrievedSsid = connectionInfo.getSSID().substring(1, connectionInfo.getSSID().length() - 1);
			}else{
				retrievedSsid = connectionInfo.getSSID();
			}
			
			if(retrievedSsid.length() > 0) return retrievedSsid;
		}
		
		throw new Exception("Unabled to retrieve information on current wifi network.");
		
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-30
	 */
	public class WifiNotEnabledException extends Exception{
		private static final long serialVersionUID = -2693396693905205724L;
		public WifiNotEnabledException(){ super(); }
		public WifiNotEnabledException(String detailMessage){ super(detailMessage); }
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-31
	 */
	public class WifiNotConnectedException extends Exception{
		private static final long serialVersionUID = 3732781232713416439L;
		public WifiNotConnectedException(){ super(); }
		public WifiNotConnectedException(String detailMessage){ super(detailMessage); }
	}
	
}
