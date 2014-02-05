package fr.ece.ostis;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.ARDrone.State;
import com.codeminders.ardrone.DroneVideoListener;
import com.codeminders.ardrone.NavData;
import com.codeminders.ardrone.NavDataListener;

import fr.ece.ostis.actions.Action;
import fr.ece.ostis.actions.ActionManager;
import fr.ece.ostis.lang.LanguageManager;
import fr.ece.ostis.network.MobileNetworkManager;
import fr.ece.ostis.network.WifiAPNetworkManager;
import fr.ece.ostis.network.WifiNetworkManager;
import fr.ece.ostis.speech.SpeechRecognitionManager;
import fr.ece.ostis.speech.SpeechRecognitionResultsListener;
import fr.ece.ostis.ui.HomeActivity;
import fr.ece.ostis.utils.TelnetManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-03
 */
public class OstisService extends Service implements SpeechRecognitionResultsListener, NavDataListener, DroneVideoListener{
	
	
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
    
    
    /* Service clients. */
    protected ArrayList<DroneStatusChangedListener> mDroneStatusChangedListeners = new ArrayList<DroneStatusChangedListener>();
    protected ArrayList<DroneFrameReceivedListener> mDroneFrameReceivedListeners = new ArrayList<DroneFrameReceivedListener>();
    
    
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
	
	
	/** Reference to the speech recognition manager. */
	protected SpeechRecognitionManager mSpeechRecognitionManager = null;
	
	
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
	
	
	/** TODO */
	public static final String mAccessPointName = "OstisAP";
	
	
	/** TODO */
	public static final int mAccessPointChannel = 9;
	
	
	/** Flag for following mode activated or not. */
	protected AtomicBoolean mFollowingActivated = new AtomicBoolean(false); 
	
	
	/** Stores the last navdata. */
	protected NavData mLastNavData = null;
	
	
	/** Monitoring thread. */
	protected Thread mThread = null;
	
	
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
		mSpeechRecognitionManager = new SpeechRecognitionManager(this);
		
		// Perform network cleanup just in case
		try{
			mWifiAPNetworkManager.disableWifiApSynchronous();
		}catch(Exception e){
			Log.w(mTag, "Failed to cleanup access point at service startup.", e);
		}
		
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
	protected class DroneConnectionTask extends AsyncTask<String, Integer, Boolean>{

		
		/** Log tag */
		protected final static String mTag = "DroneConnectionTask";
		
		
		@Override
		protected Boolean doInBackground(String... ips){
			
			String droneIp = ips[0];
			
			try{
				
				OstisService.mDrone = new ARDrone(InetAddress.getByName(droneIp), 10000, 60000);
				OstisService.mDrone.connect();
				OstisService.mDrone.clearEmergencySignal();
				OstisService.mDrone.trim();
				OstisService.mDrone.waitForReady(10000);
				OstisService.mDrone.playLED(1, 10, 4);
				OstisService.mDrone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
				OstisService.mDrone.setCombinedYawMode(true);
				return true;
				
			}catch(Exception e){
				Log.e(mTag, "Failed to connect to drone.", e);
				
				try{
					
					OstisService.mDrone.clearImageListeners();
					OstisService.mDrone.clearNavDataListeners();
					OstisService.mDrone.clearStatusChangeListeners();
					OstisService.mDrone.clearEmergencySignal();
					OstisService.mDrone.disconnect();
					
				}catch(Exception e2){
					Log.w(mTag, "Failed to clear drone state after connection failed.", e2);
				}
	
			}
			
			return false;
		}

