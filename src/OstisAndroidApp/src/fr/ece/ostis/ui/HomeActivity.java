package fr.ece.ostis.ui;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.R;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-28
 */
public class HomeActivity extends ConnectedActivity{

	
	/** Log tag. */
	protected static final String mTag = "HomeActivity";
	
	
	/** Local copy of the drone connection status. */
	protected int mDroneConnectionStatus = OstisService.DRONE_STATUS_UNKNOWN;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.activity_home);
		
		// Add on click listeners
		Button buttonFly = (Button) findViewById(R.id.buttonFly);
		buttonFly.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v){
				if(mOstisServiceIsBound){
					if(mDroneConnectionStatus == OstisService.DRONE_STATUS_CONNECTED){
						// If connected to drone, go directly to fly view
						Intent intent = new Intent(HomeActivity.this, FlyActivity.class);
						startActivity(intent);
					}else{
						// Else go to network wizard
						Intent intent = new Intent(HomeActivity.this, NetworkWizardActivity.class);
						startActivity(intent);
					}
				}
			}
			
		});
	}


	@Override
	protected void onBoundToOstisService(){
		
        try{
        	// Ask for connection status to drone
        	sendMessageToOstisService(OstisService.MSG_DRONE_STATUS_REQUEST);
        }catch(Exception e){
        	Log.w(mTag, "Could not send a drone connection status request to the ostis service.", e);
        }
        
	}


	@Override
	protected void onUnboundFromOstisService(){

	}
	

	@Override
	protected void onMessageFromOstisService(Message message){
		
		switch(message.what){
		
			// Retrieve and store connection status with drone
			case OstisService.MSG_DRONE_STATUS_UPDATED:
				mDroneConnectionStatus = message.getData().getInt("droneConnectionStatus");
				break;
		
		}
		
	}

}
