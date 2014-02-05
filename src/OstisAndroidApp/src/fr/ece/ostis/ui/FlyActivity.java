package fr.ece.ostis.ui;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.datatype.Duration;

import fr.ece.ostis.DroneBatteryChangedListener;
import fr.ece.ostis.DroneFrameReceivedListener;
import fr.ece.ostis.DroneStatusChangedListener;
import fr.ece.ostis.R;
import fr.ece.ostis.speech.SpeechRecognitionResultsListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public class FlyActivity extends ConnectedActivity implements SpeechRecognitionResultsListener, DroneStatusChangedListener, DroneBatteryChangedListener, DroneFrameReceivedListener{

	
	/** Log tag. */
	protected static final String mTag = "FlyActivity";

	
	/** Reference to the menu to change the battery level. */
	protected Menu mMenu = null;
	
	
	/* Controls. */
	protected ImageView mImageViewCamera = null;
	protected ImageView mImageViewSpeechStatus = null;
	protected TextView mTextViewSpeechStatus = null;
	protected ImageView mImageViewDroneStatus = null;
	protected TextView mTextViewDroneStatus = null;
	protected ToggleButton mToggleButtonTracking = null;
	protected ToggleButton mToggleButtonTakeOff = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.activity_fly);
		
		// Find controls
		mImageViewCamera = (ImageView) findViewById(R.id.imageViewCamera);
		mImageViewSpeechStatus = (ImageView) findViewById(R.id.imageViewSpeechStatus);
		mTextViewSpeechStatus = (TextView) findViewById(R.id.textViewSpeechStatus);
		mImageViewDroneStatus = (ImageView) findViewById(R.id.ImageViewDroneStatus);
		mTextViewDroneStatus = (TextView) findViewById(R.id.textViewDroneStatus);
		mToggleButtonTracking = (ToggleButton) findViewById(R.id.toggleButtonTracking);
		mToggleButtonTakeOff = (ToggleButton ) findViewById(R.id.toggleButtonTakeOff);
		
		// Set listeners on controls
		mToggleButtonTracking.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v){
				if(mBound) mService.debugToggleTracking(mToggleButtonTracking.isChecked());
			}
		});
		
		mToggleButtonTakeOff.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mBound) mService.debugToggleTakeOff(mToggleButtonTakeOff.isChecked());
			}
		});
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		
		// Store the reference
		mMenu = menu;
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fly, menu);
		
		// Return
		return true;
		
	}
	
	
	/*@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		
	}*/

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    switch(item.getItemId()) {
	        case R.id.action_emergency_stop:
				try{
					if(mBound && mService != null) mService.getDrone().sendEmergencySignal();
				}catch (IOException e){}
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	
	@Override
	public void onBackPressed(){
		// TODO DEBUG REMOVE
		mService.doDroneDisconnect();
		
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);   
	}
	
	
	@Override
	protected void onBoundToOstisService(){
		
		// Register service callbacks
		mService.registerStatusChangedListener(this);
		mService.registerFrameReceivedListener(this);
		mService.registerBatteryChangedListener(this);
		
		// Activate speech results <-> action matching
		mService.activateSpeechResultsToActionMatching();
		
		// Register to speech status
		mService.getSpeechRecognitionManager().registerCallback(this);
		
	}


	@Override
	protected void onBeforeUnbindFromOstisService(){
		
		// Unregister callabacks
		mService.unregisterFrameReceivedListener(this);
		mService.unregisterStatusChangedListener(this);
		mService.unregisterBatteryChangedListener(this);
		
		// Desactivate speech results <-> action matching
		mService.desactivateSpeechResultsToActionMatching();
		
	}


	@Override
	protected void onUnboundFromOstisService(){
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onSpeechRecognitionResultsAvailable(ArrayList<String> sentences) {
		mImageViewSpeechStatus.setImageResource(R.drawable.icon_alert);
		mTextViewSpeechStatus.setText("Please wait");
	}


	@Override
	public void onReadyForSpeech(){
		mImageViewSpeechStatus.setImageResource(R.drawable.icon_tick);
		mTextViewSpeechStatus.setText("Ready for order");
	}


	@Override
	public void onError(){
		mImageViewSpeechStatus.setImageResource(R.drawable.icon_alert);
		mTextViewSpeechStatus.setText("Please wait");
	}


	@Override
	public void onEndOfSpeech() {
		mImageViewSpeechStatus.setImageResource(R.drawable.icon_alert);
		mTextViewSpeechStatus.setText("Please wait");
	}


	@Override
	public void onDroneConnected(){
		mImageViewDroneStatus.setImageResource(R.drawable.icon_tick);
		mTextViewDroneStatus.setText("Connected");
	}


	@Override
	public void onDroneConnectionFailed(){
		mImageViewDroneStatus.setImageResource(R.drawable.icon_cross);
		mTextViewDroneStatus.setText("Connection failed");
	}


	@Override
	public void onDroneDisconnected() {
		mImageViewDroneStatus.setImageResource(R.drawable.icon_cross);
		mTextViewDroneStatus.setText("Disconnected");
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle("Connection lost");
		alertDialogBuilder.setMessage("Connection to drone was lost, you will be redirected to the previous screen.");
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setPositiveButton("I understand", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id){
				FlyActivity.this.finish();
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}


	@Override
	public void onDroneFrameReceived(Bitmap b) {
		if(mImageViewCamera != null){
			//((BitmapDrawable) mImageViewCamera.getDrawable()).getBitmap().recycle(); 
			mImageViewCamera.setImageBitmap(b);
		}else{
			Log.w(mTag, "Could not find image view to display camera feed.");
		}
	}


	@Override
	public void onDroneBatteryChanged(int level){
	    MenuItem item = mMenu.findItem(R.id.action_battery_level);
	    item.setTitle(String.valueOf(level) + " %");
	}


	@Override
	public void onDroneBatteryTooLow(int level){
		Toast toast = Toast.makeText(this, "Battery too low !", Toast.LENGTH_SHORT);
		toast.show();
	}
	
}