package fr.ece.ostis.ui;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-05-02
 */
public class HomeActivity extends ConnectedActivity{

	
	/** Log tag. */
	protected static final String mTag = "HomeActivity";
	
	/* Buttons */
	protected Button mButtonFly = null;
	protected Button mButtonGallery = null;
	protected Button mButtonConfig = null;
	protected Button mButtonAbout = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.activity_home);
		
		// Find controls
		mButtonFly = (Button) findViewById(R.id.buttonFly);
		mButtonConfig = (Button) findViewById(R.id.buttonConfig);
		
		// Add on click listeners
		mButtonFly.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				if(mBound){
					if(mService.getDroneStatus() == OstisService.DRONE_STATUS_CONNECTED){
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
		mButtonConfig.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mBound){
					Intent intent = new Intent(HomeActivity.this, ActionsListActivity.class);
					startActivity(intent);
				}
			}
		});
	}


	@Override
	protected void onBoundToOstisService(){
		
		// Register for callbacks eventually
        
        // Enable buttons
        if(mButtonFly != null) mButtonFly.setEnabled(true);
        if(mButtonConfig != null) mButtonConfig.setEnabled(true);
 
	}


	@Override
	protected void onBeforeUnbindFromOstisService() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void onUnboundFromOstisService(){

        // Disable buttons
        if(mButtonFly != null) mButtonFly.setEnabled(false);
        if(mButtonConfig != null) mButtonConfig.setEnabled(false);
		
	}

}
