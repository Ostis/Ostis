package fr.ece.ostis;

public interface DroneStatusChangedListener {

	
	abstract void onDroneConnected();
	
	
	abstract void onDroneConnectionFailed();
	
	
	abstract void onDroneDisconnected();
	
	
}
