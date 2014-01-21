package fr.ece.ostis.ui;

import java.lang.ref.WeakReference;

import fr.ece.ostis.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.view.Menu;


/**
 * 
 * @author Nicolas Schurando
 * @version 2014-01-21
 */
public class HomeActivity extends Activity implements ServiceConnection {

	
	/** Is bound to ostis service. */
	protected boolean mOstisServiceIsBound = false;
	
	
	/** Messenger to ostis service. */
	protected Messenger mMessengerToOstisService = null;


	/** Messenger from ostis service. */
	protected Messenger mMessengerFromOstisService = new Messenger(new IncomingMessageFromServiceHandler(this));
	
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
	}
	

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}


	@Override public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		
	}


	@Override public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 * @author Nicolas Schurando
	 * @version 2014-01-21
	 */
	protected static class IncomingMessageFromServiceHandler extends Handler{
		
		
		/** Reference to the activity. */
		private final WeakReference<HomeActivity> mActivityReference;
		
		
		/**
		 * Constructor.
		 * @param activity
		 */
		IncomingMessageFromServiceHandler(HomeActivity activity){
			mActivityReference = new WeakReference<HomeActivity>(activity);
		}
		
    	
    	/**
    	 * 
    	 */
        @Override public void handleMessage(Message message){

			// Retrieve the activity instance
			HomeActivity activity = mActivityReference.get();
        	
            switch(message.what){
            
	            /*case OstisService.MSG_NETWORK_STATUS_UPDATED:
	            	((ImageView) activity.findViewById(R.id.imageViewWiFi)).setImageResource(R.drawable.icon_tick);
	            	((ImageView) activity.findViewById(R.id.ImageView3G)).setImageResource(R.drawable.icon_tick);
	                break;*/
	                
	            default:
	                super.handleMessage(message);
            }
            
        }
        
    }

}
