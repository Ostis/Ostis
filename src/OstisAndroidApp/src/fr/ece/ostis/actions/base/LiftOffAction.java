package fr.ece.ostis.actions.base;

import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-24
 */
public class LiftOffAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public LiftOffAction(){
		super("liftOff");
		mNameTable.put(Locale.FRENCH, "Decollage");
		mNameTable.put(Locale.ENGLISH, "LiftOff");
		mVocalCommandTable.put(Locale.FRENCH, "décolle");
		mVocalCommandTable.put(Locale.ENGLISH, "lift-off");
		mDescriptionTable.put(Locale.FRENCH, "Permet au drone de decoller.");
		mDescriptionTable.put(Locale.ENGLISH, "Lets the drone lift off.");
	}
	
	@Override public void run(ARDrone drone){
		// TODO Implement lift-off base function
	}

}
