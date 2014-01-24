package fr.ece.ostis.speech;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;


/**
 * TODO
 * The Jelly been workaround is based on the work of Hoan Nguyen at http://stackoverflow.com/questions/14940657/android-speech-recognition-as-a-service-on-android-4-1-4-2/14950616#14950616.
 * @author Nicolas Schurando
 * @version 2014-01-24
 */
public class SpeechRecognitionService extends Service{
	
	
	/** Audio manager for mute hack. */
	protected AudioManager mAudioManager; 
	
	
	/** Speech recognizer. */
	protected SpeechRecognizer mSpeechRecognizer;
	
	
	/** Speech recognizer intent contains the parameters of the recognition. */
	protected Intent mSpeechRecognizerIntent;
	
	
	/** Is listening flag. */
	protected boolean mIsListening;
	
	
	/** Is countdown on flag. */
	protected volatile boolean mIsCountDownOn;
	
	
	/** List of client messengers. */
	protected ArrayList<Messenger> mMessengersToClients = new ArrayList<Messenger>();
	
	
	/** Target we publish for clients to send messages to IncomingHandler. */
	final Messenger mMessengerFromClients = new Messenger(new IncomingMessageFromClientHandler(this));

	
	/** Count down timer for Jelly Bean work around.
	 * TODO : Eventually implement relaunch on tick and exception on finish
	 */
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(1000, 1000){

		@Override public void onTick(long millisUntilFinished){ }

		@Override public void onFinish(){
			
			// Log
			Log.d("SpeechRecognitionService/CountDownTimer", "onFinish");
			
			// Reset flags
			mIsCountDownOn = false;

			// Cancel current listening
			try{
				Message message = Message.obtain(null, MSG_CANCEL_LISTENING);
				mMessengerFromClients.send(message);
			}catch(RemoteException e){
				e.printStackTrace();
			}

			// Launch new listening
			try{
				Message message = Message.obtain(null, MSG_START_LISTENING);
				mMessengerFromClients.send(message);
			}catch(RemoteException e){
				e.printStackTrace();
			}
			
		}
		
	};

	
	/*
	 * Message codes.
	 */
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_START_LISTENING = 3;
	public static final int MSG_CANCEL_LISTENING = 4;
	public static final int MSG_FINISHED_WITH_RESULTS = 5;
	public static final int MSG_FINISHED_NO_RESULTS = 6;

	
	/**
	 * TODO
	 */
	@Override public void onCreate(){
		
		// Super
		super.onCreate();
		
		// Log
		Log.d("SpeechRecognitionService", "onCreate");
		
		// Retrieve audio manager
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		// Retrieve speech recognizer
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
		
		// Create speech recognizer intent
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		
	}
	

