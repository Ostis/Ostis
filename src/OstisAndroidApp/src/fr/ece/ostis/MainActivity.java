package fr.ece.ostis;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	// Variables
	private int _BindFlag;
	private Messenger _ServiceMessenger;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set content view
		setContentView(R.layout.activity_main);

		// Start voice recognition service
	    Intent _ServiceIntent = new Intent(MainActivity.this, VoiceRecognitionService.class);
	    startService(_ServiceIntent);
	    _BindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// Return
		return true;
		
	}
	
	@Override
	protected void onStart(){
		
		// Super
	    super.onStart();

	    // Bind to voice recognition service
	    bindService(new Intent(this, VoiceRecognitionService.class), _ServiceConnection, _BindFlag);
	    
	    // Set callback on button
	    Button _StartButton = (Button) findViewById(R.id.buttonStartVoiceRecognition);
	    _StartButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
		        Message _Message = new Message();
		        _Message.what = VoiceRecognitionService.MSG_RECOGNIZER_START_LISTENING; 

		        try{
		            _ServiceMessenger.send(_Message);
		        }catch (RemoteException e){
		            e.printStackTrace();
		        }
			}
		});
	    
	}

	@Override
	protected void onStop(){
		
		// Super
	    super.onStop();

	    // Unbind from voice recognition service
	    if (_ServiceMessenger != null){
	        unbindService(_ServiceConnection);
	        _ServiceMessenger = null;
	    }
	}
	
	
	private final ServiceConnection _ServiceConnection = new ServiceConnection(){
		
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service){

	        _ServiceMessenger = new Messenger(service);
	        Message _Message = new Message();
	        _Message.what = VoiceRecognitionService.MSG_RECOGNIZER_START_LISTENING; 

	        try{
	            _ServiceMessenger.send(_Message);
	        }catch (RemoteException e){
	            e.printStackTrace();
	        }
	    }

	    @Override
	    public void onServiceDisconnected(ComponentName name){
	    	
	        Log.d("MainActivity", "onServiceDisconnected");
	        _ServiceMessenger = null;
	        
	    }

	};

}
