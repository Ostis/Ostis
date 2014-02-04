package fr.ece.ostis.ui;

import java.io.IOException;

import com.codeminders.ardrone.DroneVideoListener;

import fr.ece.ostis.R;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

/**
 * TODO
 * @author Nicolas Schurando
 * @version 2014-01-31
 */
public class FlyActivity extends ConnectedActivity implements DroneVideoListener{

	
	/** Log tag. */
	protected static final String mTag = "FlyActivity";

	
	/** Image view for displaying camera feed. */
	protected ImageView mImageViewCamera = null; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		// Super
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.activity_fly);
		
		// Find controls
		mImageViewCamera = (ImageView) findViewById(R.id.imageViewCamera);
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fly, menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_emergency_stop:
				try {
					mService.getDrone().sendEmergencySignal();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}


	@Override
	protected void onBoundToOstisService(){
		// TODO Auto-generated method stub
		
		// Register as video receiver
		mService.getDrone().addImageListener(this);
		
		// Activate speech results <-> actio matching
		mService.activateSpeechResultsToActionMatching();
		
	}


	@Override
	protected void onUnboundFromOstisService(){
		// TODO Auto-generated method stub
		
	}


	@Override
	public void frameReceived(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize){
		//if (isVisible){
			(new CameraDisplayer(startX, startY, w, h, rgbArray, offset, scansize)).execute(); 
		//}
	}
	
	
	/**
	 * TODO
	 * @author Nicolas Schurando
	 * @version 2014-01-31
	 */
	protected class CameraDisplayer extends AsyncTask<Void, Integer, Void>{
		
		public Bitmap b;
		public int[]rgbArray;
		public int offset;
		public int scansize;
		public int w;
		public int h;

		
		public CameraDisplayer(int x, int y, int width, int height, int[] arr, int off, int scan){
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
			if(mImageViewCamera != null){
				((BitmapDrawable) mImageViewCamera.getDrawable()).getBitmap().recycle(); 
				mImageViewCamera.setImageBitmap(b);
			}else{
				Log.w(mTag, "Could not find image view to display camera feed.");
			}
		}
		
	}
	
}