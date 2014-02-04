package fr.ece.ostis.speech;

import java.util.ArrayList;

public interface SpeechRecognitionResultsListener {

	void onSpeechRecognitionResultsAvailable(ArrayList<String> sentences);
	
	void onReadyForSpeech();
	
	void onEndOfSpeech();
	
	void onError();
	
}
