package fr.ece.ostis.network;

import java.lang.reflect.Method;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;


/**
 * TODO
 * @see http://stackoverflow.com/questions/7048922/android-2-3-wifi-hotspot-api
 * @author Nicolas Schurando
 * @version 2014-01-29
 */
public class WifiAPNetworkManager extends NetworkManager{
	
	
	/** Log tag. */
	protected static final String mTag = "WifiAPNetworkManager";

	
	/** TODO */
    protected static final String mAccessPointName = "OstisAP";
    
    
    /** TODO */
    protected static final int mAccessPointChannel = 9;
    
    
    /** TODO */
	protected static final int mWifiTimeout = 5000;
	
	
	/** TODO */
	protected static final int mWifiTimeoutSteps = 500;
    
	
	/** TODO */
	protected Context mContext = null;

	
	/** TODO */
	protected WifiManager mWifiManager = null;
	
	
	/** TODO */
	protected WifiNetworkManager mWifiNetworkManager = null;
	

	/** TODO */
	protected int mWifiStatePrevious = WifiManager.WIFI_STATE_UNKNOWN;
	
	
	/*
	 * Constants
	 */
	public static final int WIFI_AP_STATE_UNKNOWN = -1;
	public static final int WIFI_AP_STATE_DISABLING = 0;
    public static final int WIFI_AP_STATE_DISABLED = 1;
    public static final int WIFI_AP_STATE_ENABLING = 2;
    public static final int WIFI_AP_STATE_ENABLED = 3;
    public static final int WIFI_AP_STATE_FAILED = 4;
	
	
	/**
	 * TODO
	 * @param context
	 */
	public WifiAPNetworkManager(Context context, WifiNetworkManager wifiNetworkManager){
		
		// Retrieve context
		mContext = context;
		
		// Retrieve ostis wifi manager
		mWifiNetworkManager = wifiNetworkManager;
		
		// Get wifi manager
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		
	}
	
	
	/**
	 * Enables the wifi access point.
	 * @throws TimeoutException 
	 */
	public void enableWifiAp() throws TimeoutException{
		setWifiApEnabled(true);
	}
	
	
	/**
	 * Disables the wifi access point.
	 * @throws TimeoutException 
	 */
	public void disableWifiAp() throws TimeoutException{
		setWifiApEnabled(false);
	}
	
	
    /**
     * Enables or disables the wifi access point.
     * @param true or false
     * @return WifiAP state
     * @throws TimeoutException 
     */
    protected void setWifiApEnabled(boolean enabled) throws TimeoutException{
    	
    	// Log
        Log.d(mTag, "setWifiApEnabled " + String.valueOf(enabled));

        // Create a wifi configuration for the access point
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = mAccessPointName;
        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        
        // Try to specify channel for the wifi configuration
        try{
        	WifiConfiguration.class.getField("channel").setInt(wifiConfiguration, mAccessPointChannel);
        }catch(Exception e){
        	Log.w(mTag, e);
        }

        // Remember wireless current state
        if(enabled && mWifiStatePrevious == WifiManager.WIFI_STATE_UNKNOWN){
        	mWifiStatePrevious = mWifiNetworkManager.getWifiState();
        }

        // Disable wireless
        if(enabled && mWifiManager.getConnectionInfo() != null){
            Log.d(mTag, "Wifi disabling");
            mWifiNetworkManager.disableWifi();
            mWifiNetworkManager.waitForWifiDisabled();
            Log.d(mTag, "Wifi disabled");
        }

        // Enable/disable wifi access point
        try{
            Log.d(mTag, "Wifi ap " + (enabled?"enabling":"disabling"));
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method.invoke(mWifiManager, wifiConfiguration, enabled);
        }catch(Exception e){
            Log.e(mTag, e.getMessage());
        }

        // Hold thread up while processing occurs
        if(enabled){
        	
        	int i;
        	for(i = 0; i < mWifiTimeout; i += mWifiTimeoutSteps){
        		int state = getWifiAPState();
        		if(state == WIFI_AP_STATE_ENABLED) break;
        		
                try{
                    Thread.sleep(mWifiTimeoutSteps);
                }catch(Exception e){
                	Log.w(mTag, e);
                }
        	}
        	if(i >= mWifiTimeout) throw new TimeoutException("Enabling wifi ap timed out.");
            Log.d(mTag, "Wifi ap enabled");
            
        }else{
        	
        	int i;
        	for(i = 0; i < mWifiTimeout; i += mWifiTimeoutSteps){
        		int state = getWifiAPState();
        		if(state == WIFI_AP_STATE_DISABLED) break;
        		
                try{
                    Thread.sleep(mWifiTimeoutSteps);
                }catch(Exception e){
                	Log.w(mTag, e);
                }
        	}
        	if(i >= mWifiTimeout) throw new TimeoutException("Disabling wifi ap timed out.");
            Log.d(mTag, "Wifi ap disabled");
            
            // Enable wifi if it was enabled beforehand
            if(mWifiStatePrevious == WifiManager.WIFI_STATE_ENABLED || mWifiStatePrevious == WifiManager.WIFI_STATE_ENABLING || mWifiStatePrevious == WifiManager.WIFI_STATE_UNKNOWN){
                Log.d(mTag, "enable wifi: calling");
                mWifiNetworkManager.enableWifi();
            }

            // Reset flag
            mWifiStatePrevious = WifiManager.WIFI_STATE_UNKNOWN;
        }
        
    }
    
    
    /**
     * Returns the state of the wifi access point by performing an invoke.
     * @return an int representing the state of the wifi access point.
     */
    public int getWifiAPState(){
    	
        int state = WIFI_AP_STATE_UNKNOWN;
        
        try{
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            state = (Integer) method.invoke(mWifiManager);
        }catch(Exception e){
        	e.printStackTrace();
        }

        if(state >= 10) state -= 10;

        Log.d(mTag, "getWifiAPState.state " + String.valueOf(state));
        
        return state;
        
    }
    
	
}
