package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.codeminders.ardrone.ARDrone;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import fr.ece.ostis.actions.base.EmergencyAction;
import fr.ece.ostis.actions.base.LandAction;
import fr.ece.ostis.actions.base.TakeOffAction;
import fr.ece.ostis.speech.SpeechComparator;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-24
 */
public class ActionManager{
	
	
	/** TODO */
	private Hashtable<String, ComposedAction> mComposedActionTable;
	
	
	/** TODO */
	private Hashtable<String, BaseAction> mBaseActionTable;
	
	
	/** TODO */
	private SpeechComparator mSpeechComparator;
	
	
	/** TODO */
	private Locale mLocale;
	
	
	/** TODO */
	private Context mContext;
	
	
	/**
	 * TODO
	 * @param context
	 * @param locale
	 * @param drone
	 */
	public ActionManager(Context context, Locale locale){
		
		mLocale = locale;
		mContext = context;
		mComposedActionTable = new Hashtable<String, ComposedAction>();
		mBaseActionTable = new Hashtable<String, BaseAction>();
		mSpeechComparator = new SpeechComparator(mLocale);
		
		/*
		 * TODO : Declare/initialize/add baseActions here
		 */
		BaseAction liftOffAction = new TakeOffAction();
		mBaseActionTable.put(liftOffAction.getId(), liftOffAction);
		BaseAction landAction = new LandAction();
		mBaseActionTable.put(landAction.getId(), landAction);
		BaseAction emergencyAction = new EmergencyAction();
		mBaseActionTable.put(emergencyAction.getId(), emergencyAction);
		
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public Hashtable<String, ComposedAction> getComposedActionTable(){
		return mComposedActionTable;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public Collection<ComposedAction> getComposedActions(){
		return mComposedActionTable.values();
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public Hashtable<String, BaseAction> getBaseActionTable(){
		return mBaseActionTable;
	}
	
	
	/**
	 * TODO
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
	public ArrayList<Action> getActions(ArrayList<String> ids){
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
		if (baseAction != null) return baseAction;
		else return composedAction;
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
	 * @return True if a command has been executed, or false otherwise.
	 */
	public Action matchCommandToRun(String command){
		try{
			for (Action action : mBaseActionTable.values()){
				Log.d("ActionManager", "runCommand : " + command + " <-> " + action.getVocalCommand(mLocale));
				if (mSpeechComparator.areSimilar(command, action.getVocalCommand(mLocale))){
					//action.run(mDrone);
					//Log.d("ActionManager", "runCommand : " + command);
					return action;
				}
			}
			for (Action action : mComposedActionTable.values()){
				Log.d("ActionManager", "runCommand : " + command + " <-> " + action.getVocalCommand(mLocale));
				if (mSpeechComparator.areSimilar(command, action.getVocalCommand(mLocale))){
					//action.run(mDrone);
					//Log.d("ActionManager", "runCommand : " + command);
					return action;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	
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
				jsonObject.put("descriptionKeys", new JSONArray(composedAction.getDescriptionTable().keySet()));
				jsonObject.put("descriptionValues", new JSONArray(composedAction.getDescriptionTable().values()));
				jsonObject.put("actionIds", new JSONArray(composedAction.getActionsId()));
				jsonActions.put(jsonObject);
			}
			
			Editor preferencesEditor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
			preferencesEditor.putString("composedActions", jsonActions.toString());
			preferencesEditor.commit();
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
		try{
			mComposedActionTable.clear();
			String actionsString = PreferenceManager.getDefaultSharedPreferences(mContext).getString("composedActions", "nothing");
			JSONArray jsonComposedActions = new JSONArray(actionsString);
			int numberOfActions = jsonComposedActions.length();
			Hashtable<String, ArrayList<String>> corresTable = new Hashtable<String, ArrayList<String>>();
			
			for (int i = 0; i < numberOfActions; i++){
				JSONObject jsonObject = (JSONObject) jsonComposedActions.get(i);
				String id = jsonObject.getString("id");
				JSONArray jsonActionsId = jsonObject.getJSONArray("actionIds");
				ArrayList<String> actionsId= new ArrayList<String>();
				for (int j = 0; j < jsonActionsId.length(); j++)
					actionsId.add(jsonActionsId.getString(j));
				corresTable.put(id, actionsId);
				ComposedAction composedAction = new ComposedAction(id);
				JSONArray jsonKeys = jsonObject.getJSONArray("vocalCommandKeys");
				JSONArray jsonValues = jsonObject.getJSONArray("vocalCommandValues");
				for (int j = 0; j < jsonKeys.length(); j++)
					composedAction.setVocalCommand(new Locale(jsonKeys.getString(j)), jsonValues.getString(j));
				jsonKeys = jsonObject.getJSONArray("nameKeys");
				jsonValues = jsonObject.getJSONArray("nameValues");
				for (int j = 0; j < jsonKeys.length(); j++)
					composedAction.setName(new Locale(jsonKeys.getString(j)), jsonValues.getString(j));
				jsonKeys = jsonObject.getJSONArray("descriptionKeys");
				jsonValues = jsonObject.getJSONArray("descriptionValues");
				for (int j = 0; j < jsonKeys.length(); j++)
					composedAction.setDescription(new Locale(jsonKeys.getString(j)), jsonValues.getString(j));
				addComposedAction(composedAction);
			}
			ArrayList<String> idsList = new ArrayList<String>(corresTable.keySet());
			ArrayList<ArrayList<String>> actionIdsList = new ArrayList<ArrayList<String>>(corresTable.values());
			for (int i = 0; i < idsList.size(); i++)
				((ComposedAction)getAction(idsList.get(i))).setActions(getActions(actionIdsList.get(i)));
			return true;
		}catch(JSONException e){
			e.printStackTrace();
			return false;
		}
		
	}
	
}
