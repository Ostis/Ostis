package fr.ece.ostis.actions;

import fr.ece.ostis.lang.Language;


public abstract class Action {
	
	private String name;
	private String[] vocalCommand;
	
	public Action(String name, String vocalCom) {
		this.name = name;
		vocalCommand[Language.Value] = vocalCom;
	}
	
	public String getName() { return name; }
	public void setName(String nom) { name = nom; }
	
	public String getVocalCommand() { 
		return vocalCommand[Language.Value]; 
	}
	public void setVocalCommand(String vocalCom) { 
		vocalCommand[Language.Value] = vocalCom; 
	}
	
	public abstract void run();
	
	@Override
	public String toString() {
		return name;
	}	
}
