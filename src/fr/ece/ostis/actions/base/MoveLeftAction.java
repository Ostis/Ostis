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
public class MoveLeftAction extends BaseAction {

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public MoveLeftAction(){
		super("MoveLeft");
		mNameTable.put(Locale.FRENCH, "Tourner à gauche");
		mNameTable.put(Locale.ENGLISH, "Turn left");
		mVocalCommandTable.put(Locale.FRENCH, "virage à gauche");
		mVocalCommandTable.put(Locale.ENGLISH, "turn left");
		mDescriptionTable.put(Locale.FRENCH, "Fait tourner le drone à gauche.");
		mDescriptionTable.put(Locale.ENGLISH, "Turns the drone left.");
	}
	
	
	@Override
	public void run(ARDrone drone, OstisService service) throws IOException{
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		
		// Move left
        drone.move(0, 0, 0, -10);
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        drone.hover();
		
	}

}
