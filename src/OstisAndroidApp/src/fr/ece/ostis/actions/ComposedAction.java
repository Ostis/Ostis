package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-14
 */
public abstract class ComposedAction extends Action{
	
	/**
	 * TODO
	 */
	private ArrayList<Action> mActionList;
	
	
	/**
	 * TODO Docu
	 * @param id
	 * @param name
	 * @param vocalCommands
	 */
	public ComposedAction(int id, String name, HashMap<Locale, String> vocalCommands){
		mId = id;
		mName = name;
		mVocalCommands = vocalCommands;
		mActionList = new ArrayList<Action>();
	}
	
	
	/**
	 * TODO Docu
	 * @param id
	 * @param name
	 * @param vocalCommands
	 * @param actions
	 */
	public ComposedAction(int id, String name, HashMap<Locale, String> vocalCommands, ArrayList<Action> actions){
		mId = id;
		mName = name;
		mVocalCommands = vocalCommands;
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
	 * TODO
	 */
	@Override public void run(){
		for (Action action : mActionList)
			action.run();
	}
	
}