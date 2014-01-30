package fr.ece.ostis;

import java.net.InetAddress;
import java.util.ArrayList;
import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

import fr.ece.ostis.actions.Action;
import fr.ece.ostis.actions.ActionManager;
import fr.ece.ostis.lang.LanguageManager;
import fr.ece.ostis.network.MobileNetworkManager;
import fr.ece.ostis.network.WifiAPNetworkManager;
import fr.ece.ostis.network.WifiNetworkManager;
import fr.ece.ostis.speech.SpeechRecognitionResultsListener;
import fr.ece.ostis.ui.HomeActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-30
 */
public class OstisService extends Service implements SpeechRecognitionResultsListener, NavDataListener{
	
	
	/*
	 * Constants
	 */
	public static final int DRONE_STATUS_UNKNOWN = -1;			// Drone status
	public static final int DRONE_STATUS_CONNECTING = 0;		// Drone status
	public static final int DRONE_STATUS_DISCONNECTED = 1;		// Drone status
	public static final int DRONE_STATUS_CONNECTED = 2;			// Drone status
	public static final int NETWORK_METHOD_UNKNOWN = 0;			// Drone status
	public static final int NETWORK_METHOD_AP = 1;				// Drone status
	public static final int NETWORK_METHOD_HIPRI = 2;			// Drone status
	
	
    /** Log tag. */
    protected static final String mTag = "OstisService";
	
    
	/** Service binder. */
    protected final IBinder mBinder = new OstisServiceBinder();
    
    
    /** Service clients. */
    protected ArrayList<OnDroneStatusChangedListener> mDroneStatusChangedListeners = new ArrayList<OnDroneStatusChangedListener>();
    
    
	/** Reference to the javadrone api */
	protected static ARDrone mDrone = null;
	
	
	/** Drone connected state */
	protected int mDroneConnectionStatus = DRONE_STATUS_UNKNOWN; 
	
	
	/** Reference to the language manager. */
	protected LanguageManager mLanguageManager = null;
	
	
	/** Reference to the action manager. */
	protected ActionManager mActionManager = null;
	
	
	/* Reference to the networks manager. */
	protected MobileNetworkManager mMobileNetworkManager = null;
	protected WifiNetworkManager mWifiNetworkManager = null;
	protected WifiAPNetworkManager mWifiAPNetworkManager = null;
	
	
	/** Reference to the notification manager. */
	protected NotificationManager mNotificationManager = null;
	
	
	/** Flag for the notification shown or not. */
	protected boolean mNotificationShown = false;
	

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
		
