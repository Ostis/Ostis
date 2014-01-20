package fr.ece.ostis.actions;

import java.util.Hashtable;
import java.util.Locale;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-01-21
 */
public abstract class Action{
	
	/**
	 * TODO Docu
	 */
	protected String mId;
	protected Hashtable<Locale, String> mNameTable; // TODO Change names for different languages ?
	protected Hashtable<Locale, String> mVocalCommandTable;

	/**
	 * TODO
	 * @param id
	 * @param name
	 * @param vocalCommands
	 */
	public Action(String id) {
		mId = id;
		mNameTable = new Hashtable<Locale, String>();
		mVocalCommandTable = new Hashtable<Locale, String>();
	}
	
	/**
	 * TODO
	 * @param id
	 * @param name
	 * @param vocalCommands
	 */
	public Action(String id, Hashtable<Locale, String> names, Hashtable<Locale, String> vocalCommands) {
		mId = id;
		mNameTable = names;
		mVocalCommandTable = vocalCommands;
	}

	/**
	 * TODO
	 * @return
	 */
	public String getId(){
		return mId;
	}
	
	
	/**
	 * TODO Docu
	 * @return
	 */
	public String getName(Locale locale){
		return mNameTable.get(locale); 
	}
	
	/**
	 * TODO
	 * @param locale
	 * @return
	 */
	public Hashtable<Locale, String> getNameTable(){
		return mNameTable; 
	}
	
	
	/**
	 * TODO Docu
	 * @param name
	 */
	public void setName(Locale locale, String name){
		mNameTable.put(locale, name);
	}
	
	
	/**
	 * 
	 * @param locale
	 * @param name
	 */
	public void setNameTable(Hashtable<Locale, String> names){
		mNameTable = names;
	}
	
	
	/**
	 * 
	 * @param locale
	 * @return
	 */
	public Hashtable<Locale, String> getVocalCommandTable(){ 
		return mVocalCommandTable;
	}
	
	
	/**
	 * TODO Docu
	 * @param locale
	 * @return
	 */
	public String getVocalCommand(Locale locale){ 
		return mVocalCommandTable.get(locale);
	}
	
	
	/**
	 * TODO
	 * @param language
	 * @param vocalCommand
	 */
	public void setVocalCommand(Locale locale, String vocalCommand){ 
		mVocalCommandTable.put(locale, vocalCommand);
	}
	
	
	/**
	 * TODO
	 * @param vocalCommands
	 */
	public void setVocalCommands(Hashtable<Locale, String> vocalCommands){
		mVocalCommandTable = vocalCommands;
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
		return mId;
	}
}
