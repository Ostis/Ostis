package fr.ece.ostis.actions.base;

import java.util.Locale;

import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-14
 */
public class ActionLiftoff extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public ActionLiftoff(){
		super(0001, "Decollage");
		mVocalCommands.put(Locale.FRENCH, "d�colle");
		mVocalCommands.put(Locale.ENGLISH, "lit-off");
		
	}
	
	@Override public void run(){
		// TODO Retrieve a reference to the drone proxy
		// TODO Implement lit-off base function
	}

}
