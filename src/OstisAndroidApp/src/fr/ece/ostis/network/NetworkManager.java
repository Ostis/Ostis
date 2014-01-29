package fr.ece.ostis.network;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

/**
 * Class used for managing all network related tasks, such as wi-fi, 3g/4g and access point!!.
 * 
 * TODO Implement function to retrieve current wifi networks.
 * 
 * @see http://stackoverflow.com/questions/2513713/how-to-use-3g-connection-in-android-application-instead-of-wi-fi/4756630#4756630 for original source code.
 * 
 * @see http://stackoverflow.com/questions/7048922/android-2-3-wifi-hotspot-api for ap functions
 * 
 * @author Nicolas Schurando
 * @version 2014-01-28
 */
public class NetworkManager {

	
	/** TODO */
	private Context mContext = null;
	
	
	/** TODO */
	private WifiManager mWifiManager = null;
	
	
	/** TODO */
	private WifiLock mWifiLock = null;
	
	
	/** TODO */
	private ConnectivityManager mConnectivityManager = null;
	

	/** TODO */
	private Thread mPingerThread = null;
	
	
	/** TODO */
    private AtomicBoolean mHipriEnabled = new AtomicBoolean(false);
	
    
    /** Log tag. */
    private static final String mTag = "NetworkManager";
    
    
    /*
     * TODO
     */
    private static final String mAccessPointName = "OstisAP";
    private static final int mAccessPointChannel = 9;
    
    
    /*
     * TODO
     */
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_DISCONNECTED = 2;
	public static final int WIFI_AP_STATE_UNKNOWN = -1;
	public static final int WIFI_AP_STATE_DISABLING = 0;
    public static final int WIFI_AP_STATE_DISABLED = 1;
    public static final int WIFI_AP_STATE_ENABLING = 2;
    public static final int WIFI_AP_STATE_ENABLED = 3;
    public static final int WIFI_AP_STATE_FAILED = 4;
	
