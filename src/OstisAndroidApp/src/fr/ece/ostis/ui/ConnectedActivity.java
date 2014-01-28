package fr.ece.ostis.ui;

import java.lang.ref.WeakReference;

import fr.ece.ostis.OstisService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-28
 */
public abstract class ConnectedActivity extends Activity {
	
	
	/** TODO */
	protected boolean mOstisServiceIsBound = false;


	/** TODO */
	protected Messenger mMessengerToOstisService = null;


	/** TODO */
	protected Messenger mMessengerFromOstisService = new Messenger(new IncomingMessageFromServiceHandler(this));
	
	
	/** Log tag. */
	protected static final String mTag = "ConnectedActivity";
	
	
    /** Class for monitoring the state of the ostis service. */
	protected final ServiceConnection mOstisServiceConnection = new ServiceConnection(){
		

	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service){
	        Log.i(mTag, "Bound to ostis service.");
	        mMessengerToOstisService = new Messenger(service);
	        Message message = Message.obtain(null, OstisService.MSG_REGISTER_CLIENT);
	        message.replyTo = mMessengerFromOstisService;
	        try{
	        	mMessengerToOstisService.send(message);
	        	ConnectedActivity.this.onBoundToOstisService();
	        }catch(RemoteException e){
	        	Log.w(mTag, e);
	        	mOstisServiceIsBound = false;
	            // In this case the service has crashed before we could even do anything with it
	        }
	    }

	    
	    @Override
	    public void onServiceDisconnected(ComponentName name){
	        Log.i(mTag, "Unbound from ostis service unexpectedly.");
        	mOstisServiceIsBound = false;
	        mMessengerFromOstisService = null;
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
    	
    	if(mOstisServiceIsBound != true){
	    	
    		// Log
    		Log.i(mTag, "Binding to ostis service.");
    		
	    	// Bind to service
		    bindService(new Intent(this, OstisService.class), mOstisServiceConnection, Context.BIND_AUTO_CREATE);
		    
		    // Set flag
		    mOstisServiceIsBound = true;
	        
    	}
    	
    }
	

	/**
	 * TODO
	 */
	protected void doUnbindOstisService(){
    	
    	if(mOstisServiceIsBound != false){

    		// Log
    		Log.i(mTag, "Unbinding from ostis service.");
    		
    		// Warn service
	        try{
		        Message message = Message.obtain(null, OstisService.MSG_UNREGISTER_CLIENT);
		        message.replyTo = mMessengerFromOstisService;
	        	mMessengerToOstisService.send(message);
	        }catch(RemoteException e){
	        	Log.w(mTag, e);
	        }
    		
    		// Unbind from service
            unbindService(mOstisServiceConnection);
            
            // Set flag
            mOstisServiceIsBound = false;
            
            // Log
            Log.i(mTag, "Unbound from ostis service.");
            
            // Call abstract function
            onUnboundFromOstisService();
            
    	}
    	
    }
	
	
	/**
	 * 
	 * @author Nicolas Schurando
	 * @version 2014-01-28
	 */
	protected static class IncomingMessageFromServiceHandler extends Handler{
		
		
		/** Reference to the activity. */
		private final WeakReference<ConnectedActivity> mActivityReference;
		
		
		/**
		 * Constructor.
		 * @param activity
		 */
		IncomingMessageFromServiceHandler(ConnectedActivity activity){
			mActivityReference = new WeakReference<ConnectedActivity>(activity);
		}

		
        @Override public void handleMessage(Message message){
        	
			// Retrieve the activity instance
			ConnectedActivity activity = mActivityReference.get();
        	
			// Pass the message to the activity
			activity.onMessageFromOstisService(message);
			
        }
        
    }
	
	
	/**
	 * TODO
	 * @param message
	 */
	protected abstract void onMessageFromOstisService(Message message);
	
	
	/**
	 * TODO
	 */
	protected abstract void onBoundToOstisService();
	
	
	/**
	 * TODO
	 */
	protected abstract void onUnboundFromOstisService();
	
	
	/**
	 * TODO
	 * @param what
	 * @throws RemoteException 
	 */
	protected void sendMessageToOstisService(int what) throws RemoteException{
		Message message = Message.obtain(null, what);
        message.replyTo = mMessengerFromOstisService;
        mMessengerToOstisService.send(message);
	}
	
}
