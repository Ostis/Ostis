package fr.ece.ostis.actions.base;

import java.io.IOException;
import java.util.Locale;

import com.codeminders.ardrone.ARDrone;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;

/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-02-05
 */
public class DesactivateTrackingAction extends BaseAction {
	
	/**
	 * TODO
	 * @param id
	 */
	public DesactivateTrackingAction(String id) {
		super("DesactivateTracking");
		mNameTable.put(Locale.FRENCH, "Arr�t du suivi");
		mNameTable.put(Locale.ENGLISH, "Desactivate tracking");
		mVocalCommandTable.put(Locale.FRENCH, "arr�t suivi");
		mVocalCommandTable.put(Locale.ENGLISH, "desactivate tracking");
		mDescriptionTable.put(Locale.FRENCH, "Ordonne au drone d'arr�ter de suivre l'utilisateur.");
		mDescriptionTable.put(Locale.ENGLISH, "Ask the drone to stop tracking the user.");
	}


	/**
	 * TODO
	 */
	@Override
	public void run(ARDrone drone, OstisService ostisService)
			throws IOException {
		ostisService.setFollowingActivated(false);
	}
}