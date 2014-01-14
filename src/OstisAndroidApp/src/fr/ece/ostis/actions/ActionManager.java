package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;

import fr.ece.ostis.speech.SpeechComparator;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-14
 */
public class ActionManager{
	
	
	/**
	 * TODO
	 */
	private ArrayList<Action> mActionList;
	private SpeechComparator mSpeechComparator;
	private Locale mLocale = null;
	protected Context mContext = null;
	
	
	/**
	 * 
	 * @param context
	 */
	public ActionManager(Locale locale, Context context){
		mLocale = locale;
		mContext = context;
		mActionList = new ArrayList<Action>();
		mSpeechComparator = new SpeechComparator(mLocale);
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public ArrayList<Action> getActions(){
		return mActionList;
	}
	
	
	/**
	 * TODO
	 * @param action
	 */
	public void addAction(Action action){
		mActionList.add(action);
	}
	
	
	/**
	 * TODO
	 * @param action
	 */
	public void removeAction(Action action){
		mActionList.remove(action);
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
	 * @param command
	 */
	public void runCommand(String command){
		
		for (Action action : mActionList){
			if (mSpeechComparator.areSimilar(command, action.getVocalCommand(mLocale))){
				action.run();
				return;
			}
		}
		
	}

	
	/**
	 * TODO
	 */
	public void saveComposedActions(){
		
	}
	
	
	/**
	 * TODO
	 */
	public void loadComposedActions(){
		
	}
	
	
	/**
	 * TODO
	 * @param id
	 * @return
	 */
	public Action getActionById(int id){
		
		for (Action action : mActionList){
			if (action.getId() == id){
				return action;
			}
		}
		
		return null; // TODO Maybe throw custom excetion ?
	}
	
}
