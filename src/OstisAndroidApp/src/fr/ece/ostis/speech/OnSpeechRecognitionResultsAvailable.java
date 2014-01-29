package fr.ece.ostis.speech;

import java.util.ArrayList;

public interface OnSpeechRecognitionResultsAvailable {

	void onSpeechRecognitionResultsAvailable(ArrayList<String> sentences);
	
}
