package fr.ece.ostis.actions.base;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.AsyncTask;
import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.DroneVideoListener;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import fr.ece.ostis.OstisService;
import fr.ece.ostis.actions.BaseAction;
import fr.ece.ostis.tracking.Tracker;


/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-02-03
 */
public class FollowMeAction extends BaseAction implements DroneVideoListener{

	
	protected AtomicBoolean mIsNewBitmapAvailable;
	
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
	public void run(ARDrone drone, OstisService ostisService) throws IOException{
		
		// Ensure drone reference has been set
		if(drone == null) throw new NullPointerException("Drone reference has not been passed properly.");
		if(ostisService == null) throw new NullPointerException("OstisService reference has not been passed properly.");
		
		// Register as image listener
		drone.addImageListener(this);
		
		(new Follower(drone, ostisService, this)).execute();
		
	}

	
	@Override
	public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize){
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
			b = Bitmap.createBitmap(rgbArray, offset, scansize, w, h, Bitmap.Config.RGB_565);
			b.setDensity(100);
			return null;
		}
		
		
		@Override
		protected void onPostExecute(Void param){
			synchronized(mBitmapLock) {
				if(mBitmap != null)
					mBitmap.recycle();
				mBitmap = b;
				mIsNewBitmapAvailable.set(true);
			}
		}
		
	}
	
	private class Follower extends AsyncTask< Void, Void, Void> {
		
		protected ARDrone mDrone;
		protected OstisService mOstisService;
		protected DroneVideoListener mDroneVideoListener;
		
		public Follower(ARDrone drone, OstisService ostisService, DroneVideoListener droneVideoListener) {
			mDrone = drone;
			mOstisService = ostisService;
			mDroneVideoListener = droneVideoListener;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			while(mOstisService.getFollowingActivated()) {
				
				if (mIsNewBitmapAvailable.get()) {
					followTag();
				}
				
				else {
					try {
						try {
							mDrone.move(0, 0, 0, 0);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void param){
			mDrone.removeImageListener(mDroneVideoListener);
		}
		
		protected final float yawTrackingP = 0.75f;
		protected final float pitchTrackingP = 0.42f;
		protected final float pitchTrackingI = 0.0011f;
		protected float pitchE = 0;
		
		protected void followTag() {
			
			synchronized (mBitmapLock) {
				if (mBitmap == null) return;
				mBitmap.copyPixelsToBuffer(mBGR565Image.getByteBuffer());
				mIsNewBitmapAvailable.set(false);
			}
			PointF tagPosition = Tracker.getTagPosition(mBGR565Image);
			
			float yawMove = 0;
			float pitchMove = 0;
			
			if(tagPosition.x > -4){
				yawMove = yawTrackingP * tagPosition.x;
				
				pitchE = pitchE + tagPosition.y;
				pitchMove = (pitchTrackingP * tagPosition.y )  + (pitchTrackingI * pitchE);
				
			}
			
			try {
				mDrone.move(0,pitchMove,0, yawMove);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
