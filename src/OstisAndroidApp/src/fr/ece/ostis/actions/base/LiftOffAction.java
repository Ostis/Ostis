package fr.ece.ostis.actions.base;

import java.util.Locale;

import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-21
 */
public class LiftOffAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public LiftOffAction(){
		super("liftOff");
		mNameTable.put(Locale.FRENCH, "Decollage");
		mNameTable.put(Locale.ENGLISH, "LiftOff");
		mVocalCommandTable.put(Locale.FRENCH, "d�colle");
		mVocalCommandTable.put(Locale.ENGLISH, "lift-off");
	}
	
	@Override public void run(){
		// TODO Retrieve a reference to the drone proxy
		// TODO Implement lift-off base function
	}

}
