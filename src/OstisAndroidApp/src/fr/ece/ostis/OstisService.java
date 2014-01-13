package fr.ece.ostis;

import fr.ece.ostis.voice.VoiceRecognitionService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-13
 */
public class OstisService extends Service{
	
	/**
	 * TODO
	 */
	public OstisService(){
		
		
	}

	
	/**
	 * TODO
	 * @param intent
	 */
	@Override public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// Variables
	Context _Context = null;
	Messenger mService = null;
	boolean mIsBound;
	

	// TODO Set context
	
	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
	    @Override
	    public void handleMessage(Message msg){
	        switch (msg.what){
	            /*case VoiceRecognitionService.MSG_SET_VALUE:
	                Log.d("VRClient", "Received from service: " + msg.arg1);
	                break;*/
	            default:
	                super.handleMessage(msg);
	        }
	    }
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection(){
		
	    public void onServiceConnected(ComponentName className, IBinder service){
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
	        mService = new Messenger(service);
	        Log.d("VRClient", "Attached.");

	        // We want to monitor the service for as long as we are
	        // connected to it.
	        try {
	            Message msg = Message.obtain(null, VoiceRecognitionService.MSG_REGISTER_CLIENT);
	            msg.replyTo = mMessenger;
	            mService.send(msg);

	            // Give it some value as an example.
	            /*msg = Message.obtain(null, VoiceRecognitionService.MSG_SET_VALUE, this.hashCode(), 0);
	            mService.send(msg);*/
	        } catch (RemoteException e){
	            // In this case the service has crashed before we could even
	            // do anything with it; we can count on soon being
	            // disconnected (and then reconnected if it can be restarted)
	            // so there is no need to do anything here.
	        }

	    }

	    public void onServiceDisconnected(ComponentName className){
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        mService = null;
	        Log.d("VRClient", "Disconnected.");

	    }
	};

	void doBindService(){
	    // Establish a connection with the service.  We use an explicit
	    // class name because there is no reason to be able to let other
	    // applications replace our component.
		_Context.bindService(new Intent(_Context, VoiceRecognitionService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	    Log.d("VRClient", "Binding.");
	}

	void doUnbindService(){
	    if (mIsBound){
	        // If we have received the service, and hence registered with
	        // it, then now is the time to unregister.
	        if (mService != null){
	            try {
	                Message msg = Message.obtain(null, VoiceRecognitionService.MSG_UNREGISTER_CLIENT);
	                msg.replyTo = mMessenger;
	                mService.send(msg);
	            } catch (RemoteException e){
	                // There is nothing special we need to do if the service
	                // has crashed.
	            }
	        }

	        // Detach our existing connection.
	        _Context.unbindService(mConnection);
	        mIsBound = false;


	        // Log
	        Log.d("VRClient", "Unbinding.");
	    }
	}
	
}
