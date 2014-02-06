package fr.ece.ostis.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import fr.ece.ostis.DroneStatusChangedListener;
import fr.ece.ostis.OstisService;
import fr.ece.ostis.R;
import fr.ece.ostis.network.MobileNetworkManager;
import fr.ece.ostis.network.OnWifiScanResultsUpdatedListener;
import fr.ece.ostis.network.WifiAPNetworkManager;
import fr.ece.ostis.network.WifiNetworkManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public class NetworkWizardActivity extends ConnectedActivity implements OnWifiScanResultsUpdatedListener, DroneStatusChangedListener{

	
	/** Stores the current step of the wizard. */
	protected int mCurrentStep = -1;
	
	
	/* Controls */
	protected ProgressBar mProgressBarScan = null;
	protected ListView mListViewNetworks = null;
	protected ArrayList<HashMap<String, Object>> mWifiNetworks = null;
	protected SimpleAdapter mListViewNetworksAdapter = null;
	protected ProgressDialog mDialog = null;
	protected Button mButtonPrev = null;
	protected Button mButtonNext = null;

	
	/** Count down timer for wifi networks refreshing. */
	protected CountDownTimer mWifiScanCountDown = new CountDownTimer(5000, 5000){

		@Override public void onTick(long millisUntilFinished){ }

		@Override public void onFinish(){
			if(mCurrentStep == 2 && mBound){
				(new StartWifiScanTask()).execute();
				mWifiScanCountDown.start();
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Setup step 1
		setupStep1();
		
	}

	
	@Override
	public void onBackPressed(){
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);   
	}
	
	
	/**
	 * TODO
	 */
	protected void setupStep1(){
		
		// Update step value
		mCurrentStep = 1;
		
		// Set layout
		setContentView(R.layout.acitivity_network_step1);
		
		// Find controls
		final RadioButton buttonMethod1 = (RadioButton) findViewById(R.id.radioButtonMethod1);
		final RadioButton buttonMethod2 = (RadioButton) findViewById(R.id.radioButtonMethod2);
		mButtonNext = (Button) findViewById(R.id.buttonNext);
		
		// Set on click listeners
		mButtonNext.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				onNetworkMethodSelected(buttonMethod1.isChecked()?1:2);
				setupStep2();
			}
		});
		
		buttonMethod1.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				buttonMethod2.setChecked(!buttonMethod1.isChecked());
			}
		});
		
		buttonMethod2.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
				buttonMethod1.setChecked(!buttonMethod2.isChecked());
			}
		});
		
	}
	
	
	/**
	 * TODO
	 */
	protected void setupStep2(){
		
		// Update step value
		mCurrentStep = 2;
		
		// Set layout
		setContentView(R.layout.activity_network_step2);
		
		// Find controls
		mListViewNetworks = (ListView) findViewById(R.id.listViewNetworks);
		mProgressBarScan = (ProgressBar) findViewById(R.id.progressBarScan);
		mButtonPrev = (Button) findViewById(R.id.buttonPrev);
		//mButtonNext = (Button) findViewById(R.id.buttonNext);
		
		// Set on click listeners
		mButtonPrev.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				setupStep1();
			}
		});
		
		// Setup list view
		mWifiNetworks = new ArrayList<HashMap<String, Object>>();
		mListViewNetworksAdapter = new SimpleAdapter(this, mWifiNetworks, R.layout.list_wifi_network, new String[] { WifiNetworkManager.KEY_NETWORK_SSID }, new int[] { R.id.textViewName });
		mListViewNetworks.setAdapter(mListViewNetworksAdapter);
		mListViewNetworks.setOnItemClickListener(new OnItemClickListener(){
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				onWifiNetworkSelected((String) mWifiNetworks.get(position).get(WifiNetworkManager.KEY_NETWORK_SSID));
			}
		});
		

		// Disable wifi ap
		try{mService.getWifiAPNetworkManager().disableWifiApSynchronous();}
		catch(Exception e){}
		
		// Ask for wifi scan
		(new StartWifiScanTask()).execute();
		mWifiScanCountDown.start();
		
	}

	
	@Override
	protected void onBoundToOstisService(){
		
		// Register callbacks eventually ?
		
		// Ask for wifi scan
		if(mCurrentStep == 2){
			(new StartWifiScanTask()).execute();
			mWifiScanCountDown.start();
		}
		
	}


	@Override
	protected void onBeforeUnbindFromOstisService() {
		// TODO Auto-generated method stub
	}

	
	@Override
	protected void onUnboundFromOstisService(){
		// TODO Auto-generated method stub
	}

	
	/**
	 * TODO
	 * @param method
	 */
	protected void onNetworkMethodSelected(int method){
		
		// Warn service
		mService.setNetworkMethod(method);
		
	}
	
	protected class StartWifiScanTask extends AsyncTask<Void, Void, Void>{

		protected Exception mException = null;
		
		
		@Override
		protected Void doInBackground(Void... params){
			
			if(mBound != true) return null;
			
			try{
				WifiNetworkManager wifiNetworkManager = NetworkWizardActivity.this.mService.getWifiNetworkManager();
				
				if(!wifiNetworkManager.isWifiEnabled()){
					wifiNetworkManager.enableWifiAsynchronous();
					wifiNetworkManager.waitForWifiEnabled();
				}
			
				wifiNetworkManager.startWifiScan(NetworkWizardActivity.this);
			}catch(Exception e){
				Log.e(mTag, "Failed to start wifi scan.", e);
				mException = e;
			}
			
			return null;
		}
		
	}

	@Override
	public void onWifiScanResultsUpdated(List<ScanResult> wifiList){
		
		// Log
		Log.i(mTag, "Wifi scan results received.");
		
		// Update interface
		mProgressBarScan.setVisibility(View.GONE);
		mListViewNetworks.setVisibility(View.VISIBLE);
		
		// Update wifi list with unique values
		mWifiNetworks.clear();
		for(ScanResult result: wifiList){
			
			boolean isAlreadyPresent = false;
			for(HashMap<String, Object> alreadyPresent: mWifiNetworks){
				if(((String) alreadyPresent.get(WifiNetworkManager.KEY_NETWORK_SSID)).equals(result.SSID)){
					isAlreadyPresent = true;
				}
			}

			if(!isAlreadyPresent &&
					result.SSID.length() > 0 &&
					!result.capabilities.contains("PSK") &&
					!result.capabilities.contains("WEP") &&
					!result.capabilities.contains("EAP")){
				HashMap<String, Object> network = new HashMap<String, Object>();
				network.put(WifiNetworkManager.KEY_NETWORK_SSID, result.SSID);
				network.put(WifiNetworkManager.KEY_NETWORK_BSSID, result.BSSID);
				network.put(WifiNetworkManager.KEY_NETWORK_LEVEL, result.level);
	
				mWifiNetworks.add(network);
			}
		}
		mListViewNetworksAdapter.notifyDataSetChanged();
		
	}
	
	
	/**
	 * TODO
	 * @param ssid
	 */
	protected void onWifiNetworkSelected(String ssid){
		if(mService.getNetworkMethod() == OstisService.NETWORK_METHOD_HIPRI){
			(new MethodApTask()).execute(ssid);
		}else if(mService.getNetworkMethod() == OstisService.NETWORK_METHOD_AP){
			(new MethodHipriTask()).execute(ssid);
		}else{
			Log.e(mTag, "Network method not expected.");
			setupStep1();
		}
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-31
	 */
	protected class MethodHipriTask extends AsyncTask<String, Bundle, Void>{

		protected Exception mException = null;
		protected static final String KEY_MESSAGE = "MESSAGE";
		protected static final String KEY_STEP = "STEP";
		protected static final int mSteps = 7;
		
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			mDialog = new ProgressDialog(NetworkWizardActivity.this);
			mDialog.setMessage("Please wait while we try to configure your drone ...");
			mDialog.setCancelable(false);
			mDialog.setMax(mSteps);
			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialog.show();
		}
		

		@Override
		protected Void doInBackground(String... ssids){
			
			// Initiate variables
			String targetedSsid = ssids[0];
			Bundle progressBundle = null;
			int retryCounter = 0;
			long retryTimeCounter = 0;
			
			// Retrieve network managers
			MobileNetworkManager mobileNetworkManager = mService.getMobileNetworkManager();
			WifiNetworkManager wifiNetworkManager = mService.getWifiNetworkManager();
			WifiAPNetworkManager wifiAPNetworkManager = mService.getWifiAPNetworkManager();
			
			try{

				// Ensure that we are bound to the service
				if(mBound != true) throw new Exception("Not bound to service.");
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 1);
				progressBundle.putString(KEY_MESSAGE, "Enabling mobile connection ...");
				publishProgress(progressBundle);
				
				// Stop wifi, wifi ap and connect to 3G
				wifiAPNetworkManager.disableWifiApSynchronous(); // Throws exception
				wifiNetworkManager.disableWifiAsynchronous();
				wifiNetworkManager.waitForWifiDisabled(); // Throws exception
				mobileNetworkManager.enableMobileConnectionSynchronous();
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 2);
				progressBundle.putString(KEY_MESSAGE, "Mobile network enabled, starting speech recognition ...");
				publishProgress(progressBundle);
				
				// Start voice recognition
				mService.startSpeechRecognition();
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 3);
				progressBundle.putString(KEY_MESSAGE, "Speech recognition started, starting hipri ...");
				publishProgress(progressBundle);
				
				// Start 3G Hipri
				mobileNetworkManager.startHipri(
					"8.8.8.8",
					"google.com",
					"m.google.com",
					"web.google.com",
					"dl-ssl.google.com",
					"dl-ssl.l.google.com",
					"mobile.google.com",
					"mobile.l.google.com"); // Throws exception
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 4);
				progressBundle.putString(KEY_MESSAGE, "Hipri started, starting wifi ...");
				publishProgress(progressBundle);
				
				// Start wifi
				wifiNetworkManager.enableWifiAsynchronous();
				wifiNetworkManager.waitForWifiEnabled(); // Throws exception
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 5);
				progressBundle.putString(KEY_MESSAGE, "Wifi started, connecting to drone access point ...");
				publishProgress(progressBundle);
				
				// Connect to drone wifi
				wifiNetworkManager.connectToOpenNetwork(targetedSsid);
				retryTimeCounter = System.currentTimeMillis();
				while(true){
					try{
						Thread.sleep(1000);
						String currentSsid = wifiNetworkManager.getCurrentSsid();
						if(currentSsid.equals(targetedSsid)) break;
					}catch(Exception e2){
						Log.w(mTag, "Unable to retrieve current ssid.", e2);
					}
					
					// Abort if more than 20 seconds elapsed
					if(System.currentTimeMillis() - retryTimeCounter >= 20000)
						throw new TimeoutException("Unable to connect to network " + targetedSsid + ".");
					
				}
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 6);
				progressBundle.putString(KEY_MESSAGE, "Connected to drone access point, connecting to drone ...");
				publishProgress(progressBundle);
				
				// Connect to drone
				retryTimeCounter = System.currentTimeMillis();
				while(true){
					try{
						Thread.sleep(1000);
						Log.d(mTag, "Connecting to drone ...");
						mService.doDroneConnectSynchronous("192.168.1.1");
						Log.d(mTag, "Connected to drone, breaking retry loop ...");
						break;
					}catch(Exception e2){
						Log.w(mTag, "Connection to drone failed.", e2);
					}

					// Abort if more than 20 seconds elapsed
					if(System.currentTimeMillis() - retryTimeCounter >= 20000)
						throw new TimeoutException("Waiting for drone timed out.");
					
				}
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 7);
				progressBundle.putString(KEY_MESSAGE, "Connected to drone access point ...");
				publishProgress(progressBundle);
				
				
			}catch(Exception e){
				Log.e(mTag, "Failed to configure and connect to drone.", e);
				try{ wifiAPNetworkManager.disableWifiApSynchronous(); }
				catch (TimeoutException e2) {}
				mException = e;
			}
			
			return null;
		}

		
		@Override
		protected void onProgressUpdate(Bundle... progresses){
			mDialog.setProgress(progresses[0].getInt(KEY_STEP));
			mDialog.setMessage(progresses[0].getString(KEY_MESSAGE));
		}
		
		
		@Override
		protected void onPostExecute(Void result){
			
			// TODO Handle exception
			if(mException != null){
				// Show alert dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(NetworkWizardActivity.this);
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int id){
						// Do nothing
					}
				});
				builder.setMessage(mException.getMessage());
				AlertDialog dialog = builder.create();
				dialog.show();
			}else{
				Intent intent = new Intent(NetworkWizardActivity.this, FlyActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);  
			}
			
			// Dismiss dialog
			if(mDialog.isShowing()) mDialog.dismiss();
			
		}
		
	}
	
	
	protected class MethodApTask extends AsyncTask<String, Bundle, Void>{

		protected Exception mException = null;
		protected static final String KEY_MESSAGE = "MESSAGE";
		protected static final String KEY_STEP = "STEP";
		protected static final int mSteps = 6;
		
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			mDialog = new ProgressDialog(NetworkWizardActivity.this);
			mDialog.setMessage("Please wait while we try to configure your drone ...");
			mDialog.setCancelable(false);
			mDialog.setMax(mSteps);
			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialog.show();
		}
		
		
		@Override
		protected Void doInBackground(String... ssids){
			
			// Initiate variables
			String targetedSsid = ssids[0];
			Bundle progressBundle = null;
			int retryCounter = 0;
			long retryTimeCounter = 0;
			
			// Retrieve network managers
			WifiNetworkManager wifiNetworkManager = mService.getWifiNetworkManager();
			WifiAPNetworkManager wifiAPNetworkManager = mService.getWifiAPNetworkManager();
			
			try{

				// Ensure that we are bound to the service
				if(mBound != true) throw new Exception("Not bound to service.");
				
				// Connect to network
				wifiNetworkManager.connectToOpenNetwork(targetedSsid);
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 1);
				progressBundle.putString(KEY_MESSAGE, "Connecting to wifi network ...");
				publishProgress(progressBundle);
				
				// Wait until connected to wifi network or throw timeout exception
				retryCounter = 0;
				while(true){
					try{
						Thread.sleep(500);
						String currentSsid = wifiNetworkManager.getCurrentSsid();
						if(currentSsid.equals(targetedSsid)) break;
					}catch(Exception e2){
						Log.w(mTag, "Unable to retrieve current ssid.", e2);
					}
					retryCounter++;
					if(retryCounter >= 20) throw new TimeoutException("Unable to connect to network " + targetedSsid + ".");
				}
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 2);
				progressBundle.putString(KEY_MESSAGE, "Connected to wifi network. Connecting to drone ...");
				publishProgress(progressBundle);
				
				// Connect to drone or throw timeout exception
				retryCounter = 0;
				while(true){
					try{
						mService.doDroneConnectSynchronous("192.168.1.1");
						break;
					}catch(Exception e2){
						Log.w(mTag, "Connection to drone failed.", e2);
					}
					retryCounter++;
					if(retryCounter >= 3) throw new TimeoutException("Unable to connect to drone.");
				}
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 3);
				progressBundle.putString(KEY_MESSAGE, "Connected to drone. Pushing access point configuration to drone ...");
				publishProgress(progressBundle);
				
				// Push telnet configuration
				retryCounter = 0;
				while(true){
					try{
						mService.pushDroneApConfiguration("192.168.1.1");
						break;
					}catch(Exception e2){
						Log.w(mTag, "Unable to push configuration to drone.", e2);
					}
					retryCounter++;
					if(retryCounter >= 3) throw new TimeoutException("Unable to push access point configuration to drone.");
				}
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 4);
				progressBundle.putString(KEY_MESSAGE, "Configuration pushed. Setting up access point on the phone ...");
				publishProgress(progressBundle);
				
				// Stop wifi and start ap
				retryCounter = 0;
				while(true){
					try{
						wifiAPNetworkManager.enableWifiApSynchronous(mService.mAccessPointName, mService.mAccessPointChannel);
						break;
					}catch(Exception e2){
						Log.w(mTag, "Unable to setup access point.", e2);
					}
					retryCounter++;
					if(retryCounter >= 3) throw new TimeoutException("Unable setup access point.");
				}
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 5);
				progressBundle.putString(KEY_MESSAGE, "Access point set up, waiting for drone to connect ...");
				publishProgress(progressBundle);
				
				// Connect to drone or throw timeout exception
				retryTimeCounter = System.currentTimeMillis();
				while(true){
					try{
						Thread.sleep(1000);
						Log.d(mTag, "Connecting to drone ...");
						mService.doDroneConnectSynchronous("192.168.1.11"); // TODO Retrieve IP instead of hard-coded
						Log.d(mTag, "Connected to drone, breaking retry loop...");
						break;
					}catch(Exception e2){
						Log.w(mTag, "Connection to drone failed.", e2);
					}

					// Abort if more than 20 seconds elapsed
					if(System.currentTimeMillis() - retryTimeCounter >= 20000)
						throw new TimeoutException("Waiting for drone timed out.");
				}
				
				// Publish progress
				progressBundle = new Bundle();
				progressBundle.putInt(KEY_STEP, 6);
				progressBundle.putString(KEY_MESSAGE, "Connected to drone.");
				publishProgress(progressBundle);

			}catch(Exception e){
				Log.e(mTag, "Failed to configure and connect to drone.", e);
				try{ wifiAPNetworkManager.disableWifiApSynchronous(); }
				catch (TimeoutException e2) {}
				mException = e;
			}
			
			return null;
		}

		
		@Override
		protected void onProgressUpdate(Bundle... progresses){
			mDialog.setProgress(progresses[0].getInt(KEY_STEP));
			mDialog.setMessage(progresses[0].getString(KEY_MESSAGE));
		}
		
		
		@Override
		protected void onPostExecute(Void result){
			
			for(int i = 0; i < 10; i++) Log.d(mTag, "ON POST EXECUTE");
			
			// TODO Handle exception
			if(mException != null){
				// Show alert dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(NetworkWizardActivity.this);
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int id){
						// Do nothing
					}
				});
				builder.setMessage(mException.getMessage());
				AlertDialog dialog = builder.create();
				dialog.show();
			}else{
				Intent intent = new Intent(NetworkWizardActivity.this, FlyActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);  
			}
			
			// Dismiss dialog
			if(mDialog.isShowing()) mDialog.dismiss();
			
		}
		
	}

	
	@Override
	public void onDroneConnected(){
		// TODO Auto-generated method stub
	}


	@Override
	public void onDroneConnectionFailed(){
		// TODO Auto-generated method stub
	}


	@Override
	public void onDroneDisconnected(){
		// TODO Auto-generated method stub
	}
	
}
