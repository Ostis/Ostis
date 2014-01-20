package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import fr.ece.ostis.actions.base.LiftOffAction;
import fr.ece.ostis.speech.SpeechComparator;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-21
 */
public class ActionManager{
	
	
	/**
	 * TODO
	 */
	private Hashtable<String, ComposedAction> mComposedActionTable;
	private Hashtable<String, BaseAction> mBaseActionTable;
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
		mComposedActionTable = new Hashtable<String, ComposedAction>();
		mBaseActionTable = new Hashtable<String, BaseAction>();
		mSpeechComparator = new SpeechComparator(mLocale);
		
		// initialize/add baseActions here
		BaseAction liftOffAction = new LiftOffAction();
		mBaseActionTable.put(liftOffAction.getId(), liftOffAction);
		//
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Hashtable<String, ComposedAction> getComposedActionTable(){
		return mComposedActionTable;
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<ComposedAction> getComposedActions(){
		return mComposedActionTable.values();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Hashtable<String, BaseAction> getBaseActionTable(){
		return mBaseActionTable;
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<BaseAction> getBaseActions(){
		return mBaseActionTable.values();
	}
	
	
	/**
	 * TODO
	 * @param ids
	 * @return
	 */
	public ArrayList<Action> getActionsByIds(ArrayList<String> ids){
		ArrayList<Action> actions = new ArrayList<Action>();
		for (String id : ids)
			actions.add(getAction(id));
		return actions;
	}

	
	/**
	 * TODO
	 * @param id
	 * @return
	 */
	public Action getAction(String id){
		ComposedAction composedAction = mComposedActionTable.get(id);
		BaseAction baseAction = mBaseActionTable.get(id);
		if (composedAction != null) return composedAction;
		else return baseAction;
	}
	
	
	/**
	 * TODO
	 * @param action
	 */
	public void addComposedAction(ComposedAction composedAction){
		mComposedActionTable.put(composedAction.getId(), composedAction);
	}
	
	
	/**
	 * TODO
	 * @param action
	 */
	public void removeComposedAction(ComposedAction composedAction){
		mComposedActionTable.remove(composedAction.getId());
	}
	
	
	/**
	 * TODO
	 * @param id
	 */
	public void removeComposedAction(String id){
		mComposedActionTable.remove(id);
	}
	
	
	/**
	 * TODO
	 * @param command
	 */
	public void runCommand(String command){
		for (Action action : mBaseActionTable.values()){
			if (mSpeechComparator.areSimilar(command, action.getVocalCommand(mLocale))){
				action.run();
				return;
			}
		}
		for (Action action : mComposedActionTable.values()){
			if (mSpeechComparator.areSimilar(command, action.getVocalCommand(mLocale))){
				action.run();
				return;
			}
		}
	}

	/////////////
	public String test = "";
	////////////////
	/**
	 * TODO
	 */
	public boolean saveComposedActions(){
		try {
			JSONArray jsonActions = new JSONArray();
			for (ComposedAction composedAction : mComposedActionTable.values()){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", composedAction.getId());
				jsonObject.put("nameKeys", new JSONArray(composedAction.getNameTable().keySet()));
				jsonObject.put("nameValues", new JSONArray(composedAction.getNameTable().values()));
				jsonObject.put("vocalCommandKeys", new JSONArray(composedAction.getVocalCommandTable().keySet()));
				jsonObject.put("vocalCommandValues", new JSONArray(composedAction.getVocalCommandTable().values()));
				jsonObject.put("actionIds", new JSONArray(composedAction.getActionsId()));
				jsonActions.put(jsonObject);
			}
			
			Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
			preferencesEditor.putString("composedActions", jsonActions.toString());
			preferencesEditor.commit();
			test = jsonActions.toString();///////////////
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
	public boolean loadComposedActions(){
		try {
			mComposedActionTable.clear();
			
			String actionsString = PreferenceManager.getDefaultSharedPreferences(mContext).getString("composedActions", "nothing");
			test = actionsString;/////////
			JSONArray jsonComposedActions = new JSONArray(actionsString);
			int numberOfActions = jsonComposedActions.length();
			
			for (int i = 0; i < numberOfActions; i++)
			{
				JSONObject jsonObject = (JSONObject) jsonComposedActions.get(i);
				String id = jsonObject.getString("id");
				JSONArray jsonActionsId = jsonObject.getJSONArray("actionIds");
				ArrayList<Action> actionsId= new ArrayList<Action>();
				for (int j = 0; j < jsonActionsId.length(); j++)
					actionsId.add(getAction(jsonActionsId.getString(j)));
				ComposedAction composedAction = new ComposedAction(id, actionsId);
				JSONArray jsonKeys = jsonObject.getJSONArray("vocalCommandKeys");
				JSONArray jsonValues = jsonObject.getJSONArray("vocalCommandValues");
				for (int j = 0; j < jsonKeys.length(); j++)
					composedAction.setVocalCommand(new Locale(jsonKeys.getString(j)), jsonValues.getString(j));
				jsonKeys = jsonObject.getJSONArray("nameKeys");
				jsonValues = jsonObject.getJSONArray("nameValues");
				for (int j = 0; j < jsonKeys.length(); j++)
					composedAction.setName(new Locale(jsonKeys.getString(j)), jsonValues.getString(j));
				addComposedAction(composedAction);
			}
			return true;
		}
		catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
