package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.UUID;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-21
 */
public class ComposedAction extends Action{
	
	/**
	 * TODO
	 */
	private ArrayList<Action> mActionList;
	
	
	/**
	 * 
	 * @param actions
	 */
	public ComposedAction(ArrayList<Action> actions){
		super(generateId());
		mActionList = actions;
	}
	
	
	/**
	 * TODO Docu
	 * @param id
	 * @param name
	 * @param vocalCommands
	 * @param actions
	 */
	public ComposedAction(String id, ArrayList<Action> actions){
		super(id);
		mActionList = actions;
	}
	
	
	/**
	 * TODO
	 * @param id
	 * @param name
	 * @param vocalCommmands
	 * @param actions
	 */
	public ComposedAction(Hashtable<Locale, String> names, Hashtable<Locale, String> vocalCommmands, Hashtable<Locale, String> descriptions, ArrayList<Action> actions){
		super(generateId(), names, descriptions, vocalCommmands);
		mActionList = actions;
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
	 * @return
	 */
	public ArrayList<String> getActionsId(){
		ArrayList<String> actionsId = new ArrayList<String>();
		for (Action action : mActionList)
			actionsId.add(action.getId());
		return actionsId;
	}
	
	
	/**
	 * TODO
	 * @param position
	 * @param action
	 */
	public void addAction(Action action, int position){
		mActionList.add(position, action);
	}
	
	
	/**
	 * TODO
	 * @param action
	 */
	public void appendAction(Action action){
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
	 * 
	 * @return
	 */
	private static String generateId(){
		return UUID.randomUUID().toString().substring(0, 8);
	}
	
	
	/**
	 * TODO
	 */
	@Override public void run(){
		for (Action action : mActionList)
			action.run();
	}
	
}