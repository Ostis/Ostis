package fr.ece.ostis.actions;

import java.util.ArrayList;

import fr.ece.ostis.lang.Language;
import fr.ece.ostis.speech.SpeechComparator;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-14
 */
public class ActionManager {
	
	
	/**
	 * TODO
	 */
	private ArrayList<Action> _ActionList;
	
	
	/**
	 * TODO
	 */
	private SpeechComparator _SpeechComparator;
	
	
	/**
	 * 
	 */
	private String _Language = null; // TODO Implement, and change.
	
	
	/**
	 * TODO
	 */
	public ActionManager(){
		_ActionList = new ArrayList<Action>();
		_SpeechComparator = new SpeechComparator(Language.Value);
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public ArrayList<Action> getActions(){
		return _ActionList;
	}
	
	
	/**
	 * TODO
	 * @param action
	 */
	public void addAction(Action action){
		_ActionList.add(action);
	}
	
	
	/**
	 * TODO
	 * @param action
	 */
	public void removeAction(Action action){
		_ActionList.remove(action);
	}
	
	
	/**
	 * TODO
	 */
	public void saveActions() {
		
	}
	
	
	/**
	 * TODO
	 */
	public void loadActions() {
		
	}
	
	
	/**
	 * TODO
	 * @param _Command
	 */
	public void runCommand(String _Command){
		
		for (Action _Action : _ActionList){
			if (_SpeechComparator.areSimilar(_Command, _Action.getVocalCommand(_Language))){
				_Action.run();
				return;
			}
		}
		
	}
	
}