	/**
	 * TODO
	 */
	@Override public void onDestroy(){
		
		// Log
		Log.d("SpeechRecognitionService", "onDestroy");

		// Cleanup
		if (mIsCountDownOn) mNoSpeechCountDown.cancel();
		if (mSpeechRecognizer != null) mSpeechRecognizer.destroy();
		
		// Super
		super.onDestroy();
		
	}

	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-24
	 */
	protected static class IncomingMessageFromClientHandler extends Handler{
		
		
		/** Reference to the speech recognition service. */
		private final WeakReference<SpeechRecognitionService> mServiceReference; 
		
		
		/**
		 * Constructor.
		 * @param service
		 */
		IncomingMessageFromClientHandler(SpeechRecognitionService service){
			mServiceReference = new WeakReference<SpeechRecognitionService>(service);
		}
		
		
		/**
		 * TODO
		 * @param message
		 */
		@Override public void handleMessage(Message message){
			
			// Log
			Log.d("SpeechRecognitionService", "handleMessage | what = " + String.valueOf(message.what));

			// Retrieve the service instance
			SpeechRecognitionService service = mServiceReference.get();
			
			// React according to the type of object of the message
			switch(message.what){

				// Register a new client
				case MSG_REGISTER_CLIENT:
					Log.d("SpeechRecognitionService", "handleMessage -> New client registered");
					service.mMessengersToClients.add(message.replyTo);
					break;
					
				// Unregister a client
				case MSG_UNREGISTER_CLIENT:
					Log.d("SpeechRecognitionService", "handleMessage -> Removing existing client");
					service.mMessengersToClients.remove(message.replyTo);
					break;
			
				// Start listening in background
				case MSG_START_LISTENING:
					
					// Workaround to turn off beep sound
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
						service.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
					}
					
					// Start listening
					if (!service.mIsListening){
						Log.d("SpeechRecognitionService", "handleMessage -> Canceling listening (auto)");
						service.mSpeechRecognizer.cancel();
						Log.d("SpeechRecognitionService", "handleMessage -> Starting listening");
						service.mSpeechRecognizer.startListening(service.mSpeechRecognizerIntent);
						service.mIsListening = true;
						
						// Start the countdown timer for workaround
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
							service.mIsCountDownOn = true;
							service.mNoSpeechCountDown.start();
							service.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
						}
						
					}else{
						Log.e("SpeechRecognitionService", "handleMessage -> Already listening");
					}
					
					break;

