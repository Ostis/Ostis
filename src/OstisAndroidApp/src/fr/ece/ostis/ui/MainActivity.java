package fr.ece.ostis.ui;

import java.util.ArrayList;

import fr.ece.ostis.R;
import fr.ece.ostis.voice.VoiceRecognitionService;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-13
 */
public class MainActivity extends Activity{
	
	// Variables
	protected boolean _ServiceIsBound;
	protected Messenger _MessengerRemote = null;
	protected Messenger _MessengerLocal = new Messenger(new IncomingHandler());
	
	/**
	 * 
	 * @author Nicolas Schurando
	 * @version 2014-01-13
	 */
    class IncomingHandler extends Handler{
    	
    	/**
    	 * 
    	 */
        @Override public void handleMessage(Message _Message) {
            switch (_Message.what) {
            case VoiceRecognitionService.MSG_FINISHED_WITH_RESULTS:
            	
            	// Log
                Log.d("MainActivity", "Recus");
                
                // Debug : Display on ui
                TextView _TVDebug = (TextView) findViewById(R.id.textViewDebug);
                ArrayList<String> _Sentences = _Message.getData().getStringArrayList("VoiceRecognitionResult");
                if(_Sentences != null){
	    			for (int i = 0; i < _Sentences.size(); i++){
	                    _TVDebug.setText( _TVDebug.getText() + "\r\n" + _Sentences.get(i));
	    			}
                }
    			
                break;
            default:
                super.handleMessage(_Message);
            }
        }
        
    }
	
	
	/**
	 * 
	 */
	@Override protected void onCreate(Bundle savedInstanceState) {
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set content view
		setContentView(R.layout.activity_main);

		// Start voice recognition service
	    doStartService();
	    
	    // Set callback on button
	    Button _StartButton = (Button) findViewById(R.id.buttonStartVoiceRecognition);
	    _StartButton.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v) {
		        Message _Message = new Message();
		        _Message.what = VoiceRecognitionService.MSG_START_LISTENING; 

		        try{
		        	_MessengerRemote.send(_Message);
		        }catch (RemoteException e){
		            e.printStackTrace();
		        }
			}
		});
	}

	
	/**
	 * 
	 */
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		// Return
		return true;
		
	}
	
	
	/**
	 * 
	 */
	@Override protected void onStart(){
		
		// Super
	    super.onStart();

	    // Bind to voice recognition service
	    doBindService();
	    
	}

	
	/**
	 * 
	 */
	@Override protected void onStop(){

	    // Unbind from voice recognition service
        doUnbindService();
		
		// Super
	    super.onStop();
   
	}
	
	/**
	 * 
	 */
    @Override protected void onDestroy(){
    	
    	// Super
        super.onDestroy();

    }
    
    /**
     * 
     */
    private final ServiceConnection _ServiceConnection = new ServiceConnection(){
		
    	
    	/**
    	 * 
    	 */
	    @Override public void onServiceConnected(ComponentName name, IBinder service){

	        _MessengerRemote = new Messenger(service);
	        Message _Message = Message.obtain(null, VoiceRecognitionService.MSG_REGISTER_CLIENT);
	        _Message.replyTo = _MessengerLocal;
	        try{
	        	_MessengerRemote.send(_Message);
	        }catch (RemoteException e){
	        	
	        	// Log
	            e.printStackTrace();
	            
                // In this case the service has crashed before we could even do anything with it
	            // Nothing
	            
	        }
	    }

	    
	    /**
	     * 
	     */
	    @Override public void onServiceDisconnected(ComponentName name){
	    	
	    	// Log
	        Log.d("MainActivity", "onServiceDisconnected");
	        
	        // Delete messenger
	        _MessengerLocal = null;
	        
	    }

	};

	
	/**
	 * 
	 */
    protected void doStartService(){
    	
    	// Start service via intent
    	Intent _ServiceIntent = new Intent(MainActivity.this, VoiceRecognitionService.class);
	    startService(_ServiceIntent);
    	
    }

    
    /**
     * 
     */
	protected void doBindService(){
    	
    	if(_ServiceIsBound != true){
	    	
	    	// Bind to service
		    bindService(new Intent(this, VoiceRecognitionService.class), _ServiceConnection, Context.BIND_AUTO_CREATE);
		    
		    // Set flag
		    _ServiceIsBound = true;
	        
    	}
    	
    }
	

	/**
	 * 
	 */
	protected void doUnbindService(){
    	
    	if(_ServiceIsBound != false){
    		
    		// Unbind from service
            unbindService(_ServiceConnection);
            
            // Set flag
            _ServiceIsBound = false;
            
    	}
    	
    }

}
