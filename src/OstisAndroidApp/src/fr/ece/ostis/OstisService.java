package fr.ece.ostis;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.actions.Action;
import fr.ece.ostis.actions.ActionManager;
import fr.ece.ostis.lang.LanguageManager;
import fr.ece.ostis.network.NetworkManager;
import fr.ece.ostis.network.NetworkManager.InvokeFailedException;
import fr.ece.ostis.speech.SpeechRecognitionService;
import fr.ece.ostis.ui.HomeActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.util.Log;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-29
 */
public class OstisService extends OstisServiceCommunicator{
	
	
	/*
	 * Constants
	 */
	public static final int MSG_NETWORK_STATUS_UPDATED = 3;		// Message code : the status of the network has changed
	public static final int MSG_HIPRI_START = 4;				// TODO REMOVE
	public static final int MSG_HIPRI_STOP = 5;					// TODO REMOVE
	public static final int MSG_SPEECH_START = 6;				// TODO REMOVE ?
	public static final int MSG_SPEECH_STOP = 7;				// TODO REMOVE ?
	public static final int MSG_VOICE_RESULTS = 8;				// Message code
	public static final int MSG_DRONE_CONNECT = 10;				// Message code
	public static final int MSG_DRONE_CONNECTION_SUCCESS = 11;	// Message code
	public static final int MSG_DRONE_CONNECTION_FAILED = 12;	// Message code
	public static final int MSG_DRONE_DISCONNECT = 13;			// Message code
	public static final int MSG_DRONE_STATUS_REQUEST = 14;		// Message code
	public static final int MSG_DRONE_STATUS_UPDATED = 15;		// Message code
	public static final int DRONE_STATUS_UNKNOWN = -1;			// Drone status
	public static final int DRONE_STATUS_DISCONNECTED = 0;		// Drone status
	public static final int DRONE_STATUS_CONNECTED = 1;			// Drone status
	public static final int NETWORK_METHOD_UNKNOWN = 0;			// Drone status
	public static final int NETWORK_METHOD_AP = 1;				// Drone status
	public static final int NETWORK_METHOD_HIPRI = 2;			// Drone status
	
	
    /** Log tag. */
    protected static final String mTag = "OstisService";
	
	
	/** Reference to the javadrone api */
	protected static ARDrone mDrone = null;
	
	
	/** Reference to the action manager. */
	protected ActionManager mActionManager = null;
	
	
	/** Reference to the language manager. */
	protected LanguageManager mLanguageManager = null;
	
	
	/** Reference to the network manager. */
	protected NetworkManager mNetworkManager = null;
	
	
	/** TODO */
	protected NotificationManager mNotificationManager = null;
	
	
	/** The selected method network. */
	protected int mNetworkMethod = NETWORK_METHOD_UNKNOWN;
	
	
	/** Reference to the wakelock system. */
	protected WakeLock mWakeLock = null;
	
	
	/** Service notification id. */
	protected static final int mNotificationId = 123456;
	
	
	@Override
	public void onCreate(){
		
		// Super
		super.onCreate();
		
		// Obtain wakelock
		acquireWakeLock();
		
		// Retrieve managers
		mLanguageManager = new LanguageManager();
		mActionManager = new ActionManager(mLanguageManager.getCurrentLocale(), this);
		mNetworkManager = new NetworkManager(this);
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		// Show notification
		showNotification();
	}
	

	@Override
	public void onDestroy(){
		
		// Log
		Log.i(mTag, "Service created.");
		
		// Dismiss notification
        mNotificationManager.cancel(mNotificationId);

		// Restore network
		restoreNetwork();
		
		// Release wakelock
		releaseWakeLock();
		
		// Super
		super.onDestroy();
		
	}

	
	/**
	 * TODO
	 */
	protected void showNotification(){
		
		// In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.notification_description);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon_info, text, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, HomeActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.notification_title), text, contentIntent);

        // Send the notification.
        mNotificationManager.notify(mNotificationId, notification);
        
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
		
		// Construct message
		Bundle messageBundle = new Bundle();
		messageBundle.putInt("StatusMobile", mobileStatus);
		messageBundle.putInt("StatusWifi", wifiStatus);
        Message message = Message.obtain(null, MSG_NETWORK_STATUS_UPDATED);
        message.setData(messageBundle);
        
        // Send it to all clients
        sendMessageToClients(message);
		
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
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-29
	 */
	protected class DroneConnectionTask extends AsyncTask<byte[], Integer, Boolean>{

		
		@Override
		protected Boolean doInBackground(byte[]... ips){
			
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
	
	
	@Override
	protected void onMessageFromClient(Message message){
		switch(message.what){
			
			case MSG_HIPRI_START:
				Log.d(mTag, "Handling message : start debug");
				prepareNetwork();
				break;
			
			case MSG_HIPRI_STOP:
				Log.d(mTag, "Handling message : stop debug");
				restoreNetwork();
				break;
			
			case MSG_SPEECH_START:
				Log.d(mTag, "Handling message : start speech");
		        try{
			        Message message2 = Message.obtain(null, SpeechRecognitionService.MSG_START_LISTENING);
		        	mMessengerToSpeechService.send(message2);
		        }catch(RemoteException e){
		        	e.printStackTrace();
		        }
				break;
			
			case MSG_SPEECH_STOP:
				Log.d("OstisService", "Handling message : stop speech");
		        try{
			        Message message2 = Message.obtain(null, SpeechRecognitionService.MSG_CANCEL_LISTENING);
		        	mMessengerToSpeechService.send(message2);
		        }catch(RemoteException e){
		        	e.printStackTrace();
		        }
				break;
			
			// Connect to drone
			case MSG_DRONE_CONNECT:
				doDroneConnect();
				break;
				
			// Disconnect from drone
			case MSG_DRONE_DISCONNECT:
				doDroneDisconnect();
				break;

		}
	}


	@Override
	protected void onMessageFromSpeechRecognizer(Message message){
		
		// React according to the type of object of the message
		switch(message.what){

			// Received speech recognition results
            case SpeechRecognitionService.MSG_FINISHED_WITH_RESULTS:
            	
            	// Log
                Log.i(mTag, "Received speech recognition results.");
                
                // Pass it to the action manager
                ArrayList<String> _Sentences = message.getData().getStringArrayList("SpeechRecognitionResult");
                if(_Sentences != null){
	    			for (int i = 0; i < _Sentences.size(); i++){
	    				Action actionMatched = mActionManager.matchCommandToRun(_Sentences.get(i));
	    				
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
	
	
	/**
	 * TODO
	 */
	protected void onDroneConnected(){
		
		// Log
		Log.i(mTag, "Connected to drone.");
		
		// TODO Update local variable
		
		// TODO Warn clients
		//sendMessageToClients(message);
		
	}
	
	
	/**
	 * TODO
	 */
	protected void onDroneConnectionFailed(){
		
		// Log
		Log.w(mTag, "Connection to drone failed.");
		
		// TODO Update local variable
		
		// TODO Warn clients
		//sendMessageToClients(message);
		
	}
}
