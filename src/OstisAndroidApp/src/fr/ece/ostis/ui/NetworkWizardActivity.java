package fr.ece.ostis.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.ece.ostis.R;
import fr.ece.ostis.network.OnWifiScanResultsUpdatedListener;
import fr.ece.ostis.network.WifiNetworkManager;
import fr.ece.ostis.speech.SpeechRecognitionManager;
import android.app.ProgressDialog;
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

public class NetworkWizardActivity extends ConnectedActivity implements OnWifiScanResultsUpdatedListener{

	
	/** Stores the current step of the wizard. */
	protected int mCurrentStep = -1;
	
	
	/* Controls */
	protected ProgressBar mProgressBarScan = null;
	protected ListView mListViewNetworks = null;
	protected ArrayList<HashMap<String, Object>> mWifiNetworks = null;
	protected SimpleAdapter mListViewNetworksAdapter = null;
	protected ProgressDialog mDialog = null;
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
		mButtonNext = (Button) findViewById(R.id.buttonNext);
		
		// Set on click listeners
		mButtonNext.setOnClickListener(new OnClickListener(){
			@Override public void onClick(View v){
				setupStep3();
			}
		});
		
		// Setup list view
		mWifiNetworks = new ArrayList<HashMap<String, Object>>();
	    mListViewNetworksAdapter = new SimpleAdapter(this, mWifiNetworks, R.layout.activty_network_wifi, new String[] { WifiNetworkManager.KEY_NETWORK_SSID }, new int[] { R.id.textViewName });
	    mListViewNetworks.setAdapter(mListViewNetworksAdapter);
	    mListViewNetworks.setOnItemClickListener(new OnItemClickListener(){
    		@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				onWifiNetworkSelected((String) mWifiNetworks.get(position).get(WifiNetworkManager.KEY_NETWORK_SSID));
			}
		});
		
		// Ask for wifi scan
		(new StartWifiScanTask()).execute();
		mWifiScanCountDown.start();
		
	}

	
	/**
	 * TODO
	 */
	protected void setupStep3(){

		// Update step value
		mCurrentStep = 3;
		
		// Set layout
		setContentView(R.layout.activity_network_step3);
		
		// Find controls
		mButtonNext = (Button) findViewById(R.id.buttonNext);
		
	}
	
	@Override
	protected void onBoundToOstisService(){
		
		// Register callbacks
		
		
		// Ask for wifi scan
		if(mCurrentStep == 2){
			(new StartWifiScanTask()).execute();
			mWifiScanCountDown.start();
		}
		
	}

	
	@Override
	protected void onUnboundFromOstisService(){
		// TODO Auto-generated method stub
	}

	
	protected class StartWifiScanTask extends AsyncTask<Void, Void, Void>{

		protected Exception mException = null;
		
	    /*@Override
	    protected void onPreExecute(){
	        super.onPreExecute();
	        mProgressBarScan.setVisibility(View.VISIBLE);
	        mListViewNetworks.setVisibility(View.GONE);
	    }*/
		
	    
		@Override
		protected Void doInBackground(Void... params){
			
			if(mBound != true) return null;
			
			try{
				WifiNetworkManager wifiNetworkManager = NetworkWizardActivity.this.mService.getWifiNetworkManager();
				
				if(!wifiNetworkManager.isWifiEnabled()){
					wifiNetworkManager.enableWifi();
					wifiNetworkManager.waitForWifiEnabled();
				}
			
				wifiNetworkManager.startWifiScan(NetworkWizardActivity.this);
			}catch(Exception e){
				Log.e(mTag, "Failed to start wifi scan.", e);
				mException = e;
			}
			
			return null;
		}
		
		
		/*@Override
		protected void onPostExecute(Void result){
			if(mException != null){
				/* TODO Handle exception *
			}else{
				mProgressBarScan.setVisibility(View.GONE);
		        mListViewNetworks.setVisibility(View.VISIBLE);
			}
	    }*/
		
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
		(new ConfigureDroneTask()).execute(ssid);
	}
	
	
	protected class ConfigureDroneTask extends AsyncTask<String, Void, Void>{

		protected Exception mException = null;
		
	    @Override
	    protected void onPreExecute(){
	        super.onPreExecute();
	        mDialog = new ProgressDialog(NetworkWizardActivity.this);
	        mDialog.setMessage("Please wait while we try to configure your drone ...");
	        mDialog.setCancelable(false);
	        mDialog.show();
	    }
		
	    
		@Override
		protected Void doInBackground(String... ssids){
			
			if(mBound != true) return null;
			
			try{
				
				// Retrieve network manager
				WifiNetworkManager wifiNetworkManager = mService.getWifiNetworkManager();
				
				// Connect to network
				wifiNetworkManager.connectToOpenNetwork(ssids[0]);
				
				// TODO Wait untill connected or throw timeoutexception
				
				// TODO Ask to push telnet configuration

			}catch(Exception e){
				Log.e(mTag, "Failed to configure drone.", e);
				mException = e;
			}
			
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void result){
			/*if(mException != null){
				/* TODO Handle exception *
			}else{
				mProgressBarScan.setVisibility(View.GONE);
		        mListViewNetworks.setVisibility(View.VISIBLE);
			}*/
			
			// Dismiss dialog
			if(mDialog.isShowing()) mDialog.dismiss();
	    }
		
	}
	
}
