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
 * @version 2014-01-13
 */
public class SpeechRecognitionService extends Service{
	
	
	/**
	 *  Audio and speech recognition related variables.
	 */
	protected AudioManager _AudioManager; 
	protected SpeechRecognizer _SpeechRecognizer;
	protected Intent _SpeechRecognizerIntent;
	protected boolean _IsListening;
	protected volatile boolean _IsCountDownOn;
	
	
	/**
	 * List of client messengers.
	 */
	protected ArrayList<Messenger> _Clients = new ArrayList<Messenger>();
	
	
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger _Messenger = new Messenger(new IncomingHandler(this));
	
	
	/**
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
		_AudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		// Retrieve speech recognizer
		_SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		_SpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
		_SpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		_SpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		_SpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
		
	}
	

	/**
	 * TODO
	 */
	@Override public void onDestroy(){
		
		// Log
		Log.d("SpeechRecognitionService", "onDestroy");

		// Cleanup
		if (_IsCountDownOn) _NoSpeechCountDown.cancel();
		if (_SpeechRecognizer != null) _SpeechRecognizer.destroy();
		
		// Super
		super.onDestroy();
		
	}

	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-13
	 */
	protected static class IncomingHandler extends Handler{
		
		
		// Retrieving the speech recognition service reference
		private final WeakReference<SpeechRecognitionService> _ServiceReference; 
		IncomingHandler(SpeechRecognitionService service) {
			_ServiceReference = new WeakReference<SpeechRecognitionService>(service);
		}
		
		
		/**
		 * TODO
		 * @param _Message
		 */
		@Override public void handleMessage(Message _Message){
			
			// Log
			Log.d("SpeechRecognitionService", "handleMessage | what = " + String.valueOf(_Message.what));

			// Retrieve the service instance
			SpeechRecognitionService _Service = _ServiceReference.get();
			
			// React according to the type of object of the message
			switch(_Message.what){

				// Register a new client
				case MSG_REGISTER_CLIENT:
					Log.d("SpeechRecognitionService", "handleMessage -> New client registered");
					_Service._Clients.add(_Message.replyTo);
					break;
					
				// Unregister a client
				case MSG_UNREGISTER_CLIENT:
					Log.d("SpeechRecognitionService", "handleMessage -> Removing existing client");
					_Service._Clients.remove(_Message.replyTo);
					break;
			
				// Start listening in background
				case MSG_START_LISTENING:
				
					// Workaround to turn off beep sound
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
						_Service._AudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
					}
					
					// Start listening
					if (!_Service._IsListening){
						Log.d("SpeechRecognitionService", "handleMessage -> Canceling listening (auto)");
						_Service._SpeechRecognizer.cancel();
						Log.d("SpeechRecognitionService", "handleMessage -> Starting listening");
						_Service._SpeechRecognizer.startListening(_Service._SpeechRecognizerIntent);
						_Service._IsListening = true;
					}
					
					break;

				 case MSG_CANCEL_LISTENING:
					Log.d("SpeechRecognitionService", "handleMessage -> Canceling listening");
					_Service._SpeechRecognizer.cancel();
					_Service._IsListening = false;
					break;
			}
			
		}
		
	}
	

	/**
	 * Count down timer for Jelly Bean work around.
	 */
	protected CountDownTimer _NoSpeechCountDown = new CountDownTimer(5000, 5000){

		@Override public void onTick(long millisUntilFinished){
			
			// Log
			Log.d("SpeechRecognitionService/CountDownTimer", "onTick");

		}

		@Override public void onFinish(){
			
			// Log
			Log.d("SpeechRecognitionService/CountDownTimer", "onFinish");
			
			// Reset flag
			_IsCountDownOn = false;
			
			try{
				
				// Cancel current listening
				Message _Message = Message.obtain(null, MSG_CANCEL_LISTENING);
				_Messenger.send(_Message);
				
				// Launch new listening
				_Message = Message.obtain(null, MSG_START_LISTENING);
				_Messenger.send(_Message);
				
			}catch(RemoteException e){
				e.printStackTrace();
			}
			
		}
		
	};


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
			
			// Speech input will be processed, so there is no need for count down anymore
			if (_IsCountDownOn){
				_IsCountDownOn = false;
				if(_NoSpeechCountDown != null) _NoSpeechCountDown.cancel();
			}
			
		}

		
		/**
		 * Called whenever more sound has been received. We could use it
		 * to provide audio feedback to the user, but we don't want that.
		 * @param _Buffer a buffer containing a sequence of big-endian 16-bit integers representing a single channel audio stream.
		 */
		@Override public void onBufferReceived(byte[] _Buffer){ }

		
		/**
		 * Called after the user stops speaking.
		 */
		@Override public void onEndOfSpeech(){
			
			// Log
			Log.d("SpeechRecognitionService", "onEndOfSpeech");
			
		}

		
		/**
		 * TODOerror
		 * @param params
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
			}
			
			// Stop the countdown timer
			if (_IsCountDownOn){
				_IsCountDownOn = false;
				if(_NoSpeechCountDown != null) _NoSpeechCountDown.cancel();
			}
			
			// Set the not listening flag
			_IsListening = false;
			
			try{
				
				// Start listening again
				Message _Message = Message.obtain(null, MSG_START_LISTENING);
				_Messenger.send(_Message);
				
			}catch (RemoteException e){
				e.printStackTrace();
			}
			
			
		}

		
		/**
		 * Currently not in use, reserved by Android for adding future events.
		 * @param _EventType the type of the occurred event.
		 * @param _Params a Bundle containing the passed parameters.
		 */
		@Override public void onEvent(int _EventType, Bundle _Params){ }

		
		/**
		 * Called when partial recognition results are available. We will not be using
		 * this feature since Google almost always ignores requests for partial results.
		 * @param _PartialResults
		 */
		@Override public void onPartialResults(Bundle _PartialResults){
			
			// Log
			Log.d("SpeechRecognitionService", "onPartialResults");
			
		}
		
		
		/**
		 * Called when the endpointer is ready for the user to start speaking.
		 * @param _Params
		 */
		@Override public void onReadyForSpeech(Bundle _Params){
			
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				_IsCountDownOn = true;
				_NoSpeechCountDown.start();
				_AudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
			}
			
			// Log
			Log.d("SpeechRecognitionService", "onReadyForSpeech");
			
		}
		
		
		/**
		 * TODO
		 * @param _Results
		 */
		@Override public void onResults(Bundle _Results){
			
			// Log
			Log.d("SpeechRecognitionService", "onResults");
			
			// Extract results
			ArrayList<String> _Sentences = _Results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			float[] _Scores = _Results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

			// Log
			for (int i = 0; i < _Sentences.size(); i++){
				Log.d("SpeechRecognitionService", "onResults | " + _Sentences.get(i) + " (" + String.valueOf(_Scores[i])+")");
			}
			
			// Set the not listening flag
			_IsListening = false;
			
			try{
				
				// Start listening again
				Message _Message = Message.obtain(null, MSG_START_LISTENING);
				_Messenger.send(_Message);
				
			}catch (RemoteException e){
				e.printStackTrace();
			}
			
			// Send results to clients
			Bundle _MessageBundle = new Bundle();
			_MessageBundle.putStringArrayList("SpeechRecognitionResult", _Sentences);
			for(int _ClientsIterator = _Clients.size() - 1; _ClientsIterator >= 0; _ClientsIterator--) {
				try{
					
					// Prepare a message and send it
					Message _Message = Message.obtain(null, MSG_FINISHED_WITH_RESULTS);
					_Message.setData(_MessageBundle);
					_Clients.get(_ClientsIterator).send(_Message);
					
				}catch(RemoteException _Exception){
					
					// Log
					_Exception.printStackTrace();
					
	                // The client is dead. Remove it from the list. We are going through the list from back to front so this is safe to do inside the loop.
					_Clients.remove(_ClientsIterator);
					
				}
			}
			
		}

		
		/**
		 * TODO
		 * @param rmsdB
		 */
		@Override public void onRmsChanged(float rmsdB){}

	}
	
	
	/**
	 * When binding to the service, we return an interface to our messenger
	 * for sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent _intent){
		Log.d("SpeechRecognitionService", "onBind");
		return _Messenger.getBinder();
	}
	
}