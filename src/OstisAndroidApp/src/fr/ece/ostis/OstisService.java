package fr.ece.ostis;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.actions.Action;
import fr.ece.ostis.actions.ActionManager;
import fr.ece.ostis.lang.LanguageManager;
import fr.ece.ostis.speech.SpeechRecognitionService;
import fr.ostis.ece.network.NetworkManager;
import fr.ostis.ece.network.NetworkManager.InvokeFailedException;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;


/**
 * TODO Docu
 * TODO
 *  1 - Connect to 3G
 *  2 - Retrieve and store IP addresses for dl-ssl.l.google.com and m.google.com and mobile.l.google.com
 *  3 - Shutdown 3G ?
 *  4 - Start wi-fi
 *  5 - Add hipri routes for retrieved ip addresses
 *  6 - Check if drone is present
 * TODO Include reference to the drone proxy.
 * TODO Add a timer to relaunch enableHipri every 50 seconds
 * @author Nicolas Schurando
 * @version 2014-01-22
 */
public class OstisService extends Service{
	
	
	/** Message code : register a new client */
	public static final int MSG_REGISTER_CLIENT = 1;
	
	
	/** Message code : unregister an existing client */
	public static final int MSG_UNREGISTER_CLIENT = 2;
	
	
	/** Message code : the status of the network has changed */
	public static final int MSG_NETWORK_STATUS_UPDATED = 3;
	
	
	public static final int MSG_HIPRI_START = 4;
	public static final int MSG_HIPRI_STOP = 5;
	public static final int MSG_SPEECH_START = 6;
	public static final int MSG_SPEECH_STOP = 7;
	public static final int MSG_VOICE_RESULTS = 8;
	public static final int MSG_DRONE_CONNECT = 10;
	public static final int MSG_DRONE_CONNECTION_SUCCESS = 11;
	public static final int MSG_DRONE_CONNECTION_FAILED = 12;
	public static final int MSG_DRONE_DISCONNECT = 13;
	
	
	/** Reference to the action manager. */
	protected ActionManager mActionManager = null;
	
	
	/** List of client messengers. */
	protected ArrayList<Messenger> mMessengersToClients = new ArrayList<Messenger>();
	
	
	/** Target we publish for clients to send messages to IncomingHandler. */
	protected Messenger mMessengerFromClients = new Messenger(new IncomingMessageFromClientHandler(this));
	
	
	/** Local messenger as receiver we publish to the speech recognizer. */
	protected Messenger mMessengerFromSpeechService = new Messenger(new IncomingMessageFromSpeechRecognizer(this)); 
	
	
	/** TODO */
	protected Messenger mMessengerToSpeechService = null;
	
	
	/** Reference to the speech recognition service. */
	protected SpeechRecognitionService mSpeechRecognitionService = null; 
	
	
	/** Reference to the language manager. */
	protected LanguageManager mLanguageManager = null;
	
	
	/** TODO */
	protected boolean mSpeechServiceIsBound = false;
	
	
	/** Reference to the wakelock system. */
	protected WakeLock mWakeLock = null;

	
	/** Reference to the network manager. */
	protected NetworkManager mNetworkManager = null;
	
	
	/** Reference to the javadrone api */
	protected static ARDrone mDrone = null;
	

