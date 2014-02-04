package fr.ece.ostis;

public interface OnDroneStatusChangedListener {

	
	abstract void onDroneConnected();
	
	
	abstract void onDroneConnectionFailed();
	
	
	abstract void onDroneDisconnected();
	
	
}
