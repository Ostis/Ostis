package fr.ece.ostis.actions;

import java.util.HashMap;

/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-14
 */
public abstract class Action{
	
	/**
	 * TODO
	 */
	private String _Name;
	
	
	/**
	 * TODO
	 */
	private HashMap<String, String> _VocalCommands = null;
	
	
	/**
	 * TODO
	 * @param _Name
	 * @param _VocalCommands
	 */
	public Action(String _Name, HashMap<String, String> _VocalCommands){
		this._Name = _Name;
		this._VocalCommands = _VocalCommands;
	}
	
	
	/**
	 * TODO
	 * @param _Name
	 * @param _VocalCommand
	 */
	public Action(String _Name, String _Language, String _VocalCommand){
		this._Name = _Name;
		this._VocalCommands = new HashMap<String, String>();
		this._VocalCommands.put(_Language, _VocalCommand);
	}
	
	
	/**
	 * TODO
	 * @return
	 */
	public String getName(){
		return _Name; 
	}
	
	
	/**
	 * TODO
	 * @param _Name
	 */
	public void setName(String _Name){
		this._Name = _Name;
	}
	
	
	/**
	 * 
	 * @param _Language
	 * @return
	 */
	public String getVocalCommand(String _Language){ 
		return _VocalCommands.get(_Language);
	}
	
	
	/**
	 * 
	 * @param _Language
	 * @param _VocalCommand
	 */
	public void setVocalCommand(String _Language, String _VocalCommand){ 
		_VocalCommands.put(_Language, _VocalCommand);
	}
	
	
	/**
	 * TODO
	 */
	public abstract void run();
	
	
	/**
	 * TODO
	 * @return A string containing the name of the action.
	 */
	@Override public String toString(){
		return _Name;
	}	
}