    /** Class for monitoring the state of the speech recognition service. */
    private final ServiceConnection mServiceSpeechConnection = new ServiceConnection(){
		
    	
    	/**
    	 * Called when a connection to the Service has been established, with the IBinder of the communication channel to the Service.
    	 * @param name The concrete component name of the service that has been connected.
		 * @param service The IBinder of the Service's communication channel, which you can now make calls on.
    	 */
	    @Override public void onServiceConnected(ComponentName name, IBinder service){

	    	mMessengerToSpeechService = new Messenger(service);
	        Message message = Message.obtain(null, SpeechRecognitionService.MSG_REGISTER_CLIENT);
	        message.replyTo = mMessengerFromSpeechService;
	        try{
	        	mMessengerToSpeechService.send(message);
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
	        mMessengerToSpeechService = null;
	        
	    }

	};
	
	
	/**
	 * TODO
	 */
	@Override public void onCreate(){
		
		// Super
		super.onCreate();
		
		// Obtain wakelock
		acquireWakeLock();
		
		// Bind to speech recognition service
		doBindSpeechService();
		
		// Start language manager
		mLanguageManager = new LanguageManager();
		
		// Start action manager
		mActionManager = new ActionManager(mLanguageManager.getCurrentLocale(), this);
		
		// Start the network manager
		try{
			mNetworkManager = new NetworkManager(this);
		}catch (Exception e){
			e.printStackTrace();
			mNetworkManager = null;
		}
		
	}
	
	
	/**
	 * TODO
	 */
	@Override public void onDestroy(){
		
		// Unbind from speech recognition service
		doUnbindSpeechService();
		
		// Restore network
		restoreNetwork();
		
		// Release wakelock
		releaseWakeLock();
		
		// Cleanup
		mMessengersToClients.clear();
		
		// Super
		super.onDestroy();		
		
	}

	
	/**
	 * Acquires a wakelock that prevents the service from stopping when the user powers-off the screen.
	 */
	protected void acquireWakeLock(){
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if(mWakeLock == null) mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OstisWakeLock");
		if(!mWakeLock.isHeld()) mWakeLock.acquire();
	}
	
	
	/**
	 * Releases the wakelock previously acquired.
	 */
	protected void releaseWakeLock(){
		if(mWakeLock != null && mWakeLock.isHeld()) mWakeLock.release();
	}
	
	
	/**
	 * TODO
	 */
	protected void prepareNetwork(){
		
		// Launch task
		new PrepareNetworkTask().execute(
				"8.8.8.8",
				"google.com",
				"m.google.com",
				"web.google.com",
				"dl-ssl.google.com",
				"dl-ssl.l.google.com",
				"mobile.google.com",
				"mobile.l.google.com");
		
	}

	
	protected class PrepareNetworkTask extends AsyncTask<String, Void, Void>{
	
		private Exception mException = null;
		
		protected Void doInBackground(String... hosts){
			
			// Step 1 : Connect to mobile network
			try {
				Log.d("OstisService", "prepareNetwork -> disabling wifi");
				mNetworkManager.disableWifi();
				Log.d("OstisService", "prepareNetwork -> wifi disabled");
				OstisService.this.publishNetworkStatus(NetworkManager.STATUS_DISCONNECTED, NetworkManager.STATUS_DISCONNECTED);
				Log.d("OstisService", "prepareNetwork -> enabling mobile");
				mNetworkManager.enableMobileConnection();
				Log.d("OstisService", "prepareNetwork -> mobile enabled");
				OstisService.this.publishNetworkStatus(NetworkManager.STATUS_CONNECTED, NetworkManager.STATUS_DISCONNECTED);
			}catch(InvokeFailedException e){
				e.printStackTrace();
				mException = e;
				return null;
			}catch(TimeoutException e){
				e.printStackTrace();
				mException = e;
				return null;
			}
			
			// Step 2 : Retrieve IP addresses
			Log.d("OstisService", "prepareNetwork -> starting lookup");
			ArrayList<Integer> addresses = new ArrayList<Integer>();
			for(int i = 0; i < hosts.length; i++){
				try {
					addresses.add(mNetworkManager.lookupHost(hosts[i]));
				}catch(UnknownHostException e){
					e.printStackTrace();
					mException = e;
					return null;
				}catch(Exception e){
					e.printStackTrace();
					mException = e;
					return null;
				}
			    if(isCancelled()) break;
			}
			Log.d("OstisService", "prepareNetwork -> lookup finished");
			
			// Step 3 : Stop normal mobile network and start hipri mobile network
			Log.d("OstisService", "prepareNetwork -> starting hipri");
			mNetworkManager.startHipri(addresses);
			Log.d("OstisService", "prepareNetwork -> hipri started");
			
			// Step 4 : Start Wi-Fi
			Log.d("OstisService", "prepareNetwork -> enabling wifi");
			mNetworkManager.enableWifi();
			Log.d("OstisService", "prepareNetwork -> wifi enabled");
			OstisService.this.publishNetworkStatus(NetworkManager.STATUS_CONNECTED, NetworkManager.STATUS_CONNECTED);
			Log.d("OstisService", "prepareNetwork -> obtaining wifi lock");
			mNetworkManager.acquireWifiLock(); // TODO Release wakelock at some point
			Log.d("OstisService", "prepareNetwork -> wifi wake locked");
			
			// Return
			return null;
			
		}
		
		protected void onPostExecute(ArrayList<Integer> addresses){
			if(mException != null){
				// TODO : handle exception
			}else{
			    // TODO : send a message to client to indicate that the config is okay
			}
		}
		
	}
	
	
	/**
	 * TODO
	 * @param mobileStatus
	 * @param wifiStatus
	 */
	protected void publishNetworkStatus(int mobileStatus, int wifiStatus){
		
		Bundle messageBundle = new Bundle();
		messageBundle.putInt("StatusMobile", mobileStatus);
		messageBundle.putInt("StatusWifi", wifiStatus);
        Message message = Message.obtain(null, MSG_NETWORK_STATUS_UPDATED);
        message.setData(messageBundle);
		
		for(int clientsIterator = mMessengersToClients.size() - 1; clientsIterator >= 0; clientsIterator--){
		
	        try{
	        	mMessengersToClients.get(clientsIterator).send(message);
	        }catch(RemoteException e){
	        	e.printStackTrace();
	        }
	        
		}
		
	}
	
	/**
	 * 
	 */
	protected void restoreNetwork(){
		
		mNetworkManager.disableWifi();
		mNetworkManager.stopHipri();
		try {
			mNetworkManager.enableMobileConnection();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvokeFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * When binding to the service, we return an interface to our messenger
	 * for sending messages to the service.
	 */
	@Override public IBinder onBind(Intent intent){
		Log.d("OstisService", "onBind");
		return mMessengerFromClients.getBinder();
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-22
	 */
	protected static class IncomingMessageFromClientHandler extends Handler{
		
		
		/** Reference to the ostis service. */
		private final WeakReference<OstisService> mOstisServiceReference;
		
		
		/**
		 * Constructor.
		 * @param service
		 */
		IncomingMessageFromClientHandler(OstisService service){
			mOstisServiceReference = new WeakReference<OstisService>(service);
		}
	
		
		/**
		 * TODO
		 * @param message
		 */
		@Override public void handleMessage(Message message){
			
			// Log
			Log.d("OstisService<->Activities", "handleMessage | what = " + String.valueOf(message.what));

			// Retrieve the service instance
			OstisService service = mOstisServiceReference.get();
			
			// React according to the type of object of the message
			switch(message.what){

				// Register a new client
				case MSG_REGISTER_CLIENT:
					Log.d("OstisService", "handleMessage -> New client registered");
					service.mMessengersToClients.add(message.replyTo);
					break;
					
				// Unregister a client
				case MSG_UNREGISTER_CLIENT:
					Log.d("OstisService", "handleMessage -> Removing existing client");
					service.mMessengersToClients.remove(message.replyTo);
					break;
		
				case MSG_HIPRI_START:
					Log.d("OstisService", "handleMessage -> start debug");
					service.prepareNetwork();
					break;
					
				case MSG_HIPRI_STOP:
					Log.d("OstisService", "handleMessage -> stop debug");
					service.restoreNetwork();
					break;
					
				case MSG_SPEECH_START:
					Log.d("OstisService", "handleMessage -> start speech");
			        try{
				        Message message2 = Message.obtain(null, SpeechRecognitionService.MSG_START_LISTENING);
			        	service.mMessengerToSpeechService.send(message2);
			        }catch(RemoteException e){
			        	e.printStackTrace();
			        }
					break;
					
				case MSG_SPEECH_STOP:
					Log.d("OstisService", "handleMessage -> stop speech");
			        try{
				        Message message2 = Message.obtain(null, SpeechRecognitionService.MSG_CANCEL_LISTENING);
			        	service.mMessengerToSpeechService.send(message2);
			        }catch(RemoteException e){
			        	e.printStackTrace();
			        }
					break;
				
				// Connect to drone
				case MSG_DRONE_CONNECT:
					service.doDroneConnect();
					break;
					
				// Disconnect from drone
				case MSG_DRONE_DISCONNECT:
					service.doDroneDisconnect();
					break;

			}
			
		}
		
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-22
	 */
	protected static class IncomingMessageFromSpeechRecognizer extends Handler{
		
		
		/**
		 * Reference to the ostis service.
		 */
		private final WeakReference<OstisService> mServiceReference;
		
		
		/**
		 * Constructor.
		 * @param service
		 */
		IncomingMessageFromSpeechRecognizer(OstisService service){
			mServiceReference = new WeakReference<OstisService>(service);
		}
	
		
		/**
		 * TODO
		 * @param message
		 */
		@Override public void handleMessage(Message message){
			
			// Log
			Log.d("OstisService<->SpeechService", "handleMessage | what = " + String.valueOf(message.what));

			// Retrieve the service instance
			OstisService service = mServiceReference.get();
			
			// React according to the type of object of the message
			switch(message.what){

				// Received speech recognition results
	            case SpeechRecognitionService.MSG_FINISHED_WITH_RESULTS:
	            	
	            	// Log
	                Log.d("OstisService", "Received speech recognition results.");
	                
	                // Pass it to the action manager
	                ArrayList<String> _Sentences = message.getData().getStringArrayList("SpeechRecognitionResult");
	                if(_Sentences != null){
		    			for (int i = 0; i < _Sentences.size(); i++){
		    				Action actionMatched = service.mActionManager.matchCommandToRun(_Sentences.get(i));
		    				
		    				if(actionMatched != null){
		    					Log.d("OstisService", "Running action " + actionMatched.getId());
		    					try {
									actionMatched.run(OstisService.mDrone);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		    					return;
		    				}
		    			}
	                }
	                break;
		
			}
			
		}
		
	}

	
    /**
     * Binds to the speech recognition service.
     */
	protected void doBindSpeechService(){
    	if(mSpeechServiceIsBound != true){
		    bindService(new Intent(this, SpeechRecognitionService.class), mServiceSpeechConnection, Context.BIND_AUTO_CREATE);
		    mSpeechServiceIsBound = true;
    	}
    }
	

	/**
	 * Unbinds from the speech recognition service.
	 */
	protected void doUnbindSpeechService(){
    	if(mSpeechServiceIsBound != false){
	        try{
		        Message message = Message.obtain(null, SpeechRecognitionService.MSG_UNREGISTER_CLIENT);
		        message.replyTo = mMessengerFromSpeechService;
	        	mMessengerToSpeechService.send(message);
	        }catch(RemoteException e){
	        	e.printStackTrace();
	        }
    		unbindService(mServiceSpeechConnection);
    		mSpeechServiceIsBound = false;
    	}
    }
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-22
	 */
	protected class DroneConnectionTask extends AsyncTask<byte[], Integer, Boolean>{

		@Override protected Boolean doInBackground(byte[]... ips){
			
			byte[] droneIp = ips[0];
			
			try{
				
				OstisService.mDrone = new ARDrone(InetAddress.getByAddress(droneIp), 10000, 60000);
				OstisService.mDrone.connect();
				OstisService.mDrone.clearEmergencySignal();
				OstisService.mDrone.trim();
				OstisService.mDrone.waitForReady(10000);
				OstisService.mDrone.playLED(1, 10, 4);
				OstisService.mDrone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
				OstisService.mDrone.setCombinedYawMode(true);
				return true;
				
			}catch(Exception e){
				Log.e("DroneConnectionTask", "Failed to connect to drone.", e);
				
				try{
					
					OstisService.mDrone.clearEmergencySignal();
					OstisService.mDrone.clearImageListeners();
					OstisService.mDrone.clearNavDataListeners();
					OstisService.mDrone.clearStatusChangeListeners();
					OstisService.mDrone.disconnect();
					
				}catch(Exception e2){
					Log.w("DroneConnectionTask", "Failed to clear drone state.", e2);
				}
	
			}
			
			return false;
		}

		protected void onPostExecute(Boolean success){
			if(success.booleanValue()){
				onDroneConnected();
			}else{
				onDroneConnectionFailed();
			}
		}
	}
	
	
	/**
	 * TODO
	 */
	protected void doDroneConnect(){
		Log.d("OstisService", "doDroneConnect");
		(new DroneConnectionTask()).execute(new byte[]{(byte) 192, (byte) 168, (byte) 1, (byte) 1}); 
	}	
	
	
	/**
	 * TODO
	 */
	protected void doDroneDisconnect(){
		Log.d("OstisService", "doDroneDisconnect");
		
		if(OstisService.mDrone == null) return;
		
		try{
			OstisService.mDrone.clearEmergencySignal();
			OstisService.mDrone.clearImageListeners();
			OstisService.mDrone.clearNavDataListeners();
			OstisService.mDrone.clearStatusChangeListeners();
		}catch(Exception e){
			Log.w("OstisService", "Failed to clear drone state.", e);
		}
		
		try{
			OstisService.mDrone.disconnect();
		}catch(Exception e){
			Log.w("OstisService", "Failed to disconnect drone.", e);
		}
		
	}
	
	
	/**
	 * TODO
	 */
	protected void onDroneConnected(){
		Log.d("OstisService", "onDroneConnected");
		// TODO
	}
	
	
	/**
	 * TODO
	 */
	protected void onDroneConnectionFailed(){
		Log.d("OstisService", "onDroneConnectionFailed");
		// TODO
	}
}
