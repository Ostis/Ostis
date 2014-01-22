package fr.ece.ostis.ui;

import fr.ece.ostis.R;
import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.view.Menu;
import android.os.Handler;

/**
 * 
 * @author Nicolas Schurando
 * @version 2014-01-21
 */
public class FlyActivity extends Activity implements Handler.Callback {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fly);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fly, menu);
		return true;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		return false;
	}

}
