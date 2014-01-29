package fr.ece.ostis.network;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;


/**
 * TODO
 * @see http://stackoverflow.com/questions/2513713/how-to-use-3g-connection-in-android-application-instead-of-wi-fi/4756630#4756630
 * @author Nicolas Schurando
 * @version 2014-01-29
 */
public class MobileNetworkManager extends NetworkManager{
	
	
	/** Log tag. */
	protected static final String mTag = "MobileNetworkManager";

	
	/** TODO */
	protected Context mContext = null;
	
	
	/** TODO */
	protected ConnectivityManager mConnectivityManager = null;
	

	/** TODO */
	protected Thread mPingerThread = null;
	
	
	/** TODO */
    protected AtomicBoolean mHipriEnabled = new AtomicBoolean(false);
    
    
    /** TODO */
	protected static final int mMobileTimeout = 5000;
	
	
	/** TODO */
	protected static final int mMobileTimeoutSteps = 500;
	
	
	/**
	 * TODO
	 * @param context
	 */
	public MobileNetworkManager(Context context){
		
		// Retrieve context
		mContext = context;
		
		// Get connectivity manager
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
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
	        final Class<?> classConnectivityManager = Class.forName(mConnectivityManager.getClass().getName());
	        
	        // Sets the inner service field accessible
	        final Field fieldConnectivityManager = classConnectivityManager.getDeclaredField("mService");
	        fieldConnectivityManager.setAccessible(true);
	        
	        // Retrieve the interface
	        final Object interfaceConnectivityManager = fieldConnectivityManager.get(mConnectivityManager);
	        final Class<?> classInterfaceConnectivityManagerClass = Class.forName(interfaceConnectivityManager.getClass().getName());
	        
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
		//Log.d("NetworkManager", "Setting wifi dns");
		//setWifiDns("8.8.8.8");
		
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
	
}
