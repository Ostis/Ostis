package fr.ece.ostis;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import fr.ece.ostis.lang.LanguageManager;
import fr.ece.ostis.speech.SpeechRecognitionService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;


/**
 * TODO Docu.
 * TODO Include reference to the drone proxy.
 * @author Nicolas Schurando
 * @version 2014-01-14
 */
public class OstisService extends Service{
	
	
	/*
	 * Message codes.
	 */
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	
	
	/**
	 * List of client messengers.
	 */
	protected ArrayList<Messenger> mClientMessengers = new ArrayList<Messenger>();
	
	
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mServiceMessenger = new Messenger(new IncomingMessageFromClientHandler(this));
	
	
	/**
	 * Reference to the speech recognition service.
	 */
	protected SpeechRecognitionService mSpeechRecognitionService = null;
	
	
	/**
	 * Reference to the language manager.
	 */
	protected LanguageManager mLanguageManager = null;

	
	/**
	 * TODO
	 * @param intent
	 */
	@Override public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-14
	 */
	protected static class IncomingMessageFromClientHandler extends Handler{
		
		
		/**
		 * Reference to the ostis service.
		 */
		private final WeakReference<OstisService> mServiceReference;
		
		
		/**
		 * Constructor.
		 * @param service
		 */
		IncomingMessageFromClientHandler(OstisService service){
			mServiceReference = new WeakReference<OstisService>(service);
		}
	
		
		/**
		 * TODO
		 * @param message
		 */
		@Override public void handleMessage(Message message){
			
			// Log
			Log.d("OstisService", "handleMessage | what = " + String.valueOf(message.what));

			// Retrieve the service instance
			OstisService service = mServiceReference.get();
			
			// React according to the type of object of the message
			switch(message.what){

				// Register a new client
				case MSG_REGISTER_CLIENT:
					Log.d("OstisService", "handleMessage -> New client registered");
					service.mClientMessengers.add(message.replyTo);
					break;
					
				// Unregister a client
				case MSG_UNREGISTER_CLIENT:
					Log.d("OstisService", "handleMessage -> Removing existing client");
					service.mClientMessengers.remove(message.replyTo);
					break;
		
			}
			
		}
		
	}
	
}
