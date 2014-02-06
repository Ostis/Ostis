package fr.ece.ostis.speech;

import java.util.ArrayList;

import fr.ece.ostis.ui.NetworkWizardActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;


/**
 * TODO
 * @see http://stackoverflow.com/questions/14940657/android-speech-recognition-as-a-service-on-android-4-1-4-2/14950616#14950616 for the Jelly been workaround, based on the work of Hoan Nguyen.
 * @author Nicolas Schurando
 * @version 2014-01-30
 */
public class SpeechRecognitionManager{
	
	
    /** Log tag. */
    protected static final String mTag = "SpeechRecognitionService";
    
    
	/** TODO */
	protected Context mContext = null;
	
    
	/** Audio manager for mute hack. */
	protected AudioManager mAudioManager; 
	
	
	/** Speech recognizer. */
	protected SpeechRecognizer mSpeechRecognizer;
	
	
	/** Speech recognizer intent contains the parameters of the recognition. */
	protected Intent mSpeechRecognizerIntent;
	
	
	/** Is listening flag. */
	protected boolean mIsListening = false;
	
	
	/** Is countdown on flag. */
	protected volatile boolean mIsCountDownOn;
	
	
	/** List of client callbacks. */
	protected ArrayList<SpeechRecognitionResultsListener> mResultsAvailableCallbacks = new ArrayList<SpeechRecognitionResultsListener>();
	
	
	/** Count down timer for Jelly Bean work around. */
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(1000, 1000){

		@Override public void onTick(long millisUntilFinished){ }

		@Override public void onFinish(){
			
			// Log
			//Log.d(mTag + "/CountDownTimer", "onFinish");
			
			// Reset flags
			mIsCountDownOn = false;

			// Cancel current listening
			mSpeechRecognizer.cancel();

			// Launch new listening
			if(mIsListening){
				startListening();
			}
			
		}
		
	};

	
	/**
	 * TODO
	 */
	public SpeechRecognitionManager(Context context){
		
		// Retrieve context
		mContext = context;
		
		// Retrieve audio manager
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		
		// Retrieve speech recognizer
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
		mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
		
		// Create speech recognizer intent
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
		
	}

	
	/**
	 * TODO
	 * @param callback
	 */
	public void registerCallback(SpeechRecognitionResultsListener callback){
		mResultsAvailableCallbacks.add(callback);
	}
	
	
	/**
	 * TODO
	 * @param callback
	 */
	public void unregisterCallback(SpeechRecognitionResultsListener callback){
		mResultsAvailableCallbacks.remove(callback);
	}
	

	/**
	 * TODO
	 */
	public void startListening(){
		
		// Should be started on main thread
		if(Looper.myLooper() != Looper.getMainLooper()){
			Handler mainHandler = new Handler(mContext.getMainLooper());
			Runnable myRunnable = new Runnable(){
				@Override
				public void run(){
					startListening();
				}
			};
			mainHandler.post(myRunnable);
			return;
		}
		
		// Workaround to turn off beep sound
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
			mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
		}
		
		// Start listening
		//if (!mIsListening){
			Log.i(mTag, "Starting listening");
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
			mIsListening = true;
			
			// Start the countdown timer for workaround
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				mIsCountDownOn = true;
				mNoSpeechCountDown.start();
				mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
			}
			
		/*}else{
			Log.w(mTag, "Could not start listening, already listening !");
		}*/
		
	}
	
	
	/**
	 * TODO
	 */
	public void cancelListening(){
		
		// Log
		Log.i(mTag, "Canceling listening");
		
		// Stop the countdown timer
		if (mIsCountDownOn){
			mIsCountDownOn = false;
			if(mNoSpeechCountDown != null) mNoSpeechCountDown.cancel();
		}
		
		// Stop speech recognition
		mSpeechRecognizer.cancel();
		mIsListening = false;
		
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-29
	 */
	protected class SpeechRecognitionListener implements RecognitionListener{

		
		/** Log tag */
		protected final static String mTag = "SpeechRecognitionListener";
		

		@Override
		public void onBeginningOfSpeech(){
			//Log.d(mTag, "onBeginningOfSpeech");
		}

		
		@Override
		public void onBufferReceived(byte[] buffer){ }

		
		@Override
		public void onEndOfSpeech(){
			//Log.d(mTag, "onEndOfSpeech");
			
			// Warn clients
			for (SpeechRecognitionResultsListener callback: mResultsAvailableCallbacks)
				if(callback != null) callback.onError();
		}

		
		/**
		 * TODO
		 * @param error
		 */
		@Override
		public void onError(int error){
			Log.i(mTag, "Speech recognition error.");
			switch(error){
				case SpeechRecognizer.ERROR_AUDIO: Log.w(mTag, "Audio recording error."); break;
				case SpeechRecognizer.ERROR_CLIENT: Log.w(mTag, "Other client side errors."); break;
				case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: Log.w(mTag, "Insufficient permissions"); break;
				case SpeechRecognizer.ERROR_NETWORK: Log.w(mTag, "Other network related errors."); break;
				case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.w(mTag, "Network operation timed out."); break;
				case SpeechRecognizer.ERROR_NO_MATCH: Log.w(mTag, "No recognition result matched."); break;
				case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: Log.w(mTag, "RecognitionService busy."); break;
				case SpeechRecognizer.ERROR_SERVER: Log.w(mTag, "Server sends error status."); break;
				case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: Log.w(mTag, "No speech input"); break;
				default: Log.w(mTag, "Unknown error " + String.valueOf(error)); break;
			}
			
			// Stop the countdown timer
			if (mIsCountDownOn){
				mIsCountDownOn = false;
				if(mNoSpeechCountDown != null) mNoSpeechCountDown.cancel();
			}

			// Cancel current listening
			mSpeechRecognizer.cancel();
			
			// Warn clients
			for (SpeechRecognitionResultsListener callback: mResultsAvailableCallbacks)
				if(callback != null) callback.onError();
			
			// Start listening again
			startListening();
			
		}


		@Override
		public void onEvent(int eventType, Bundle params){ }

		
		@Override
		public void onPartialResults(Bundle partialResults){
			//Log.i(mTag, "onPartialResults");
		}
		
		
		@Override
		public void onReadyForSpeech(Bundle params){
			//Log.d(mTag, "onReadyForSpeech");
			
			// Speech input will be processed, so there is no need for count down anymore
			if (mIsCountDownOn){
				mIsCountDownOn = false;
				if(mNoSpeechCountDown != null) mNoSpeechCountDown.cancel();
			}
			
			// Warn clients
			for (SpeechRecognitionResultsListener callback: mResultsAvailableCallbacks)
				if(callback != null) callback.onReadyForSpeech();
			
		}
		
		
		@Override
		public void onResults(Bundle results){
			
			// Log
			Log.i(mTag, "Speech recognition results available.");
			
			// Extract results
			ArrayList<String> sentences = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

			// Cancel current listening
			cancelListening();
			
			// Start listening again
			startListening();
			
			// Prevent null pointer exception
			if(sentences == null || scores == null) return;

			// Log
			for (int i = 0; i < sentences.size(); i++){
				Log.d(mTag, " `-> " + sentences.get(i) + " (" + String.valueOf(scores[i])+")");
			}
			
			// Send results to callbacks
			for (SpeechRecognitionResultsListener callback: mResultsAvailableCallbacks)
				if(callback != null) callback.onSpeechRecognitionResultsAvailable(sentences);
			
		}

		
		@Override
		public void onRmsChanged(float rmsdB){}

	}
	
}