	/*
	 * TODO
	 */
	protected int mMobileStatePrevious = -1;
	protected int mWifiStatePrevious = WifiManager.WIFI_STATE_UNKNOWN;
	
	
	/*
	 * TODO
	 */
	protected static final int mWifiTimeout = 5000;
	protected static final int mWifiTimeoutSteps = 500;
	protected static final int mMobileTimeout = 5000;
	protected static final int mMobileTimeoutSteps = 500;
	
	
	/**
	 * TODO
	 * @param context
	 */
	public NetworkManager(Context context) throws Exception{
		
		// Retrieve context
		mContext = context;
		
		// Get wifi manager
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		
		// Get connectivity manager
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
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
		NetworkInfo mWifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}
	
	
	/**
	 * TODO
	 */
	public void enableWifi(){
		setWifiEnabled(true);
	}
	
	
	/**
	 * TODO
	 */
	public void disableWifi(){
		setWifiEnabled(false);
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
	 * @param enabled
	 * @throws NullPointerException
	 */
	protected void setWifiEnabled(boolean enabled) throws NullPointerException{
		if(mWifiManager == null) throw new NullPointerException("Wifi manager is null.");
		mWifiManager.setWifiEnabled(enabled);
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
	public void startWifiScan() throws Exception{
		
		// Ensure that wifi has been enabled.
		if(isWifiEnabled() != true) throw new Exception("Wifi has not been enabled.");
		
		// Start scan
		if(mWifiManager.startScan() != true) throw new Exception("Wifi scan failed.");
		
	}
	
	
	/**
	 * TODO
	 * @return
	 * @throws Exception 
	 */
	public List<ScanResult> getWifiResults() throws Exception{
		
		// Ensure that wifi has been enabled.
		if(isWifiEnabled() != true) throw new Exception("Wifi has not been enabled.");
		
		// Return
		return mWifiManager.getScanResults();
		
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
        	mWifiStatePrevious = getWifiState();
        }

        // Disable wireless
        if(enabled && mWifiManager.getConnectionInfo() != null){
            Log.d(mTag, "Wifi disabling");
            disableWifi();
            waitForWifiDisabled();
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
                enableWifi();
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
	
	
	/**
	 * TODO
	 * @throws NullPointerException
	 * @throws InvokeFailedException 
	 * @throws TimeoutException
	 */
	public void enableMobileConnection() throws NullPointerException, InvokeFailedException, TimeoutException{
		
		// Enable mobile data
		setMobileDataEnabled(true);
		
		// Wait until connected
	    try{
	    	State checkState = null;
	        for(int counter = 0; counter < mMobileTimeout; counter += mMobileTimeoutSteps){
	            checkState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	            if(checkState.compareTo(State.CONNECTED) == 0) return;
	            Thread.sleep(mMobileTimeout);
	        }
	    }catch(InterruptedException e){
	    	Log.w(mTag, e);
	    }
	    
	    // Throw timeout exception
	    throw new TimeoutException("Enabling mobile connection timed out.");
	    
	}
	
	
	/**
	 * TODO
	 * @throws NullPointerException
	 * @throws InvokeFailedException 
	 * @throws TimeoutException 
	 */
	public void disableMobileConnection() throws NullPointerException, InvokeFailedException, TimeoutException{
		
		// Disable mobile data
		setMobileDataEnabled(false);
		
		// Wait until disconnected
	    try{
	    	State checkState = null;
	        for(int counter = 0; counter < 60; counter++){
	            checkState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	            if(checkState.compareTo(State.DISCONNECTED) == 0) return;
	            Thread.sleep(500);
	        }
	    }catch(InterruptedException e){}
	    
	    // Throw timeout exception
	    throw new TimeoutException();
	    
	}
	
	
	/**
	 * TODO
	 * @param enabled
	 * @throws NullPointerException
	 * @throws InvokeFailedException
	 */
	protected void setMobileDataEnabled(boolean enabled) throws NullPointerException, InvokeFailedException{
        
		// Ensure connectivity manager is set
		if(mConnectivityManager == null) throw new NullPointerException("Connectivity manager is null.");
		
		try{
			
			// Retrieve the class for the connectivity manager
	        final Class classConnectivityManager = Class.forName(mConnectivityManager.getClass().getName());
	        
	        // Sets the inner service field accessible
	        final Field fieldConnectivityManager = classConnectivityManager.getDeclaredField("mService");
	        fieldConnectivityManager.setAccessible(true);
	        
	        // Retrieve the interface
	        final Object interfaceConnectivityManager = fieldConnectivityManager.get(mConnectivityManager);
	        final Class classInterfaceConnectivityManagerClass = Class.forName(interfaceConnectivityManager.getClass().getName());
	        
	        // Retrieve the method, set it accessible, and invoke it
	        final Method setMobileDataEnabledMethod = classInterfaceConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	        setMobileDataEnabledMethod.setAccessible(true);
	        setMobileDataEnabledMethod.invoke(interfaceConnectivityManager, enabled);
	        
		}catch(IllegalArgumentException e){
			Log.w(mTag, e);
			throw new InvokeFailedException();
		}catch(IllegalAccessException e){
			Log.w(mTag, e);
			throw new InvokeFailedException();
		}catch(InvocationTargetException e){
			Log.w(mTag, e);
			throw new InvokeFailedException();
		}catch(ClassNotFoundException e){
			Log.w(mTag, e);
			throw new InvokeFailedException();
		}catch(NoSuchFieldException e){
			Log.w(mTag, e);
			throw new InvokeFailedException();
		}catch(NoSuchMethodException e){
			Log.w(mTag, e);
			throw new InvokeFailedException();
		}
        
     }
	
	
	/**
	 * TODO
	 * @param dns
	 */
	public void setWifiDns(String dns){
		Log.d(mTag, "setWifiDns");
	    android.provider.Settings.System.putString(mContext.getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS1, dns);
	    android.provider.Settings.System.putString(mContext.getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS2, dns);
	}
	
	
	/**
	 * TODO
	 * @throws Exception 
	 */
	protected void enableHipri(ArrayList<Integer> addresses) throws Exception{
		
		// Don't do anything if we are connecting. On the other hand re-new connection if we are connected.
		State state = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
		Log.d("NetworkManager", "Hipri state: " + state);
		if(state.compareTo(State.CONNECTING) == 0){
			Log.d("NetworkManager", "Hipri already connecting, no need to go further.");
			return;
		}
		
		// Debug : reset wifi dns
		Log.d("NetworkManager", "Setting wifi dns");
		setWifiDns("8.8.8.8");
		
	    // (Re-)Activate mobile connection in addition to other connection already activated
	    Log.d("NetworkManager", "Starting to use Hipri profile.");
	    int resultInt = mConnectivityManager.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
	    switch(resultInt){
	    
	    	case -1: // -1 means errors
	    		Log.e("NetworkManager", "Hipri request failed.");
	    		throw new Exception("Could not start using hipri profile, system failed.");
	    		
	    	case 0: // 0 means already enabled
		        Log.e("NetworkManager", "Hipri already enabled, no need to perform additional network settings.");
		        return;
		        
	    	case 1: // 1 means enabled
		        Log.e("NetworkManager", "Hipri sucessfully enabled.");
		        break;
		        
	    	default: // Other values can be returned, because this method is vendor specific
		        Log.e("NetworkManager", "Hipri request result code is unknown : " + String.valueOf(resultInt));
	    		throw new Exception("Could not start using hipri profile, the result was unexcepted.");
	    }
		
	    // Wait some time needed to connection manager for waking up
	    try{
	    	State checkState = null;
	        for(int counter = 0; counter < 60; counter++){
	            checkState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
	            if(checkState.compareTo(State.CONNECTED) == 0) break;
	            Thread.sleep(500);
	        }
	    }catch(InterruptedException e){}
	    
	    // Add routes
	    for (int address : addresses){
	    	if(mConnectivityManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_HIPRI, address)){
	    		Log.d("NetworkManager", "Route to host " + String.valueOf(address) + " added.");
	    	}else{
	    		Log.e("NetworkManager", "Route to host " + String.valueOf(address) + " failed.");
	    		throw new Exception("Could not add a route for host " + String.valueOf(address) + ", hipri will not work.");
	    	}
	    }
		
	}
	
	
	protected void disableHipri(){
		
		mHipriEnabled.set(false);
		
		mConnectivityManager.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
		
	}
	
	
	/**
	 * TODO
	 * @param addresses
	 */
	public void startHipri(final ArrayList<Integer> addresses){
		
		// Set enable flag
		mHipriEnabled.set(true);
		
		// Defines the pinger thread as renewing the hipri connection every 20 seconds. No routing setup is needed.
		mPingerThread = new Thread(new Runnable(){
            @Override public void run(){
                while(mHipriEnabled.get()){
                	try{
						enableHipri(addresses);
						Thread.sleep(20000);
					}catch(InterruptedException e){
						stopHipri(); // Or do nothing ?
					}catch(Exception e){
						stopHipri();
					}
                }
            }
        });
		
		// Start pinger thread
        mPingerThread.start();
		
	}
	
	
	/**
	 * TODO
	 */
	public void stopHipri(){
		
		// Reset flag
        mHipriEnabled.set(false);
		
        // Diable hipri
		disableHipri();
        
        // Stop pinger thread
		if(mPingerThread != null){
	        mPingerThread.interrupt();
	        mPingerThread = null;
		}
        
	}
	
	
	/*
	 * Enable mobile connection for a specific address.
	 * @param addresses the addresses to enable.
	 * @return true for success, else false
	 * @throws Exception 
	 * @deprecated
	 *
	public boolean startHipriConnection(ArrayList<Integer> addresses){

	    // Skip if hipri is already connected or connecting
	    State state = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
	    Log.d("NetworkManager", "TYPE_MOBILE_HIPRI network state: " + state);
	    if (state.compareTo(State.CONNECTED) == 0 || state.compareTo(State.CONNECTING) == 0) {
	        return true;
	    }

	    // Activate mobile connection in addition to other connection already activated
	    Log.d("NetworkManager", "Starting to use HIPRI profile.");
	    int resultInt = mConnectivityManager.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
	    switch(resultInt){
	    
	    	case -1: // -1 means errors
	    		Log.e("NetworkManager", " -> Wrong result of startUsingNetworkFeature, maybe problems.");
	    		return false;
	    		
	    	case 0: // 0 means already enabled
		        Log.e("NetworkManager", " -> Hipri already enabled, no need to perform additional network settings.");
		        return true;
		        
	    	case 1: // 1 means enabled
		        Log.e("NetworkManager", " -> Hipri sucessfully enabled.");
		        break;
		        
	    	default: // Other values can be returned, because this method is vendor specific
		        Log.e("NetworkManager", " -> Result code is unknown (" + String.valueOf(resultInt) + ").");
		        break;
	    }	    
	    
	    // Wait some time needed to connection manager for waking up
	    try{
	    	State checkState = null;
	        for(int counter = 0; counter < 60; counter++){
	            checkState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).getState();
	            if(checkState.compareTo(State.CONNECTED) == 0) break;
	            Thread.sleep(500);
	        }
	    }catch(InterruptedException e){}
	    
	    // Add a route to the hipri for each of the addresses 
	    for (int address : addresses){
	    	if(mConnectivityManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_HIPRI, address)){
	    		Log.d("NetworkManager", "Route to host " + String.valueOf(address) + " added.");
	    	}else{
	    		Log.e("NetworkManager", "Route to host " + String.valueOf(address) + " failed.");
	    		return false;
	    	}
	    }
	    
	    // Return true
	    return true;
	    
	}*/

	
	/*
	 * TODO
	 * @deprecated
	 *
	public void stopHipriConnection() {
        mConnectivityManager.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
    }*/
	
	
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
	 * Transform host name in int value used by {@link ConnectivityManager.requestRouteToHost} method
	 * @param hostname
	 * @return the translation of the hostname to an integer
	 * @throws UnknownHostException if the host doesn't exist.
	 */
	public int lookupHost(String hostname) throws UnknownHostException{
		
	    InetAddress inetAddress = InetAddress.getByName(hostname);
	    
	    Log.d("NetworkManager", "Host " + hostname + " > " + inetAddress.toString());
	    
	    byte[] addrBytes;
	    int addrInt;
	    
	    addrBytes = inetAddress.getAddress();
	    addrInt = ((addrBytes[3] & 0xff) << 24)
	            | ((addrBytes[2] & 0xff) << 16)
	            | ((addrBytes[1] & 0xff) << 8 )
	            |  (addrBytes[0] & 0xff);
	    
	    return addrInt;
	    
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 */
	public class InvokeFailedException extends Exception {
		public InvokeFailedException(){
			super();
		}
		public InvokeFailedException(String detailMessage){
			super(detailMessage);
		}
	}
	
	
}


