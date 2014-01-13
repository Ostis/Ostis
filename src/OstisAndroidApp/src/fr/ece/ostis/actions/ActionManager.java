package fr.ece.ostis.actions;

import java.util.ArrayList;

import fr.ece.ostis.lang.Language;


public class ActionManager {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	private ArrayList<Action> actionList;
	private VRComparator vrComparator;
	
	public ActionManager(){
		actionList = new ArrayList<Action>();
		vrComparator = new VRComparator(Language.Value);
	}
	
	public ArrayList<Action> getActions(){
		return actionList;
	}
	
	public void addAction(Action action){
		actionList.add(action);
	}
	
	public void removeAction(Action action){
		actionList.remove(action);
	}
	
	public void saveActions() {
		
	}
	
	public void loadActions() {
		
	}
	
	public void runCommand(String command)
	{
		for (Action a : actionList)
		{
			if (vrComparator.areSimilar(command, a.getVocalCommand()))
			{
				a.run();
				return;
			}
		}
	}
	
}
