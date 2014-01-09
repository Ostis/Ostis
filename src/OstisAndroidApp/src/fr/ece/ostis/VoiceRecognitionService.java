package fr.ece.ostis;

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

public class VoiceRecognitionService extends Service{
	
	protected AudioManager mAudioManager; 
	protected SpeechRecognizer mSpeechRecognizer;
	protected Intent mSpeechRecognizerIntent;
	protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

	protected boolean mIsListening;
	protected volatile boolean mIsCountDownOn;

	static final int MSG_RECOGNIZER_START_LISTENING = 1;
	static final int MSG_RECOGNIZER_CANCEL = 2;

	@Override
	public void onCreate(){
		
		// Super
		super.onCreate();
		
		// Log
		Log.d("Ostis/VoiceService", "onCreate");
		
		// Retrieve audio manager
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		// Retrieve speech recognizer
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
		
	}

	protected static class IncomingHandler extends Handler{
		
		private WeakReference<VoiceRecognitionService> mTarget;

		IncomingHandler(VoiceRecognitionService target){
			mTarget = new WeakReference<VoiceRecognitionService>(target);
		}

		@Override
		public void handleMessage(Message msg){
			
			// Log
			Log.d("Ostis/VoiceService", "handleMessage");
			
			final VoiceRecognitionService target = mTarget.get();

			switch(msg.what){
			
				case MSG_RECOGNIZER_START_LISTENING:
				
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
						// turn off beep sound  
						target.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
					}
					if (!target.mIsListening){
						target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
						target.mIsListening = true;
						
						// Log
						Log.d("Ostis/VoiceService", "Message start listening");
					}
					break;

				 case MSG_RECOGNIZER_CANCEL:
					target.mSpeechRecognizer.cancel();
					target.mIsListening = false;
					
					// Log
					Log.d("Ostis/VoiceService", "Message canceled recognizer");
					break;
			 }
	   } 
	} 

	// Count down timer for Jelly Bean work around
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000){

		@Override
		public void onTick(long millisUntilFinished){
			
			// Log
			Log.d("Ostis/VoiceService/CountDownTimer", "onTick");
			
			// TODO Auto-generated method stub

		}

		@Override
		public void onFinish(){
			
			// Log
			Log.d("Ostis/VoiceService/CountDownTimer", "onFinish");
			
			mIsCountDownOn = false;
			Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
			try{
				mServerMessenger.send(message);
				message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
				mServerMessenger.send(message);
			}catch (RemoteException e){

			}
		}
	};

	@Override
	public void onDestroy(){
		
		// Super
		super.onDestroy();
		
		// Log
		Log.d("Ostis/VoiceService", "onDestroy");

		// Cleanup
		if (mIsCountDownOn) mNoSpeechCountDown.cancel();
		if (mSpeechRecognizer != null) mSpeechRecognizer.destroy();
	}

	protected class SpeechRecognitionListener implements RecognitionListener{

		@Override
		public void onBeginningOfSpeech(){
			
			// Log
			Log.d("Ostis/VoiceService", "onBeginningOfSpeech");
			
			// Speech input will be processed, so there is no need for count down anymore
			if (mIsCountDownOn){
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}
		}

		@Override
		public void onBufferReceived(byte[] buffer){
			
			// Log
			Log.d("Ostis/VoiceService", "onBufferReceived");
			
		}

		@Override
		public void onEndOfSpeech(){
			
			// Log
			Log.d("Ostis/VoiceService", "onEndOfSpeech");
			
		}

		@Override
		public void onError(int error){
			
			// Log
			Log.d("Ostis/VoiceService", "onError | error = " + String.valueOf(error));
			switch(error){
				case SpeechRecognizer.ERROR_AUDIO: Log.e("Ostis/VoiceService/onError", "Audio recording error."); break;
				case SpeechRecognizer.ERROR_CLIENT: Log.e("Ostis/VoiceService/onError", "Other client side errors."); break;
				case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: Log.e("Ostis/VoiceService/onError", "Insufficient permissions"); break;
				case SpeechRecognizer.ERROR_NETWORK: Log.e("Ostis/VoiceService/onError", "Other network related errors."); break;
				case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: Log.e("Ostis/VoiceService/onError", "Network operation timed out."); break;
				case SpeechRecognizer.ERROR_NO_MATCH: Log.e("Ostis/VoiceService/onError", "No recognition result matched."); break;
				case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: Log.e("Ostis/VoiceService/onError", "RecognitionService busy."); break;
				case SpeechRecognizer.ERROR_SERVER: Log.e("Ostis/VoiceService/onError", "Server sends error status."); break;
				case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: Log.e("Ostis/VoiceService/onError", "No speech input"); break;
			}
			
			if (mIsCountDownOn){
				mIsCountDownOn = false;
				mNoSpeechCountDown.cancel();
			}
			
			mIsListening = false;
			
		}

		@Override
		public void onEvent(int eventType, Bundle params){
			
			// Log
			Log.d("Ostis/VoiceService", "onEvent");

		}

		@Override
		public void onPartialResults(Bundle partialResults){
			
			// Log
			Log.d("Ostis/VoiceService", "onPartialResults");

            /*ArrayList data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String str = "";
            for (int i = 0; i < data.size(); i++){
                  Log.d("Ostis/VoiceService/onPartialResults", "result = " + data.get(i));
                  str += data.get(i);
            }*/
			
		}

		@Override
		public void onReadyForSpeech(Bundle params){
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				mIsCountDownOn = true;
				mNoSpeechCountDown.start();
				mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
			}
			
			// Log
			Log.d("Ostis/VoiceService", "onReadyForSpeech"); 
		}

		@Override
		public void onResults(Bundle results){
			
			// Log
			Log.d("Ostis/VoiceService", "onResults");
			
			
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String str = "";
            for (int i = 0; i < data.size(); i++){
                  Log.d("Ostis/VoiceService/onResults", "result = " + data.get(i));
                  str += data.get(i);
            }
            
            // Declare listening stopped
            mIsListening = false;
            //mSpeechRecognizer.cancel();
           // mSpeechRecognizer.stopListening();
		}

		@Override
		public void onRmsChanged(float rmsdB){
			Log.d("Ostis/VoiceService", "onRmsChanged | rmsdB = " + String.valueOf(rmsdB));
		}

	}

	@Override
	public IBinder onBind(Intent intent){
		Log.d("Ostis/VoiceService", "onBind");
			
		return mServerMessenger.getBinder();
	}
	
}