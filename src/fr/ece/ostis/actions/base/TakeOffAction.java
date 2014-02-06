package fr.ece.ostis.actions.base;

import java.io.IOException;
import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-24
 */
public class TakeOffAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public TakeOffAction(){
		super("TakeOff");
		mNameTable.put(Locale.FRENCH, "Decollage");
		mNameTable.put(Locale.ENGLISH, "TakeOff");
		mVocalCommandTable.put(Locale.FRENCH, "décollage");
		mVocalCommandTable.put(Locale.ENGLISH, "takeoff");
		mDescriptionTable.put(Locale.FRENCH, "Permet au drone de decoller.");
		mDescriptionTable.put(Locale.ENGLISH, "Lets the drone lift off.");
	}
	
	@Override public void run(ARDrone drone, OstisService ostisService) throws IOException{
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		
		// TODO Implement lift-off base function
        drone.clearEmergencySignal();
        drone.trim();
        drone.waitForReady(5000);
		drone.takeOff();
		drone.hover();
		
	}

}
