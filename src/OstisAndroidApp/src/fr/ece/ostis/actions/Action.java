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
	protected Hashtable<Locale, String> mNameTable;
	protected Hashtable<Locale, String> mVocalCommandTable;
	protected Hashtable<Locale, String> mDescriptionTable;

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
		mDescriptionTable = new Hashtable<Locale, String>();
	}
	
	/**
	 * TODO
	 * @param id
	 * @param name
	 * @param vocalCommands
	 */
	public Action(String id, Hashtable<Locale, String> names, Hashtable<Locale, String> vocalCommands, Hashtable<Locale, String> descriptions) {
		mId = id;
		mNameTable = names;
		mVocalCommandTable = vocalCommands;
		mDescriptionTable = descriptions;
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
	 * TODO Docu
	 * @param name
	 */
	public void setName(Locale locale, String name){
		mNameTable.put(locale, name);
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
	 * 
	 * @param locale
	 * @param name
	 */
	public void setNameTable(Hashtable<Locale, String> names){
		mNameTable = names;
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
	 * 
	 * @param locale
	 * @return
	 */
	public Hashtable<Locale, String> getVocalCommandTable(){ 
		return mVocalCommandTable;
	}
	
	
	/**
	 * TODO
	 * @param vocalCommands
	 */
	public void setVocalCommandTable(Hashtable<Locale, String> vocalCommands){
		mVocalCommandTable = vocalCommands;
	}
	
	
	/**
	 * TODO Docu
	 * @param locale
	 * @return
	 */
	public String getDescription(Locale locale){ 
		return mDescriptionTable.get(locale);
	}
	
	
	/**
	 * TODO
	 * @param locale
	 * @param description
	 */
	public void setDescription(Locale locale, String description){ 
		mDescriptionTable.put(locale, description);
	}

	
	/**
	 * 
	 * @param locale
	 * @return
	 */
	public Hashtable<Locale, String> getDescriptionTable(){ 
		return mDescriptionTable;
	}
	
	
	/**
	 * TODO
	 * @param descriptionTable
	 */
	public void setDescriptionTable(Hashtable<Locale, String> descriptionTable){
		mDescriptionTable = descriptionTable;
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
