package fr.ece.ostis;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-05
 */
public interface DroneBatteryChangedListener{

	void onDroneBatteryChanged(int level);

	void onDroneBatteryTooLow(int level);
	
}