		// Instantiate managers
		mLanguageManager = new LanguageManager();
		mActionManager = new ActionManager(this, mLanguageManager.getCurrentLocale());
		mMobileNetworkManager = new MobileNetworkManager(this.getApplicationContext());
		mWifiNetworkManager = new WifiNetworkManager(this);
		mWifiAPNetworkManager = new WifiAPNetworkManager(this, mWifiNetworkManager);
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
	}
	

	@Override
	public void onDestroy(){
		
		// Log
		Log.i(mTag, "Service created.");
		
		// Dismiss notification if displayed
		if(mNotificationShown) hideNotification();
		
		// Restore network
		/*restoreNetwork();*/
		
		// Release wakelock if held
		if(mWakeLock.isHeld()) releaseWakeLock();
		
		// Super
		super.onDestroy();
		
	}


	@Override
	public IBinder onBind(Intent intent){
		return mBinder;
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
        
        // Set flag
        mNotificationShown = true;
        
	}
	
	
	/**
	 * TODO
	 */
	protected void hideNotification(){
		
		// Dismiss notification
        mNotificationManager.cancel(mNotificationId);
        
        // Set flag
        mNotificationShown = false;
		
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
		/*new PrepareNetworkTask().execute(
				"8.8.8.8",
				"google.com",
				"m.google.com",
				"web.google.com",
				"dl-ssl.google.com",
				"dl-ssl.l.google.com",
				"mobile.google.com",
				"mobile.l.google.com");*/
		
	}

	
	/*protected class PrepareNetworkTask extends AsyncTask<String, Void, Void>{
	
		private Exception mException = null;
		
		protected Void doInBackground(String... hosts){
			
			// Step 1 : Connect to mobile network
			try {
				Log.d("OstisService", "prepareNetwork -> disabling wifi");
				mWifiNetworkManager.disableWifi();
				Log.d("OstisService", "prepareNetwork -> wifi disabled");
				OstisService.this.publishNetworkStatus(NetworkManager.STATUS_DISCONNECTED, NetworkManager.STATUS_DISCONNECTED);
				Log.d("OstisService", "prepareNetwork -> enabling mobile");
				mMobileNetworkManager.enableMobileConnection();
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
		
	}*/
	
	
	/*/**
	 * 
	 *
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
		
	}*/
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-30
	 */
	protected class DroneConnectionTask extends AsyncTask<byte[], Integer, Boolean>{

		
		/** Log tag */
		protected final static String mTag = "DroneConnectionTask";
		
		
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
				Log.w(mTag, "Failed to connect to drone.", e);
				
				try{
					
					OstisService.mDrone.clearEmergencySignal();
					OstisService.mDrone.clearImageListeners();
					OstisService.mDrone.clearNavDataListeners();
					OstisService.mDrone.clearStatusChangeListeners();
					OstisService.mDrone.disconnect();
					
				}catch(Exception e2){
					Log.w(mTag, "Failed to clear drone state after connection failed.", e2);
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
	public void doDroneConnect(){
		Log.i(mTag, "Connecting to drone.");
		(new DroneConnectionTask()).execute(new byte[]{(byte) 192, (byte) 168, (byte) 1, (byte) 1});
		mDroneConnectionStatus = DRONE_STATUS_CONNECTING;
	}	
	
	
	/**
	 * TODO
	 */
	public void doDroneDisconnect(){
		
		// Log
		Log.i(mTag, "Disconnecting from drone.");
		
		if(mDrone == null) return;
		try{
			mDrone.clearEmergencySignal();
			mDrone.clearImageListeners();
			mDrone.clearNavDataListeners();
			mDrone.clearStatusChangeListeners();
		}catch(Exception e){
			Log.w(mTag, "Failed to clear drone state.", e);
		}
		try{
			mDrone.disconnect();
		}catch(Exception e){
			Log.w("OstisService", "Failed to disconnect drone.", e);
		}
		
		mDroneConnectionStatus = DRONE_STATUS_DISCONNECTED;
		Log.i(mTag, "Disconnected from drone.");
		
		// Warn clients
		for(OnDroneStatusChangedListener callback: mDroneStatusChangedListeners){
			if(callback != null) callback.onDroneDisconnected();
			else Log.w(mTag, "Drone status changed listener is null.");
		}
	}
	
	
	/**
	 * TODO
	 */
	protected void onDroneConnected(){
		
		// Log
		Log.i(mTag, "Connected to drone.");
		
		// Update local variable
		mDroneConnectionStatus = DRONE_STATUS_CONNECTED;
		
		// Warn clients
		for(OnDroneStatusChangedListener callback: mDroneStatusChangedListeners){
			if(callback != null) callback.onDroneConnected();
			else Log.w(mTag, "Drone status changed listener is null.");
		}
		
	}
	
	
	/**
	 * TODO
	 */
	protected void onDroneConnectionFailed(){
		
		// Log
		Log.w(mTag, "Connection to drone failed.");
		
		// Update local variable
		mDroneConnectionStatus = DRONE_STATUS_DISCONNECTED;
		
		// Warn clients
		for(OnDroneStatusChangedListener callback: mDroneStatusChangedListeners){
			if(callback != null) callback.onDroneConnectionFailed();
			else Log.w(mTag, "Drone status changed listener is null.");
		}
		
	}
	
	
	/**
	 * TODO
	 * @param listener
	 */
	public void registerDroneStatusChangedListener(OnDroneStatusChangedListener listener){
		mDroneStatusChangedListeners.add(listener);
	}
	
	
	/**
	 * TODO
	 * @param listener
	 */
	public void unregisterDroneStatusChangedListener(OnDroneStatusChangedListener listener){
		mDroneStatusChangedListeners.remove(listener);
	}


	@Override
	public void onSpeechRecognitionResultsAvailable(ArrayList<String> sentences) {

    	// Log
        Log.i(mTag, "Received speech recognition results.");
        
        // Pass it to the action manager
        if(sentences != null){
			for (int i = 0; i < sentences.size(); i++){
				Action actionMatched = mActionManager.matchCommandToRun(sentences.get(i));
				if(actionMatched != null){
					Log.d("OstisService", "Running action " + actionMatched.getId());
					try{
						actionMatched.run(mDrone);
					}catch(Exception e){
						Log.e(mTag, "Could not run matched action", e);
						// TODO Auto-generated catch block
					}
					return;
				}
			}
        }
		
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public WifiNetworkManager getWifiNetworkManager(){
		return mWifiNetworkManager;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public int getDroneStatus(){
		return mDroneConnectionStatus;
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-30
	 */
    public class OstisServiceBinder extends Binder{
    	public OstisService getService(){
            return OstisService.this;
        }
    }


    /**
     * TODO
     * @param method
     * @throws Exception
     */
    public void setNetworkMethod(int method) throws Exception{
    	if(mNetworkMethod != NETWORK_METHOD_UNKNOWN) throw new Exception("Network method already set.");
    	mNetworkMethod = method;
    }
    
    
	@Override
	public void navDataReceived(NavData nd){
		
		// Hide / Display service notification according to is flying
		if(!mNotificationShown && nd.isFlying()) showNotification();
		else if(mNotificationShown) hideNotification();
		
		// Obtain / Release wakelock according to is flying
		if(!mWakeLock.isHeld() && nd.isFlying()) acquireWakeLock();
		else if(!nd.isFlying() && !mWakeLock.isHeld()) releaseWakeLock();
		
	}
    
}
