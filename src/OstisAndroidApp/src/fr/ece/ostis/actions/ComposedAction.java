package fr.ece.ostis.actions;

import java.util.ArrayList;


public  class ComposedAction extends Action{
	private ArrayList<Action> actionList;
	
	public ComposedAction(String name, String vocalCommand) {
		super(name, vocalCommand);
		actionList = new ArrayList<Action>();
	}

	public ArrayList<Action> getActions() {
		return actionList;
	}
	
	public void addAction(Action action){
		actionList.add(action);
	}
	
	public void removeAction(Action action){
		actionList.remove(action);
	}
	
	@Override
	public void run() {
		for (Action a : actionList)
			a.run();	
	}	
}