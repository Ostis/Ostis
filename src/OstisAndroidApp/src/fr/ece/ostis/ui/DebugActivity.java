package fr.ece.ostis.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import fr.ece.ostis.NetworkManager;
import fr.ece.ostis.OstisService;
import fr.ece.ostis.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ToggleButton;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-13
 */
public class DebugActivity extends Activity{

	
	/** TODO */
	protected boolean mOstisServiceIsBound = false;


	/** TODO */
	protected Messenger mMessengerToOstisService = null;


	/** TODO */
	protected Messenger mMessengerFromOstisService = new Messenger(new IncomingMessageFromServiceHandler(this));


    /** Class for monitoring the state of the ostis service. */
    private final ServiceConnection mOstisServiceConnection = new ServiceConnection(){
		
    	
    	/**
    	 * Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
    	 * @param name The concrete component name of the service that has been connected.
		 * @param service The IBinder of the Service's communication channel, which you can now make calls on.
    	 */
	    @Override public void onServiceConnected(ComponentName name, IBinder service){

	        mMessengerToOstisService = new Messenger(service);
	        Message message = Message.obtain(null, OstisService.MSG_REGISTER_CLIENT);
	        message.replyTo = mMessengerFromOstisService;
	        try{
	        	mMessengerToOstisService.send(message);
	        }catch (RemoteException e){
	        		
	        	// Log
	            e.printStackTrace();
	            
                // In this case the service has crashed before we could even do anything with it
	            // Nothing
	            
	        }
	        
	    }

	    
	    /**
	     * Called when a connection to the Service has been lost. This typically happens when the process hosting the
	     * service has crashed or been killed. This does not remove the ServiceConnection itself -- this binding to the
	     * service will remain active, and you will receive a call to onServiceConnected(ComponentName, IBinder) when
	     * the Service is next running.
	     * @param name The concrete component name of the service whose connection has been lost.
	     */
	    @Override public void onServiceDisconnected(ComponentName name){
	    	
	    	// Log
	        Log.d("MainActivity", "onServiceDisconnected");
	        
	        // Delete messenger
	        mMessengerFromOstisService = null;
	        
	    }

	};
	
	
	/**
	 * 
	 * @author Nicolas Schurando
	 * @version 2014-01-13
	 */
	protected static class IncomingMessageFromServiceHandler extends Handler{
		
		
		/** Reference to the activity. */
		private final WeakReference<DebugActivity> mActivityReference;
		
		
		/**
		 * Constructor.
		 * @param activity
		 */
		IncomingMessageFromServiceHandler(DebugActivity activity){
			mActivityReference = new WeakReference<DebugActivity>(activity);
		}
		
    	
    	/**
    	 * TODO
    	 */
        @Override public void handleMessage(Message message){
			
			// Log
			Log.d("MainActivity <-> OstisService", "handleMessage | what = " + String.valueOf(message.what));
        	
			// Retrieve the activity instance
        	DebugActivity activity = mActivityReference.get();
        	
            switch(message.what){
            
	            case OstisService.MSG_NETWORK_STATUS_UPDATED:
	            	
	            	if( message.getData() == null) break;
	            	
	            	int statusMobile = message.getData().getInt("StatusMobile");
	            	int statusWifi = message.getData().getInt("StatusWifi");
	            
            		if(statusWifi == NetworkManager.STATUS_CONNECTED) ((ImageView) activity.findViewById(R.id.imageViewWiFi)).setImageResource(R.drawable.icon_tick);
            		else ((ImageView) activity.findViewById(R.id.imageViewWiFi)).setImageResource(R.drawable.icon_cross);
            		
            		if(statusMobile == NetworkManager.STATUS_CONNECTED) ((ImageView) activity.findViewById(R.id.ImageView3G)).setImageResource(R.drawable.icon_tick);
            		else ((ImageView) activity.findViewById(R.id.ImageView3G)).setImageResource(R.drawable.icon_cross);
	            	
	                break;
	            	
	            case OstisService.MSG_DRONE_CONNECTION_SUCCESS:
	            	// TODO
	            	break;
	                
	            case OstisService.MSG_DRONE_CONNECTION_FAILED:
	            	// TODO
	            	break;
	                
	            default:
	                super.handleMessage(message);
            }
            
        }
        
    }
	
	
	/**
	 * TODO
	 * @param savedInstanceState
	 */
    @Override protected void onCreate(Bundle savedInstanceState) {
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set content view
		setContentView(R.layout.activity_debug_nico);

		// Start voice recognition service
	    doStartService();
	    
	    // Set callback on button
	    final ToggleButton _HipriButton = (ToggleButton) findViewById(R.id.toggleButtonHipri);
	    _HipriButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
		        Message _Message = new Message();
		        _Message.what = (_HipriButton.isChecked())?OstisService.MSG_HIPRI_START:OstisService.MSG_HIPRI_STOP; 

		        try{
		        	mMessengerToOstisService.send(_Message);
		        }catch (RemoteException e){
		            e.printStackTrace();
		        }
			}
		});
	    
	    // Set callback on button
	    final ToggleButton _SpeechButton = (ToggleButton) findViewById(R.id.toggleButtonSpeech);
	    _SpeechButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
		        Message _Message = new Message();
		        _Message.what = (_SpeechButton.isChecked())?OstisService.MSG_SPEECH_START:OstisService.MSG_SPEECH_STOP; 

		        try{
		        	mMessengerToOstisService.send(_Message);
		        }catch (RemoteException e){
		            e.printStackTrace();
		        }
			}
		});
	    
	    // Set callback on button
	    final Button _ConnectButton = (Button) findViewById(R.id.buttonDroneConnect);
	    _ConnectButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
		        Message _Message = new Message();
		        _Message.what = OstisService.MSG_DRONE_CONNECT; 

		        try{
		        	mMessengerToOstisService.send(_Message);
		        }catch (RemoteException e){
		            e.printStackTrace();
		        }
			}
		});
	    
	    // Set callback on button
	    final Button _DisconnectButton = (Button) findViewById(R.id.buttonDroneDisconnect);
	    _DisconnectButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
		        Message _Message = new Message();
		        _Message.what = OstisService.MSG_DRONE_DISCONNECT; 

		        try{
		        	mMessengerToOstisService.send(_Message);
		        }catch (RemoteException e){
		            e.printStackTrace();
		        }
			}
		});
	    
	}

	
	/**
	 * TODO
	 * @param menu
	 */
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// Return
		return true;
		
	}
	
	
	/**
	 * TODO
	 */
	@Override protected void onResume(){
		
		// Super
	    super.onResume();

	    // Bind to voice recognition service
	    doBindOstisService();
	    
	}

	
	/**
	 * TODO
	 */
	@Override protected void onPause(){

	    // Unbind from voice recognition service
        doUnbindOstisService();
		
		// Super
	    super.onPause();
   
	}

  
	
	/**
	 * TODO
	 */
    protected void doStartService(){
    	
    	// Start service via intent
    	Intent _ServiceIntent = new Intent(DebugActivity.this, OstisService.class);
    	startService(_ServiceIntent);
    	
    }

    
    /**
     * TODO
     */
	protected void doBindOstisService(){
    	
    	if(mOstisServiceIsBound != true){
	    	
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

    		// Warn service
	        try{
		        Message message = Message.obtain(null, OstisService.MSG_UNREGISTER_CLIENT);
		        message.replyTo = mMessengerFromOstisService;
	        	mMessengerToOstisService.send(message);
	        }catch(RemoteException e){
	        	e.printStackTrace();
	        }
    		
    		// Unbind from service
            unbindService(mOstisServiceConnection);
            
            // Set flag
            mOstisServiceIsBound = false;
            
    	}
    	
    }

}
