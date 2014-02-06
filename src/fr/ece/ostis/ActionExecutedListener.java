package fr.ece.ostis;

import fr.ece.ostis.actions.Action;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public interface ActionExecutedListener {

	abstract void onActionExecuted(Action action);
	
}
