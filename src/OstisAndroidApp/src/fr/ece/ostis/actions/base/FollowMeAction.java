package fr.ece.ostis.actions.base;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import com.codeminders.ardrone.ARDrone;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import fr.ece.ostis.DroneFrameReceivedListener;
import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;
import fr.ece.ostis.tracking.Tracker;


/**
 * Special action that follows the user, based on a particular color, defined in Tracker.
 * Work left to do is : move code of Tracker here, and call ensureAltitude() periodically.
 * @author Paul Bouillon
 * @version 2014-02-05
 */
public class FollowMeAction extends BaseAction implements DroneFrameReceivedListener{

	
	/** Log tag. */
	protected static final String mTag = "FollowMeAction";
	
	
	/** Reference to the drone. */
	protected ARDrone mDrone = null;
	
	
	/** Reference to the service. */
	protected OstisService mService = null;
	
	
	/** Reference to the thread. */
	protected Thread mThread = null;
	
	
	/** Atomic flag to store if a new bitmap has been made available since last treatment. */
	protected AtomicBoolean mIsNewBitmapAvailable;
	
	protected AtomicBoolean mIs;
	protected Bitmap mBitmap;
	protected IplImage mBGR565Image;
	protected final Object mBitmapLock = new Object();
	
	
	/**
	 * Public constructor which sets the values for this base action.
	 */
	public FollowMeAction(){
		super("FollowMe");
		mNameTable.put(Locale.FRENCH, "Suis moi");
		mNameTable.put(Locale.ENGLISH, "Follow me");
		mVocalCommandTable.put(Locale.FRENCH, "suis moi");
		mVocalCommandTable.put(Locale.ENGLISH, "follow me");
		mDescriptionTable.put(Locale.FRENCH, "Permet au drone de suivre.");
		mDescriptionTable.put(Locale.ENGLISH, "Lets the drone follow the user.");
		
		mIsNewBitmapAvailable = new AtomicBoolean(false);
		mBGR565Image = cvCreateImage(new CvSize(Tracker.AR_VIDEO_WIDTH, Tracker.AR_VIDEO_HEIGHT), 8, 2);
	}
	
	
	@Override
	public void run(ARDrone drone, OstisService service) throws IOException{
		
		// Store references
		mDrone = drone;
		mService = service;
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		if(service == null) throw new NullPointerException("OstisService reference has not been passed properly.");
		
		// Register as image listener
		mService.registerFrameReceivedListener(this);
		
		// Set flag
		service.setFollowingActivated(true);
		
		// Create and start separate thread
		mThread = new Thread(new Runnable(){ 
			public void run(){
				
				// First step : go up
				ensureAltitude();
				
				// Second step : follow
				follow();
			}
		});
		mThread.start();
		
	}
	
	protected void ensureAltitude(){
		float lastAltitude = -1;
		while(mService.getFollowingActivated()){
			try{
				mDrone.move(0, 0, 0.5f, 0);
				if(mService.getDroneAltitude() <= lastAltitude){mDrone.move(0, 0, 0, 0);}
				lastAltitude = mService.getDroneAltitude();
				if(mService.getDroneAltitude() >= 1){mDrone.move(0, 0, 0, 0); break;}
				Thread.sleep(100);
			}catch(IOException e){
				e.printStackTrace();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * TODO
	 */
	@Override
	public void onDroneFrameReceived(Bitmap b) {
		// TODO Auto-generated method stub
		synchronized(mBitmapLock) {
			Log.d(mTag, "Setting new Bitmap");
			mBitmap = b;
		}
		mIsNewBitmapAvailable.set(true);
	}
	
	
	/**
	 * TODO
	 */
	protected final float yawTrackingP = 1f;
	protected final float pitchTrackingP = -0.082f;
	protected long time = -1;
	
	
	/**
	 * TODO
	 */
	protected void follow(){
		while(mService.getFollowingActivated()){
			if (mIsNewBitmapAvailable.get()){
				Log.d(mTag, "New image, launching follow action ...");
				followTag();
				time = -1;
			}else{
				Log.d(mTag, "No new image, don't move !");
				if (time < 0) {
					time = System.currentTimeMillis();
				}else if(System.currentTimeMillis() - time > 100){
					try{
						mDrone.move(0, 0, 0, 0);
						// TODO Add mDrone.hover ?
					}catch(IOException e){}
				}else if(System.currentTimeMillis() - time > 200){
					time = -1;
					try{
						mDrone.hover();
					}catch(IOException e){}
				}
				try{
					Thread.sleep(10);
				}catch(InterruptedException e){}
			}
		}
		mService.unregisterFrameReceivedListener(this);
	}
	
	
	/**
	 * TODO
	 */
	protected void followTag(){
		
		synchronized (mBitmapLock) {
			Log.d(mTag, "Creating IplImage");
			if (mBitmap == null) return;
			mBitmap.copyPixelsToBuffer(mBGR565Image.getByteBuffer());
		}
		mIsNewBitmapAvailable.set(false);
		PointF tagPosition = Tracker.getTagPosition(mBGR565Image);
		
		float yawMove = 0;
		float pitchMove = 0;
		
		if(tagPosition.x > -4){
			Log.d(mTag, "Tag detected !! Position: " + tagPosition.toString());
			yawMove = yawTrackingP * tagPosition.x;
			
			if (tagPosition.y > -0.3f)
				pitchMove = pitchTrackingP;
			
			try {
				Log.d(mTag, "Moving !");
				mDrone.move(0, pitchMove, 0, yawMove);
			} catch (IOException e) {}
		}else{
			try{
				Log.d(mTag, "Hovering");
				mDrone.move(0, 0, 0, 0);
				mDrone.hover();
			}catch(IOException e) {}
		}
		
	}

}
