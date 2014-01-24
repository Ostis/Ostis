package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.UUID;

import com.codeminders.ardrone.ARDrone;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-23
 */
public class ComposedAction extends Action{
	
	
	/** TODO */
	private ArrayList<Action> mActionList;
	
	
	/**
	 * TODO
	 * @param actions
	 */
	public ComposedAction(){
		super(generateId());
		mActionList = new ArrayList<Action>();
	}
	
	
	/**
	 * TODO Docu
	 * @param id
	 * @param name
	 * @param vocalCommands
	 * @param actions
	 */
	public ComposedAction(String id){
		super(id);
		mActionList = new ArrayList<Action>();
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
	 * @param actionList
	 */
	public void setActions(ArrayList<Action> actionList){
		mActionList = actionList;
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public ArrayList<String> getActionsId(){
		ArrayList<String> actionsId = new ArrayList<String>();
		for (Action action : mActionList)
			if (action != null)
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
	 * TODO
	 * @return
	 */
	private static String generateId(){
		return UUID.randomUUID().toString().substring(0, 8);
	}
	
	
	/**
	 * TODO
	 * @param drone
	 */
	@Override public void run(ARDrone drone){
		for(Action action : mActionList)
			if(action != null)
				action.run(drone);
	}
	
}