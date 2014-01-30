package fr.ece.ostis.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-29
 */
public abstract class NetworkManager{
	
	
	/**
	 * Transform host name in int value used by {@link ConnectivityManager.requestRouteToHost} method
	 * @param hostname
	 * @return the translation of the hostname to an integer
	 * @throws UnknownHostException if the host doesn't exist.
	 */
	public int lookupHost(String hostname) throws UnknownHostException{
		
		InetAddress inetAddress = InetAddress.getByName(hostname);
		
		Log.d("NetworkManager", "Host " + hostname + " > " + inetAddress.toString());
		
		byte[] addrBytes;
		int addrInt;
		
		addrBytes = inetAddress.getAddress();
		addrInt = ((addrBytes[3] & 0xff) << 24)
				| ((addrBytes[2] & 0xff) << 16)
				| ((addrBytes[1] & 0xff) << 8 )
				|  (addrBytes[0] & 0xff);
		
		return addrInt;
		
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-30
	 */
	public class InvokeFailedException extends Exception{
		private static final long serialVersionUID = 5725839131023631430L;
		public InvokeFailedException(){ super(); }
		public InvokeFailedException(String detailMessage){ super(detailMessage); }
	}
	
}


