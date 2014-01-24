package fr.ece.ostis.actions.base;

import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-24
 */
public class LandAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public LandAction(){
		super("Land");
		mNameTable.put(Locale.FRENCH, "Atterrissage");
		mNameTable.put(Locale.ENGLISH, "Land");
		mVocalCommandTable.put(Locale.FRENCH, "attéris");
		mVocalCommandTable.put(Locale.ENGLISH, "land");
		mDescriptionTable.put(Locale.FRENCH, "Permet au drone d'attérir.");
		mDescriptionTable.put(Locale.ENGLISH, "Lets the drone land.");
	}
	
	@Override public void run(ARDrone drone){
		// TODO Implement lit-off base function
	}

}
