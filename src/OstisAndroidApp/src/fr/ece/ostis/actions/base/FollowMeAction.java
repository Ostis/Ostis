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
 * TODO
 * @author Paul Bouillon
 * @version 2014-02-05
 */
public class FollowMeAction extends BaseAction implements DroneFrameReceivedListener{

	
	protected AtomicBoolean mIsNewBitmapAvailable;
	protected Bitmap mBitmap;
	protected IplImage mBGR565Image;
	protected final Object mBitmapLock = new Object();
	
	protected ARDrone mDrone;
	protected OstisService mOstisService;
	
	
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
	public void run(ARDrone drone, OstisService ostisService) throws IOException{
		
		mDrone = drone;
		mOstisService = ostisService;
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		if(ostisService == null) throw new NullPointerException("OstisService reference has not been passed properly.");
		
		ostisService.setFollowingActivated(true);
		
		// Register as image listener
		mOstisService.registerFrameReceivedListener(this);
		
		final DroneFrameReceivedListener droneFrameRL = this;
		
		new Thread(
			new Runnable() { 
				public void run() {
					follow(mDrone, mOstisService, droneFrameRL); 
				}
			}).start();
		
	}
	
	/**
	 * TODO
	 */
	@Override
	public void onDroneFrameReceived(Bitmap b) {
		// TODO Auto-generated method stub
		synchronized(mBitmapLock) {
			Log.d("FollowMeAction", "Setting new Bitmap");
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
	 * @param drone
	 * @param ostisService
	 * @param droneFrameRL
	 */
	protected void follow(ARDrone drone, OstisService ostisService, DroneFrameReceivedListener droneFrameRL){
		while(ostisService.getFollowingActivated()) {
			
			if (mIsNewBitmapAvailable.get()) {
				Log.d("FollowMeAction", "New image, launching follow action");
				followTag(drone);
				time = -1;
			} else {
				Log.d("FollowMeAction", "No new image : don't move !");
				if (time < 0) {
					time = System.currentTimeMillis();
				}
				else if (System.currentTimeMillis() - time > 100) {
					try {
						drone.move(0, 0, 0, 0);
						// TODO Add mDrone.hover ?
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if (System.currentTimeMillis() - time > 200) {
					time = -1;
					try {
						drone.hover();
						// TODO Add mDrone.hover ?
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {		
					Thread.sleep(10);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			
		}
		ostisService.unregisterFrameReceivedListener(droneFrameRL);
	}
	
	/**
	 * TODO
	 * @param drone
	 */
	protected void followTag(ARDrone drone) {
		
		synchronized (mBitmapLock) {
			Log.d("Follower", "Creating IplImage");
			if (mBitmap == null) return;
			mBitmap.copyPixelsToBuffer(mBGR565Image.getByteBuffer());
		}
		mIsNewBitmapAvailable.set(false);
		PointF tagPosition = Tracker.getTagPosition(mBGR565Image);
		
		float yawMove = 0;
		float pitchMove = 0;
		
		if(tagPosition.x > -4){
			Log.d("FollowMeAction", "Tag detected !! Position: " + tagPosition.toString());
			yawMove = yawTrackingP * tagPosition.x;
			
			if (tagPosition.y > -0.3f)
				pitchMove = pitchTrackingP;
			
			try {
				Log.d("FollowMeAction", "Moving !");
				drone.move(0, pitchMove, 0, yawMove);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				Log.d("FollowMeAction", "Hovering");
				drone.move(0, 0, 0, 0);
				drone.hover();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