				 case MSG_CANCEL_LISTENING:
					Log.d("SpeechRecognitionService", "handleMessage -> Canceling listening");
					service.mSpeechRecognizer.cancel();
					service.mIsListening = false;
					break;
			}
			
		}
		
	}


	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-13
	 */
	protected class SpeechRecognitionListener implements RecognitionListener{

		
		/**
		 * Called whenever the user has started to speak.
		 */
		@Override public void onBeginningOfSpeech(){
			
			// Log
			Log.d("SpeechRecognitionService", "onBeginningOfSpeech");
			
		}

		
		/**
		 * Called whenever more sound has been received. We could use it
		 * to provide audio feedback to the user, but we don't want that.
		 * @param buffer a buffer containing a sequence of big-endian 16-bit integers representing a single channel audio stream.
		 */
		@Override public void onBufferReceived(byte[] buffer){ }

		
		/**
		 * Called after the user stops speaking.
		 */
		@Override public void onEndOfSpeech(){
			
			// Log
			Log.d("SpeechRecognitionService", "onEndOfSpeech");
			
		}

		
		/**
		 * TODO
		 * @param error
		 */
		@Override public void onError(int error){
			
			// Log
			Log.d("SpeechRecognitionService", "onError | error = " + String.valueOf(error));
			switch(error){
				case SpeechRecognizer.ERROR_AUDIO: Log.e("SpeechRecognitionService/onError", "Audio recording error."); break;
				case SpeechRecognizer.ERROR_CLIENT: Log.e("SpeechRecognitionService/onError", "Other client side errors."); break;
				case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: Log.e("SpeechRecognitionService/onError", "Insufficient permissions"); break;
				case SpeechRecognizer.ERROR_NETWORK: Log.e("SpeechRecognitionService/onError", "Other network related errors."); break;
				case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.e("SpeechRecognitionService/onError", "Network operation timed out."); break;
				case SpeechRecognizer.ERROR_NO_MATCH: Log.e("SpeechRecognitionService/onError", "No recognition result matched."); break;
				case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: Log.e("SpeechRecognitionService/onError", "RecognitionService busy."); break;
				case SpeechRecognizer.ERROR_SERVER: Log.e("SpeechRecognitionService/onError", "Server sends error status."); break;
				case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: Log.e("SpeechRecognitionService/onError", "No speech input"); break;
				default: Log.e("SpeechRecognitionService/onError", "Unknown error"); break;
			}
			
			// Stop the countdown timer
			if (mIsCountDownOn){
				mIsCountDownOn = false;
				if(mNoSpeechCountDown != null) mNoSpeechCountDown.cancel();
			}

			// Cancel current listening
			try{
				Message message = Message.obtain(null, MSG_CANCEL_LISTENING);
				mMessengerFromClients.send(message);
			}catch(RemoteException e){
				e.printStackTrace();
			}
			
			// Start listening again
			try{
				Message message = Message.obtain(null, MSG_START_LISTENING);
				mMessengerFromClients.send(message);
			}catch (RemoteException e){
				e.printStackTrace();
			}
			
		}

		
		/**
		 * Currently not in use, reserved by Android for adding future events.
		 * @param eventType the type of the occurred event.
		 * @param params a Bundle containing the passed parameters.
		 */
		@Override public void onEvent(int eventType, Bundle params){ }

		
		/**
		 * Called when partial recognition results are available. We will not be using
		 * this feature since Google almost always ignores requests for partial results.
		 * @param partialResults
		 */
		@Override public void onPartialResults(Bundle partialResults){
			
			// Log
			Log.d("SpeechRecognitionService", "onPartialResults");
			
		}
		
		
		/**
		 * Called when the endpointer is ready for the user to start speaking.
		 * @param params parameters set by the recognition service. Reserved for future use.
		 */
		@Override public void onReadyForSpeech(Bundle params){
			
			// Log
			Log.d("SpeechRecognitionService", "onReadyForSpeech");
			
			// Speech input will be processed, so there is no need for count down anymore
			if (mIsCountDownOn){
				mIsCountDownOn = false;
				if(mNoSpeechCountDown != null) mNoSpeechCountDown.cancel();
			}
			
		}
		
		
		/**
		 * Called when recognition results are ready.
		 * @param results the recognition results. To retrieve the results in ArrayList<String> format use getStringArrayList(String) with RESULTS_RECOGNITION as a parameter. A float array of confidence values might also be given in CONFIDENCE_SCORES.
		 */
		@Override public void onResults(Bundle results){
			
			// Log
			Log.d("SpeechRecognitionService", "onResults");
			
			// Extract results
			ArrayList<String> sentences = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

			// Cancel current listening
			try{
				Message message = Message.obtain(null, MSG_CANCEL_LISTENING);
				mMessengerFromClients.send(message);
			}catch(RemoteException e){
				e.printStackTrace();
			}
			
			// Start listening again
			try{
				Message message = Message.obtain(null, MSG_START_LISTENING);
				mMessengerFromClients.send(message);
			}catch (RemoteException e){
				e.printStackTrace();
			}
			
			// Prevent null pointer exception
			if(sentences == null || scores == null) return;

			// Log
			for (int i = 0; i < sentences.size(); i++){
				Log.d("SpeechRecognitionService", "onResults | " + sentences.get(i) + " (" + String.valueOf(scores[i])+")");
			}
			
			// Send results to clients
			Bundle messageBundle = new Bundle();
			Message message = Message.obtain(null, MSG_FINISHED_WITH_RESULTS);
			messageBundle.putStringArrayList("SpeechRecognitionResult", sentences);
			message.setData(messageBundle);
			for(int clientsIterator = mMessengersToClients.size() - 1; clientsIterator >= 0; clientsIterator--){
				
				// Try to send the message to the client. If an exception is raised, it means that the client is dead. So we should
				// remove it from the list. We are going through the list from back to front so this is safe to do inside the loop.
				try{
					mMessengersToClients.get(clientsIterator).send(message);
				}catch(RemoteException e){
					e.printStackTrace();
	                mMessengersToClients.remove(clientsIterator);
				}
			}
			
		}

		
		/**
		 * The sound level in the audio stream has changed. There is no guarantee that this method will be called.
		 * @param rmsdB the new RMS dB value.
		 */
		@Override public void onRmsChanged(float rmsdB){}

	}
	
	
	/**
	 * When binding to the service, we return an interface to our messenger
	 * for sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent){
		Log.d("SpeechRecognitionService", "onBind");
		return mMessengerFromClients.getBinder();
	}
	
}