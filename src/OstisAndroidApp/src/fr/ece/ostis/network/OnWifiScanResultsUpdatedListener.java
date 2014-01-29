package fr.ece.ostis.network;

import java.util.List;

import android.net.wifi.ScanResult;

public interface OnWifiScanResultsUpdatedListener {

	void onWifiScanResultsUpdated(List<ScanResult> wifiList);
	
}
