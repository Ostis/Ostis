package fr.ece.ostis.ui;

import fr.ece.ostis.R;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-30
 */
public class FlyActivity extends ConnectedActivity{

	
	/** Log tag. */
	protected static final String mTag = "FlyActivity";

	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fly);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fly, menu);
		return true;
	}


	@Override
	protected void onBoundToOstisService() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void onUnboundFromOstisService() {
		// TODO Auto-generated method stub
		
	}
	
}