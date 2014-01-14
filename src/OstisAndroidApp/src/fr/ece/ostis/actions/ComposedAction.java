package fr.ece.ostis.actions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-14
 */
public class ComposedAction extends Action{
	
	/**
	 * TODO
	 */
	private ArrayList<Action> _ActionList;
	
	
	/**
	 * TODO
	 * @param _Name
	 * @param _Language
	 * @param _VocalCommand
	 */
	public ComposedAction(String _Name, String _Language, String _VocalCommand){
		super(_Name, _Language, _VocalCommand);
		_ActionList = new ArrayList<Action>();
	}

	
	/**
	 * TODO
	 * @param _Name
	 * @param _Language
	 * @param _VocalCommands
	 */
	public ComposedAction(String _Name, String _Language, HashMap<String, String> _VocalCommands){
		super(_Name, _VocalCommands);
		_ActionList = new ArrayList<Action>();
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
	 * @param _Action
	 */
	public void appendAction(Action _Action){
		_ActionList.add(_Action);
	}
	
	
	/**
	 * TODO
	 * @param _Position
	 * @param _Action
	 */
	public void addAction(int _Position, Action _Action){
		_ActionList.add(_Position, _Action);
	}
	
	
	/**
	 * TODO
	 * @param _Action
	 */
	public void removeAction(Action _Action){
		_ActionList.remove(_Action);
	}
	
	
	/**
	 * TODO
	 */
	@Override public void run(){
		for (Action a : _ActionList)
			a.run();	
	}
	
}