package fr.ece.ostis.actions;

import java.util.HashMap;
import java.util.Locale;

/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-14
 */
public abstract class Action{
	
	
	/**
	 * TODO Docu
	 */
	protected int mId;
	protected String mName;
	private HashMap<Locale, String> mVocalCommands = null;
	
	
	/**
	 * TODO Docu
	 * @param id
	 * @param name
	 * @param vocalCommands
	 */
	public Action(int id, String name, HashMap<Locale, String> vocalCommands){
		this.mId = id;
		this.mName = name;
		this.mVocalCommands = vocalCommands;
	}


	public int getId() {
		return mId;
	}


	public void setId(int id) {
		this.mId = id;
	}	
	
	
	/**
	 * TODO Docu
	 * @return
	 */
	public String getName(){
		return mName; 
	}
	
	/**
	 * TODO Docu
	 * @param _Name
	 */
	public void setName(String _Name){
		this.mName = _Name;
	}
	
	
	/**
	 * TODO Docu
	 * @param locale
	 * @return
	 */
	public String getVocalCommand(Locale locale){ 
		return mVocalCommands.get(locale);
	}
	
	
	/**
	 * TODO
	 * @param language
	 * @param vocalCommand
	 */
	public void setVocalCommand(Locale locale, String vocalCommand){ 
		mVocalCommands.put(locale, vocalCommand);
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
		return mName;
	}
}
