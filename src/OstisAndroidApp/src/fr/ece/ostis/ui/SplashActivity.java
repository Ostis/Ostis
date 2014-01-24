package fr.ece.ostis.ui;

import fr.ece.ostis.R;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.content.Intent;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-24
 */
public class SplashActivity extends Activity{

	protected CountDownTimer mTimer = null;
	protected static final long mSplashTime = 1000;
	protected static final Class<DebugActivity> mNextActivity = DebugActivity.class;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.activity_splash);
		
		// Create timer
		mTimer = new CountDownTimer(mSplashTime, mSplashTime){
			
			
			@Override
			public void onTick(long millisUntilFinished){ }
			
			
			@Override
			public void onFinish(){
				Intent intent = new Intent(SplashActivity.this, mNextActivity);
				startActivity(intent);
			}
		};
		
	}
	
	
	@Override
	protected void onStart(){
		
		// Super
		super.onStart();
		

		// Start timer
		mTimer.start();
		
	}
	
	
	@Override
	protected void onStop(){
		
		// Cancel timer
		if(mTimer != null) mTimer.cancel();
		
		// Super
		super.onStop();
		
	}

}
