package fr.ece.ostis;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import fr.ece.ostis.speech.SpeechRecognitionService;

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
 * Implements all the communication-related functions for the OstisService. Has to be extended by the OstisService.
 * @author Nicolas Schurando
 * @version 2014-01-29
 */
public abstract class OstisServiceCommunicator extends Service{
	
	
	/*
	 * Constants
	 */
	public static final int MSG_REGISTER_CLIENT = 1;			// Message code : register a new client
	public static final int MSG_UNREGISTER_CLIENT = 2;			// Message code : unregister an existing client
	
	
    /** Log tag. */
    protected static final String mTag = "OstisServiceCommunicator";
	
	
	/** Reference to the speech recognition service. */
	protected SpeechRecognitionService mSpeechRecognitionService = null; 
	
	
	/** TODO */
	protected boolean mSpeechServiceIsBound = false;
    
	
	/** List of client messengers. */
	protected ArrayList<Messenger> mMessengersToClients = new ArrayList<Messenger>();
	
	
	/** Target we publish for clients to send messages to IncomingHandler. */
	protected Messenger mMessengerFromClients = new Messenger(new IncomingMessageFromClientHandler(this));
	
	
	/** Local messenger as receiver we publish to the speech recognizer. */
	protected Messenger mMessengerFromSpeechService = new Messenger(new IncomingMessageFromSpeechRecognizer(this)); 
	
	
	/** TODO */
	protected Messenger mMessengerToSpeechService = null;

	
	/** Class for monitoring the state of the speech recognition service. */
    private final ServiceConnection mServiceSpeechConnection = new ServiceConnection(){
		
    	
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service){
	    	
	    	// Log
	        Log.i(mTag, "Connected to speech recognition service.");
	        
	        // Retrieve messenger to speech recognition service
	    	mMessengerToSpeechService = new Messenger(service);
	    	
	    	// Send register message
	        Message message = Message.obtain(null, SpeechRecognitionService.MSG_REGISTER_CLIENT);
	        message.replyTo = mMessengerFromSpeechService;
	        try{
	        	mMessengerToSpeechService.send(message);
	        }catch(RemoteException e){
	            Log.w(mTag, e);
                // In this case the service has crashed before we could even do anything with it
	        }
	        
	    }

	    
	    @Override
	    public void onServiceDisconnected(ComponentName name){
	    	
	    	// Log
	    	Log.i(mTag, "Disconnected from speech recognition service.");
	    	
	    	// Unset messenger
	        mMessengerToSpeechService = null;
	        
	    }

	};


	@Override
	public void onCreate(){
		
		// Super
		super.onCreate();
		
		// Bind to speech recognition service
		doBindSpeechService();
		
	}
	

	@Override
	public void onDestroy(){
		
		// Unbind from speech recognition service
		doUnbindSpeechService();
		
		// Cleanup
		mMessengersToClients.clear();
		
		// Super
		super.onDestroy();
		
	}
	
	
	
	@Override
	public IBinder onBind(Intent intent){
		
		// Log
		Log.i(mTag, "Client bound to service.");
		
		// When binding to the service, we return an interface to our messenger for sending messages to the service.
		return mMessengerFromClients.getBinder();
		
	}
	
	
    /**
     * Binds to the speech recognition service.
     */
	protected void doBindSpeechService(){
    	if(mSpeechServiceIsBound != true){
		    bindService(new Intent(this, SpeechRecognitionService.class), mServiceSpeechConnection, Context.BIND_AUTO_CREATE);
		    mSpeechServiceIsBound = true;
    	}
    }
	

	/**
	 * Unbinds from the speech recognition service.
	 */
	protected void doUnbindSpeechService(){
    	if(mSpeechServiceIsBound != false){
	        try{
		        Message message = Message.obtain(null, SpeechRecognitionService.MSG_UNREGISTER_CLIENT);
		        message.replyTo = mMessengerFromSpeechService;
	        	mMessengerToSpeechService.send(message);
	        }catch(RemoteException e){
	        	e.printStackTrace();
	        }
    		unbindService(mServiceSpeechConnection);
    		mSpeechServiceIsBound = false;
    	}
    }
	
	
	/**
	 * TODO
	 * @param id
	 * @param message
	 */
	protected void sendMessageToClient(int id, Message message){
		try{
        	mMessengersToClients.get(id).send(message);
        }catch(RemoteException e){
        	Log.w(mTag, e);
        }
	}
	
	
	/**
	 * TODO
	 * @param message
	 */
	protected void sendMessageToClients(Message message){
		for(int clientsIterator = mMessengersToClients.size() - 1; clientsIterator >= 0; clientsIterator--){
	        try{
	        	mMessengersToClients.get(clientsIterator).send(message);
	        }catch(RemoteException e){
	        	Log.w(mTag, e);
	        }
		}
	}
	
	
	/**
	 * TODO
	 * @param message
	 */
	protected abstract void onMessageFromClient(Message message);
	
	
	/**
	 * TODO
	 * @param message
	 */
	protected abstract void onMessageFromSpeechRecognizer(Message message);
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-29
	 */
	protected static class IncomingMessageFromClientHandler extends Handler{
		
		
		/** Reference to the ostis service. */
		private final WeakReference<OstisServiceCommunicator> mOstisServiceReference;
		
		
		/**
		 * Constructor.
		 * @param service
		 */
		IncomingMessageFromClientHandler(OstisServiceCommunicator service){
			mOstisServiceReference = new WeakReference<OstisServiceCommunicator>(service);
		}
	
		
		@Override
		public void handleMessage(Message message){
			
			// Log
			Log.i(mTag, "Message " + String.valueOf(message.what) + " received from client.");
			
			// Retrieve the service instance
			OstisServiceCommunicator service = mOstisServiceReference.get();
			
			// React according to the type of object of the message
			switch(message.what){

				// Register a new client
				case MSG_REGISTER_CLIENT:
					Log.i(mTag, "Handling message : New client registered");
					service.mMessengersToClients.add(message.replyTo);
					break;
					
				// Unregister a client
				case MSG_UNREGISTER_CLIENT:
					Log.i(mTag, "Handling message : Removing existing client");
					service.mMessengersToClients.remove(message.replyTo);
					break;
					
				// Call handler
				default:
					service.onMessageFromClient(message);
					break;
			}
			
		}
		
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-29
	 */
	protected static class IncomingMessageFromSpeechRecognizer extends Handler{
		
		
		/** Reference to the ostis service. */
		private final WeakReference<OstisServiceCommunicator> mServiceReference;
		
		
		/**
		 * Constructor.
		 * @param service
		 */
		IncomingMessageFromSpeechRecognizer(OstisServiceCommunicator service){
			mServiceReference = new WeakReference<OstisServiceCommunicator>(service);
		}
	
		
		@Override
		public void handleMessage(Message message){
			
			// Log
			Log.i(mTag, "Message " + String.valueOf(message.what) + " received from speech recognition service.");

			// Retrieve the service instance
			OstisServiceCommunicator service = mServiceReference.get();
			
			// Call handler
			service.onMessageFromSpeechRecognizer(message);
			
		}
		
	}

}
