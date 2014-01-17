package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

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
	public boolean saveComposedActions(){
		try {
			JSONObject jsonActions = new JSONObject();
			int numberOfActions = 0;
			for (Action action : mActionList){
				if (action instanceof ComposedAction){
					ComposedAction composedAction = (ComposedAction)action;
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", composedAction.getId());
					jsonObject.put("name", composedAction.getName());
					jsonObject.put("vocalCommands", new JSONObject((Map<Locale, String>)composedAction.getVocalCommands()));
					//jsonObject.put
					//jsonActions.put("action" + numberOfActions, jsonObject);
				}
				numberOfActions++;
			}
			jsonActions.put("numberOfActions", numberOfActions);
			return true;
		}
		catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
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
