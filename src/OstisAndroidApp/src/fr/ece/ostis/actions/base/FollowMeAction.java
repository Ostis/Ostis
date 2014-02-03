package fr.ece.ostis.actions.base;

import java.io.IOException;
import java.util.Locale;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;

import fr.ece.ostis.actions.BaseAction;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-03
 */
public class FollowMeAction extends BaseAction implements DroneVideoListener{

	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public FollowMeAction(){
		super("FollowMe");
		mNameTable.put(Locale.FRENCH, "Suis moi");
		mNameTable.put(Locale.ENGLISH, "Follow me");
		mVocalCommandTable.put(Locale.FRENCH, "suis moi");
		mVocalCommandTable.put(Locale.ENGLISH, "follow me");
		mDescriptionTable.put(Locale.FRENCH, "Permet au drone de suivre.");
		mDescriptionTable.put(Locale.ENGLISH, "Lets the drone follow the user.");
	}
	
	
	@Override
	public void run(ARDrone drone) throws IOException{
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		
		// Register as image listener
		drone.addImageListener(this); // TODO remove image listener on end of task
		
		// TODO Implement follow-me base function
		
	}

	
	@Override
	public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize){
		// TODO Auto-generated method stub
		
	}

}
