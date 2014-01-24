package fr.ece.ostis.actions.base;

import java.io.IOException;
import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-24
 */
public class EmergencyAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public EmergencyAction(){
		super("Emergency");
		mNameTable.put(Locale.FRENCH, "Signal d'urgence");
		mNameTable.put(Locale.ENGLISH, "Emergency signal");
		mVocalCommandTable.put(Locale.FRENCH, "arret d'urgence");
		mVocalCommandTable.put(Locale.ENGLISH, "emergency stop");
		mDescriptionTable.put(Locale.FRENCH, "Arret d'urgence.");
		mDescriptionTable.put(Locale.ENGLISH, "Emergency stop.");
	}
	
	@Override public void run(ARDrone drone) throws IOException{
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		
		// TODO Implement lit-off base function
		drone.sendEmergencySignal();
		
	}

}
