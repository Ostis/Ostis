package fr.ece.ostis.actions.base;

import java.util.Locale;

import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-14
 */
public class ActionLand extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public ActionLand(){

		mId = 0002;
		mName = "Atterrissage";
		mVocalCommands.put(Locale.FRENCH, "attéris");
		mVocalCommands.put(Locale.ENGLISH, "land");
		
	}
	
	@Override public void run(){
		// TODO Retrieve a reference to the drone proxy
		// TODO Implement lit-off base function
	}

}
