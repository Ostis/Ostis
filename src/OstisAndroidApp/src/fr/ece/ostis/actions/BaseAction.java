package fr.ece.ostis.actions;

import fr.ece.ostis.OstisService;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-14
 */
public abstract class BaseAction extends Action {
	/**
	 * 
	 * @param id
	 * @param name
	 * @param vocalCommands
	 */
	public BaseAction(String id, OstisService ostisService) {
		super(id, ostisService);
	}
}
