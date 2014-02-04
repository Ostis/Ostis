package fr.ece.ostis.actions.base;

import java.io.IOException;
import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-04
 */
public class MoveForwardAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public MoveForwardAction(){
		super("MoveForward");
		mNameTable.put(Locale.FRENCH, "Avancer");
		mNameTable.put(Locale.ENGLISH, "Go forward");
		mVocalCommandTable.put(Locale.FRENCH, "avance");
		mVocalCommandTable.put(Locale.ENGLISH, "go forward");
		mDescriptionTable.put(Locale.FRENCH, "Fait avancer le drone.");
		mDescriptionTable.put(Locale.ENGLISH, "Makes the drone move forward.");
	}
	
	
	@Override
	public void run(ARDrone drone, OstisService service) throws IOException{
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		
		// Move forward
        drone.move(0, -1, 0, 0);
        try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        drone.hover();
		
	}

}
