package fr.ece.ostis.actions.base;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.Log;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;
import fr.ece.ostis.tracking.Tracker;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-02-04
 */
public class FollowMeAction extends BaseAction implements DroneVideoListener{

	
	protected AtomicBoolean mIsNewBitmapAvailable;
	protected AtomicBoolean mIs;
	protected Bitmap mBitmap;
	protected IplImage mBGR565Image;
	protected final Object mBitmapLock = new Object();
	
	protected ARDrone mDrone;
	protected OstisService mOstisService;
	//protected final Thread 
	
	
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
		drone.addImageListener(this);
		
		final DroneVideoListener dvl = this;
		// TODO
		//(new Follower(drone, ostisService, this)).execute();
		
		new Thread(
			new Runnable() { 
				public void run() {
					follow(mDrone, mOstisService, dvl); 
				}
			}).start();
		
	}

	
	@Override
	public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize){
		Log.d("BitmapCreator", "New frame received");
		(new BitmapCreator(startX, startY, w, h, rgbArray, offset, scansize)).execute(); 
	}
	

	protected class BitmapCreator extends AsyncTask<Void, Integer, Void>{
		
		public Bitmap b;
		public int[]rgbArray;
		public int offset;
		public int scansize;
		public int w;
		public int h;

		
		public BitmapCreator(int x, int y, int width, int height, int[] arr, int off, int scan){
			super();
			rgbArray = arr;
			offset = off;
			scansize = scan;
			w = width;
			h = height;
		}
		
		
		@Override
		protected Void doInBackground(Void... params){
			Log.d("BitmapCreator", "Doing work...");
			b = Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
			b.setDensity(100);
			Log.d("BitmapCreator", "Work finished !");
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void param){
			Log.d("BitmapCreator", "Entering onPostExecute");
			synchronized(mBitmapLock) {
				Log.d("BitmapCreator", "Into Lock");
				if(mBitmap != null) mBitmap.recycle();
				mBitmap = b;
			}
			mIsNewBitmapAvailable.set(true);
			Log.d("FollowMeAction", "New Bitmap generated !");
		}
		
	}
	
	protected final float yawTrackingP = 0.75f;
	protected final float pitchTrackingP = 0.42f;
	protected final float pitchTrackingI = 0.0011f;
	protected float pitchE = 0;
	
	protected void follow(ARDrone drone, OstisService ostisService, DroneVideoListener droneVideoListener){
		while(ostisService.getFollowingActivated()) {
			
			if (mIsNewBitmapAvailable.get()) {
				Log.d("FollowMeAction", "Analizing image");
				followTag(drone);
			} else {
				Log.d("FollowMeAction", "No new image : don't move !");
				try {
					try {
						drone.move(0, 0, 0, 0);
						// TODO Add mDrone.hover ?
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Thread.sleep(30);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			
		}
		drone.removeImageListener(droneVideoListener);
	}
	
	protected void followTag(ARDrone drone) {
		
		synchronized (mBitmapLock) {
			Log.d("Follower", "Into Lock");
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
			
			pitchE = pitchE + tagPosition.y;
			pitchMove = (pitchTrackingP * tagPosition.y )  + (pitchTrackingI * pitchE);
		}
		
		try {
			Log.d("FollowMeAction", "Moving !");
			drone.move(0, pitchMove, 0, yawMove);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO
	 * @author Paul Bouillon
	 * @version 2014-02-04
	 */
	/*
	protected class Follower extends AsyncTask<Void, Void, Void> {
		
		protected ARDrone mDrone;
		protected OstisService mOstisService;
		protected DroneVideoListener mDroneVideoListener;
		
		protected final float yawTrackingP = 0.75f;
		protected final float pitchTrackingP = 0.42f;
		protected final float pitchTrackingI = 0.0011f;
		protected float pitchE = 0;
		
		

		public Follower(ARDrone drone, OstisService ostisService, DroneVideoListener droneVideoListener){
			mDrone = drone;
			mOstisService = ostisService;
			mDroneVideoListener = droneVideoListener;
		}
		
		
		@Override
		protected Void doInBackground(Void... params) {
			while(mOstisService.getFollowingActivated()) {
				
				if (mIsNewBitmapAvailable.get()) {
					Log.d("FollowMeAction", "Analizing image");
					followTag();
				} else {
					Log.d("FollowMeAction", "No new image : don't move !");
					try {
						try {
							mDrone.move(0, 0, 0, 0);
							// TODO Add mDrone.hover ?
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Thread.sleep(30);
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
				
			}
			return null;
		}
		
		
		
		
		protected void followTag() {
			
			synchronized (mBitmapLock) {
				Log.d("Follower", "Into Lock");
				if (mBitmap == null) return;
				mBitmap.copyPixelsToBuffer(mBGR565Image.getByteBuffer());
			}
			mIsNewBitmapAvailable.set(false);
			PointF tagPosition = Tracker.getTagPosition(mBGR565Image);
			
			float yawMove = 0;
			float pitchMove = 0;
			
			if(tagPosition.x > -4){
				yawMove = yawTrackingP * tagPosition.x;
				
				pitchE = pitchE + tagPosition.y;
				pitchMove = (pitchTrackingP * tagPosition.y )  + (pitchTrackingI * pitchE);
			}
			
			try {
				Log.d("FollowMeAction", "Moving !");
				mDrone.move(0, pitchMove, 0, yawMove);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		@Override
		protected void onPostExecute(Void param){
			mDrone.removeImageListener(mDroneVideoListener);
			Log.d("FollowMeAction", "Stopping following action");
		}
		
	}*/
}
