package fr.ece.ostis.ui;

import java.io.IOException;
import java.util.ArrayList;

import fr.ece.ostis.DroneFrameReceivedListener;
import fr.ece.ostis.DroneStatusChangedListener;
import fr.ece.ostis.R;
import fr.ece.ostis.speech.SpeechRecognitionResultsListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public class FlyActivity extends ConnectedActivity implements SpeechRecognitionResultsListener, DroneStatusChangedListener, DroneFrameReceivedListener{

	
	/** Log tag. */
	protected static final String mTag = "FlyActivity";

	
	/* Controls. */
	protected ImageView mImageViewCamera = null;
	protected ImageView mImageViewSpeechStatus = null;
	protected TextView mTextViewSpeechStatus = null;
	protected ImageView mImageViewDroneStatus = null;
	protected TextView mTextViewDroneStatus = null;
	
	
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
		mTextViewDroneStatus = (TextView) findViewById(R.id.TextViewDroneStatus);
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fly, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_emergency_stop:
				try {
					mService.getDrone().sendEmergencySignal();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	@Override
	protected void onBoundToOstisService(){
		
		// Register service callbacks
		mService.registerStatusChangedListener(this);
		mService.registerFrameReceivedListener(this);
		
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
	
}