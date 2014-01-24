package fr.ece.ostis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

/**
 * Class used for managing all network related tasks, including wi-fi
 * to the drone, and 3g connectivity to the voice recognition service.
 * 
 * TODO Implement function to retrieve current wifi networks.
 * 
 * @see http://stackoverflow.com/questions/2513713/how-to-use-3g-connection-in-android-application-instead-of-wi-fi/4756630#4756630 for original source code.
 * 
 * @author Nicolas Schurando
 * @version 2014-01-20
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
	
    
    /*
     * TODO
     */
    public static final int KEY_MOBILE = 1;
    public static final int KEY_WIFI = 2;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_DISCONNECTED = 2;
    
    
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
	 * @throws NullPointerException
	 * @throws InvokeFailedException 
	 * @throws TimeoutException
	 * @deprecated Shouldn't be used.
	 */
	public void enableMobileConnection() throws NullPointerException, InvokeFailedException, TimeoutException{
		
		// Enable mobile data
		setMobileDataEnabled(true);
		
		// Wait until connected
	    try{
	    	State checkState = null;
	        for(int counter = 0; counter < 60; counter++){
	            checkState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
	            if(checkState.compareTo(State.CONNECTED) == 0) return;
	            Thread.sleep(500);
	        }
	    }catch(InterruptedException e){}
	    
	    // Throw timeout exception
	    throw new TimeoutException();
	    
	}
	
	
	/**
	 * TODO
	 * @throws NullPointerException
	 * @throws InvokeFailedException 
	 * @throws TimeoutException 
	 * @deprecated Shouldn't be used.
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
	 * @deprecated Shouldn't be used.
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
			e.printStackTrace();
			throw new InvokeFailedException();
		}catch(IllegalAccessException e){
			e.printStackTrace();
			throw new InvokeFailedException();
		}catch(InvocationTargetException e){
			e.printStackTrace();
			throw new InvokeFailedException();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
			throw new InvokeFailedException();
		}catch(NoSuchFieldException e){
			e.printStackTrace();
			throw new InvokeFailedException();
		}catch(NoSuchMethodException e){
			e.printStackTrace();
			throw new InvokeFailedException();
		}
        
     }
	
	
	/**
	 * TODO
	 * @param dns
	 */
	public void setWifiDns(String dns){
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


