package fr.ece.ostis.ui;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.OstisService.OstisServiceBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-30
 */
public abstract class ConnectedActivity extends Activity{
	
	
	/** TODO */
	protected OstisService mService = null;
	
	
	/** TODO */
	protected boolean mBound = false;
	
	
	/** Log tag. */
	protected static final String mTag = "ConnectedActivity";
	
	
    /** Class for monitoring the state of the ostis service. */
	protected final ServiceConnection mOstisServiceConnection = new ServiceConnection(){
		

	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service){
	    	Log.i(mTag, "Bound to ostis service.");
	    	
			OstisServiceBinder binder = (OstisServiceBinder) service;
			mService = binder.getService();
			mBound = true;
			
			onBoundToOstisService();
	    }

	    
	    @Override
	    public void onServiceDisconnected(ComponentName name){
	    	Log.i(mTag, "Unbound from ostis service unexpectedly.");
        	
	    	mBound = false;
        	mService = null;
        	
	        onUnboundFromOstisService();
	    }

	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Start service
		doStartService();
		
	}
	
	
	@Override
	protected void onResume(){
		
		// Super
		super.onResume();
		
		// Unbind
		doBindOstisService();
		
	}
	
	
	@Override
	protected void onPause(){
		
		// Unbind
		doUnbindOstisService();
		
		// Super
		super.onPause();
		
	}
	
	
	/**
	 * TODO
	 */
    protected void doStartService(){
    	
    	// Log
    	Log.i(mTag, "Starting ostis service.");
    	
    	// Start service via intent
    	Intent _ServiceIntent = new Intent(ConnectedActivity.this, OstisService.class);
    	startService(_ServiceIntent);
    	
    }
    
	
    /**
     * TODO
     */
	protected void doBindOstisService(){
    	if(mBound != true){
    		Log.i(mTag, "Binding to ostis service.");
		    bindService(new Intent(this, OstisService.class), mOstisServiceConnection, Context.BIND_AUTO_CREATE);
    	}
    }
	

	/**
	 * TODO
	 */
	protected void doUnbindOstisService(){
    	
    	if(mBound != false){

    		// Log
    		Log.i(mTag, "Unbinding from ostis service.");
    		
    		// TODO Warn service or remove callbacks ?
    		
    		// Unbind from service
            unbindService(mOstisServiceConnection);
            
            // Set flag
            mBound = false;
            
            // Log
            Log.i(mTag, "Unbound from ostis service.");
            
            // Call abstract function
            onUnboundFromOstisService();
    	}
    	
    }
	
	
	/**
	 * TODO
	 */
	protected abstract void onBoundToOstisService();
	
	
	/**
	 * TODO
	 */
	protected abstract void onUnboundFromOstisService();
	
}