		protected void onPostExecute(final Boolean success){
			/*Handler mainHandler = new Handler(OstisService.this.getMainLooper());
			Runnable myRunnable = new Runnable(){
				@Override
				public void run(){*/
					if(success.booleanValue()){
						onDroneConnected();
					}else{
						onDroneConnectionFailed();
					}
				/*}
			};
			mainHandler.post(myRunnable);*/
		}
	}
	
	
	/**
	 * TODO
	 * @param ip
	 * @throws Exception 
	 * @deprecated Should may be replaced by async connection
	 */
	public void doDroneConnectSynchronous(String ip) throws Exception{
		
		mDroneConnectionStatus = DRONE_STATUS_CONNECTING;
		
		try{
			mDrone = new ARDrone(InetAddress.getByName(ip), 10000, 60000);
			mDrone.connect();
			mDrone.clearEmergencySignal();
			mDrone.trim();
			mDrone.waitForReady(10000);
			mDrone.playLED(1, 10, 4);
			mDrone.selectVideoChannel(ARDrone.VideoChannel.HORIZONTAL_ONLY);
			mDrone.setCombinedYawMode(true);
			
			mDroneConnectionStatus = DRONE_STATUS_CONNECTED;
			
			onDroneConnected();
		}catch(Exception e){
			Log.e(mTag, "Failed to connect to drone.", e);
			mDroneConnectionStatus = DRONE_STATUS_DISCONNECTED;
			
			try{
				mDrone.clearNavDataListeners();
				mDrone.clearStatusChangeListeners();
				mDrone.clearImageListeners();
				mDrone.clearEmergencySignal();
				mDrone.disconnect();
			}catch(Exception e2){
				Log.w(mTag, "Failed to clear drone state after connection failed.", e2);
			}
			
			onDroneConnectionFailed();

			throw new Exception("Connection to drone failed.");
		}
		
	}
	
	
	/**
	 * TODO
	 * @deprecated Task should be executed from outside the service.
	 */
	public void doDroneConnectAsynchronous(String ip){
		Log.i(mTag, "Connecting to drone.");
		(new DroneConnectionTask()).execute(ip);
		mDroneConnectionStatus = DRONE_STATUS_CONNECTING;
	}	
	
	
	/**
	 * TODO
	 */
	public void doDroneDisconnect(){
		
		// Log
		Log.i(mTag, "Disconnecting from drone.");
		
		// Prevent null pointer
		if(mDrone == null) return;
		
		// Unregister callbacks
		mDrone.removeImageListener(this);
		mDrone.removeNavDataListener(this);
		
		// Try to clear drone state
		try{
			mDrone.clearEmergencySignal();
			mDrone.clearImageListeners();
			mDrone.clearNavDataListeners();
			mDrone.clearStatusChangeListeners();
		}catch(Exception e){
			Log.w(mTag, "Failed to clear drone state.", e);
		}
		
		// Try to disconnect
		try{
			mDrone.disconnect();
		}catch(Exception e){
			Log.w("OstisService", "Failed to disconnect drone.", e);
		}
		
		// Reset flag
		mDroneConnectionStatus = DRONE_STATUS_DISCONNECTED;
		
		// Log
		Log.i(mTag, "Disconnected from drone.");
		
		// Warn clients
		for(DroneStatusChangedListener callback: mDroneStatusChangedListeners){
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
		
		// Register callbacks
		mDrone.addImageListener(this);
		mDrone.addNavDataListener(this);
		
		// Warn clients
		for(DroneStatusChangedListener callback: mDroneStatusChangedListeners){
			if(callback != null) callback.onDroneConnected();
			else Log.w(mTag, "Drone status changed listener is null.");
		}
		
		// Start pinger thread
		mThread = new Thread(new Runnable(){
			
			protected boolean mPreviouslyOk = false;
			
			@Override
			public void run(){
				while(true){
					if(mDrone != null){
						Log.d(mTag, "Pinger thread reports drone status = " + mDrone.getState());
						/*if(mPreviouslyOk && mDrone.getState() == State.DISCONNECTED || mDrone.getState() == State.ERROR){
							Log.d(mTag, "Pinger thread reports drone disconnected or error.");
							onDroneDisconnected();
							return;
						}else if(mDrone.getState() == State.*/
					}else{
						Log.d(mTag, "Pinger thread reports drone is null");
					}
					try {
						Thread.sleep(1000);
					}catch (InterruptedException e){
						e.printStackTrace();
						return;
					}
				}
			}
		});
		mThread.start();
		
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
		for(DroneStatusChangedListener callback: mDroneStatusChangedListeners){
			if(callback != null) callback.onDroneConnectionFailed();
			else Log.w(mTag, "Drone status changed listener is null.");
		}
		
	}
	
	
	/**
	 * TODO
	 */
	protected void onDroneDisconnected(){
		
		// Log
		Log.w(mTag, "Disconnected from drone.");
		
		// Update local variable
		mDroneConnectionStatus = DRONE_STATUS_DISCONNECTED;
		
		// Warn clients
		for(DroneStatusChangedListener callback: mDroneStatusChangedListeners){
			if(callback != null) callback.onDroneDisconnected();
			else Log.w(mTag, "Drone status changed listener is null.");
		}
		
	}
	
	
	/**
	 * TODO
	 * @throws Exception 
	 */
	public void pushDroneApConfiguration(String ip) throws Exception{
		
		// TODO Ensure that wifi or wifiap is activated and connected
		// ...
		
		// TODO Retrieve or set ap ip config
		// ...
		
		// Push conig to the drone
		if(TelnetManager.executeRemotely(ip, 23,
				"iwconfig ath0 mode managed essid " + mAccessPointName + "\n" +
				"ifconfig ath0 192.168.1.11 netmask 255.255.255.0 up" + "\n" +
				"route add default gw 192.168.1.1" + "\n") != true){
			throw new Exception("Unable to push access point configuration to drone.");
		}
		
	}
	
	
	/**
	 * TODO
	 * @param listener
	 */
	public void registerFrameReceivedListener(DroneFrameReceivedListener listener){
		mDroneFrameReceivedListeners.add(listener);
	}
	
	
	/**
	 * TODO
	 * @param listener
	 */
	public void unregisterFrameReceivedListener(DroneFrameReceivedListener listener){
		mDroneFrameReceivedListeners.remove(listener);
	}
	
	
	/**
	 * TODO
	 * @param listener
	 */
	public void registerStatusChangedListener(DroneStatusChangedListener listener){
		mDroneStatusChangedListeners.add(listener);
	}
	
	
	/**
	 * TODO
	 * @param listener
	 */
	public void unregisterStatusChangedListener(DroneStatusChangedListener listener){
		mDroneStatusChangedListeners.remove(listener);
	}

	
	/**
	 * TODO
	 */
	public void activateSpeechResultsToActionMatching(){
		mSpeechRecognitionManager.registerCallback(this);
	}
	
	
	/**
	 * TODO
	 */
	public void desactivateSpeechResultsToActionMatching(){
		mSpeechRecognitionManager.unregisterCallback(this);
	}

	
	/**
	 * TODO
	 */
	public void startSpeechRecognition(){
		mSpeechRecognitionManager.startListening();
	}
	
	
	/**
	 * TODO
	 */
	public void stopSpeechRecognition(){
		mSpeechRecognitionManager.cancelListening();
	}

	
	@Override
	public void onSpeechRecognitionResultsAvailable(ArrayList<String> sentences){

    	// Log
        Log.i(mTag, "Received speech recognition results.");
        
        // Pass it to the action manager
        if(sentences != null){
			for (int i = 0; i < sentences.size(); i++){
				Action actionMatched = mActionManager.matchCommandToRun(sentences.get(i));
				if(actionMatched != null){
					Log.d("OstisService", "Running action " + actionMatched.getId());
					try{
						actionMatched.run(mDrone, this);
					}catch(Exception e){
						Log.e(mTag, "Could not run matched action", e);
					}finally{
						Log.d(mTag, "Drone state is " + mDrone.getState());
						if(mDrone.getState() == State.DISCONNECTED || mDrone.getState() == State.ERROR){
							doDroneDisconnect();
						}
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
	public SpeechRecognitionManager getSpeechRecognitionManager(){
		return mSpeechRecognitionManager;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public MobileNetworkManager getMobileNetworkManager(){
		return mMobileNetworkManager;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public WifiNetworkManager getWifiNetworkManager(){
		return mWifiNetworkManager;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public WifiAPNetworkManager getWifiAPNetworkManager(){
		return mWifiAPNetworkManager;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public ARDrone getDrone(){
		return mDrone;
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
     */
    public void setNetworkMethod(int method){
    	mNetworkMethod = method;
    }


    /**
     * TODO
     * @return
     */
    public int getNetworkMethod(){
    	return mNetworkMethod;
    }
    

    /**
     * TODO
     * @return
     */
    public boolean getFollowingActivated(){
    	return mFollowingActivated.get();
    }
    
    
    /**
     * TODO
     * @param value
     */
    public void setFollowingActivated(boolean value){
    	mFollowingActivated.set(value);
    }
    
    
	@Override
	public void navDataReceived(NavData nd){
		
		// Hide / Display service notification according to is flying
		if(!mNotificationShown && nd.isFlying()) showNotification();
		else if(mNotificationShown) hideNotification();
		
		// Obtain / Release wakelock according to is flying
		if((mWakeLock == null || !mWakeLock.isHeld()) && nd.isFlying()) acquireWakeLock();
		else if(!nd.isFlying() && (mWakeLock != null && mWakeLock.isHeld())) releaseWakeLock();
		
		// TODO Monitor the battery
		// ...
		
		// Store last data
		mLastNavData = nd;
	}


	@Override
	public void onReadyForSpeech(){}


	@Override
	public void onError(){}


	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize){
		(new CameraFrameReceiver(startX, startY, w, h, rgbArray, offset, scansize)).execute();
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-02-05
	 */
	protected class CameraFrameReceiver extends AsyncTask<Void, Integer, Void>{
		
		public Bitmap b;
		public int[]rgbArray;
		public int offset;
		public int scansize;
		public int w;
		public int h;

		
		public CameraFrameReceiver(int x, int y, int width, int height, int[] arr, int off, int scan){
			super();
			rgbArray = arr;
			offset = off;
			scansize = scan;
			w = width;
			h = height;
		}
		
		
		@Override
		protected Void doInBackground(Void... params){
			b = Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
			b.setDensity(100);
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void param){			
			// Warn clients
			for(DroneFrameReceivedListener callback: mDroneFrameReceivedListeners){
				if(callback != null) callback.onDroneFrameReceived(b);
				else Log.w(mTag, "Drone frame received listener is null.");
			}
		}
		
	}
    
}
