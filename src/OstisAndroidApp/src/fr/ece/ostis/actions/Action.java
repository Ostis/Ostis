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
	protected String mName; // TODO Change names for different languages ?
	protected HashMap<Locale, String> mVocalCommands = null;

	/**
	 * 
	 * @param id
	 * @param name
	 * @param vocalCommands
	 */
	public Action(int id, String name) {
		mId = id;
		mName = name;
		mVocalCommands = new HashMap<Locale, String>();
	}

	/**
	 * TODO
	 * @return
	 */
	public int getId(){
		return mId;
	}


	/**
	 * TODO
	 * @param id
	 */
	public void setId(int id){
		mId = id;
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
	 * @param name
	 */
	public void setName(String name){
		mName = name;
	}
	
	
	/**
	 * 
	 * @param locale
	 * @return
	 */
	public HashMap<Locale, String> getVocalCommands(){ 
		return mVocalCommands;
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
	 * Executes the given action.
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
