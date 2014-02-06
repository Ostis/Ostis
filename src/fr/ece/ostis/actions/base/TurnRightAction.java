package fr.ece.ostis.actions.base;

import java.io.IOException;
import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;

/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-02-06
 */
public class TurnRightAction extends BaseAction {
	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public TurnRightAction() {
		super("TurnRight");
		mNameTable.put(Locale.FRENCH, "Tourner à droite");
		mNameTable.put(Locale.ENGLISH, "Turn right");
		mVocalCommandTable.put(Locale.FRENCH, "droite");
		mVocalCommandTable.put(Locale.ENGLISH, "turn right");
		mDescriptionTable.put(Locale.FRENCH, "Fait tourner le drone à droite.");
		mDescriptionTable.put(Locale.ENGLISH, "Turns the drone right.");
	}

	@Override
	public void run(ARDrone drone, OstisService ostisService)
			throws IOException {
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		
		// Move right
        drone.move(0, 0, 0, 10);
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        drone.hover();
	}

}
