package fr.ece.ostis.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-31
 */
public class TelnetManager{

	
	/** Log tag. */
	protected static final String mTag = "TelnetManager";
	
	
	/**
	 * Code taken from the official parrot ar.drone api.
	 * @param host
	 * @param port
	 * @param command
	 * @return
	 */
	public static boolean executeRemotely(String host, int port, String command){
		
		Socket socket = null;
		OutputStream out = null;
		
		try {
			socket = new Socket(host, port);
			out = socket.getOutputStream();
			out.write(command.getBytes());
			out.flush();
	        return true;
		}catch(UnknownHostException e){
			Log.w(mTag, e);
			return false;
		}catch (IOException e){
			Log.w(mTag, e);
			return false;
		}finally{
			if(out != null){
				try{
					out.close();
				}catch(IOException e){
					Log.w(mTag, e);
				}
			}
			if(socket != null && !socket.isClosed()){
				try{
					socket.close();
				}catch(IOException e){
					Log.w(mTag, e);
				}
			}
		}
	}
	
}
