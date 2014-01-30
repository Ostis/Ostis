package fr.ece.ostis.ui;

import java.util.List;

import fr.ece.ostis.R;
import fr.ece.ostis.network.OnWifiScanResultsUpdatedListener;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;

public class NetworkWizardActivity extends ConnectedActivity implements OnWifiScanResultsUpdatedListener{

	
	/** Stores the current step of the wizard. */
	protected int mCurrentStep = -1;
	
	
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
		setContentView(R.layout.acitivity_network);
		
		// Find controls
		final RadioButton buttonMethod1 = (RadioButton) findViewById(R.id.radioButtonMethod1);
		final RadioButton buttonMethod2 = (RadioButton) findViewById(R.id.radioButtonMethod2);
		final Button buttonNext = (Button) findViewById(R.id.buttonNext);
		
		// Set on click listeners
		buttonNext.setOnClickListener(new OnClickListener(){
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
	
	
	protected void setupStep2(){
		
		// Update step value
		mCurrentStep = 2;
		
		// Set layout
		setContentView(R.layout.activity_network_method2_step1);
		
		// Ask for wifi scan
		try{
			mService.getWifiNetworkManager().startWifiScan(this);
		}catch(Exception e){
			Log.e(mTag, "Could not scan for wifi networks.", e);
		}
		
	}

	
	@Override
	protected void onBoundToOstisService(){
		// TODO Auto-generated method stub
	}

	
	@Override
	protected void onUnboundFromOstisService(){
		// TODO Auto-generated method stub
	}


	@Override
	public void onWifiScanResultsUpdated(List<ScanResult> wifiList){
		Log.i(mTag, "Wifi scan results received.");
		
		// TODO Update wifi list
	}
	
}
