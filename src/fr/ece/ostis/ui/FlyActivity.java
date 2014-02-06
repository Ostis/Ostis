package fr.ece.ostis.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import fr.ece.ostis.ActionExecutedListener;
import fr.ece.ostis.DroneBatteryChangedListener;
import fr.ece.ostis.DroneFrameReceivedListener;
import fr.ece.ostis.DroneStatusChangedListener;
import fr.ece.ostis.OstisService;
import fr.ece.ostis.R;
import fr.ece.ostis.actions.Action;
import fr.ece.ostis.lang.LanguageManager;
import fr.ece.ostis.speech.SpeechRecognitionResultsListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public class FlyActivity extends ConnectedActivity implements SpeechRecognitionResultsListener, DroneStatusChangedListener, DroneBatteryChangedListener, DroneFrameReceivedListener, ActionExecutedListener{

	
	/** Log tag. */
	protected static final String mTag = "FlyActivity";

	
	/** Reference to the menu to change the battery level. */
	protected Menu mMenu = null;
	
	
	/** List of last executed actions. */
	protected ArrayList<Action> mLastActions = new ArrayList<Action>();
	
	
	/** Reference to the language manager. */
	protected LanguageManager mLanguageManager = null;
	
	
	/** Reference to the adapter. */
	protected ActionListAdapterItem mListViewActionsAdapter = null;
	
	
	/* Controls. */
	protected ImageView mImageViewCamera = null;
	protected ImageView mImageViewSpeechStatus = null;
	protected TextView mTextViewSpeechStatus = null;
	protected ImageView mImageViewDroneStatus = null;
	protected TextView mTextViewDroneStatus = null;
	protected ListView mListViewActions = null;
	
	
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
		mListViewActions = (ListView) findViewById(R.id.listViewActions);
		
		// Instantiate language manager
		mLanguageManager = new LanguageManager();
		
		// Instantiate adapter
		mListViewActionsAdapter = new ActionListAdapterItem(this, R.layout.list_action, mLastActions);
		mListViewActions.setAdapter(mListViewActionsAdapter);
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		
		// Store the reference
		mMenu = menu;
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fly, menu);
		
		// Ask for current battery state
		onDroneBatteryChanged(mService.getDroneBattery());
		
		// Return
		return true;
		
	}

	/*@Override
	public boolean onPrepareOptionsMenu(Menu menu)*/
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    switch(item.getItemId()) {
	        case R.id.action_emergency_stop:
				try{
					if(mBound && mService != null) mService.getDrone().sendEmergencySignal();
				}catch (IOException e){}
	            return true;
	        case R.id.action_debug_takeoff:
	        	if (mBound) mService.debugToggleTakeOff(true);
	        	return true;
	        case R.id.action_debug_land:
	        	if (mBound) mService.debugToggleTakeOff(false);
	        	return true;
	        case R.id.action_debug_tracking_on:
	        	if(mBound && mService != null) mService.debugToggleTracking(true);
	        	return true;
	        case R.id.action_debug_tracking_off:
	        	if(mBound && mService != null) mService.debugToggleTracking(false);
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
		mService.registerActionExecutedListener(this);
		
		// Ask for current battery state
		onDroneBatteryChanged(mService.getDroneBattery());
		
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
		mService.unregisterActionExecutedListener(this);
		
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
	public void onDroneBatteryChanged(final int level){
	    if(mMenu != null){
			Handler mainHandler = new Handler(this.getMainLooper());
			Runnable myRunnable = new Runnable(){
				@Override
				public void run(){
			    	MenuItem item = mMenu.findItem(R.id.action_battery_level);
			    	item.setTitle(String.valueOf(level) + " %");
				}
			};
			mainHandler.post(myRunnable);
	    }
	}


	@Override
	public void onDroneBatteryTooLow(final int level){
		Handler mainHandler = new Handler(this.getMainLooper());
		Runnable myRunnable = new Runnable(){
			@Override
			public void run(){
				Toast toast = Toast.makeText(FlyActivity.this, "Battery too low !", Toast.LENGTH_SHORT);
				toast.show();
			}
		};
		mainHandler.post(myRunnable);
	}


	@Override
	public void onActionExecuted(final Action action){
		
		Handler mainHandler = new Handler(this.getMainLooper());
		Runnable myRunnable = new Runnable(){
			@Override
			public void run(){
		
				// Prevent control not fetched
				if(mListViewActions == null || mListViewActionsAdapter == null) return;
				
				// Add and notify
				mLastActions.add(action);
				mListViewActionsAdapter.notifyDataSetChanged();
				
			}
		};
		mainHandler.post(myRunnable);

	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-02-05
	 */
	protected class ActionListAdapterItem extends ArrayAdapter<Action>{

	    Context mContext;
	    int mLayoutResourceId;
	    ArrayList<Action> mData = null;

	    
	    public ActionListAdapterItem(Context context, int layoutResourceId, ArrayList<Action> data){
	        super(context, layoutResourceId, data);
	        mContext = context;
	        mData = data;
	        mLayoutResourceId = layoutResourceId;
	    }

	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent){
	    	
            // Inflate the layout if not already created
	        if(convertView == null){
	            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
	            convertView = inflater.inflate(mLayoutResourceId, parent, false);
	        }

	        // action based on the position
	        Action action = mData.get(position);

	        // get the TextView and then set the text (item name) and tag (item ID) values
	        TextView textViewName = (TextView) convertView.findViewById(R.id.textViewName);
	        TextView textViewCommand = (TextView) convertView.findViewById(R.id.textViewCommand);
	        TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
	        textViewName.setText(action.getName(mLanguageManager.getCurrentLocale()));
	        textViewCommand.setText("\" " + action.getVocalCommand(mLanguageManager.getCurrentLocale()) + " \"");
	        textViewDescription.setText(action.getDescription(mLanguageManager.getCurrentLocale()));

	        // Return view
	        return convertView;
	    }

	}
	
}