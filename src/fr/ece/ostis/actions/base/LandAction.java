package fr.ece.ostis.actions.base;

import java.io.IOException;
import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public class LandAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public LandAction(){
		super("Land");
		mNameTable.put(Locale.FRENCH, "Atterrissage");
		mNameTable.put(Locale.ENGLISH, "Land");
		mVocalCommandTable.put(Locale.FRENCH, "atterrissage");
		mVocalCommandTable.put(Locale.ENGLISH, "land");
		mDescriptionTable.put(Locale.FRENCH, "Permet au drone d'atterrir.");
		mDescriptionTable.put(Locale.ENGLISH, "Lets the drone land.");
	}
	
	@Override public void run(ARDrone drone, OstisService ostisService) throws IOException{
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		
		// TODO Implement lit-off base function
		drone.land();
		
	}

}
