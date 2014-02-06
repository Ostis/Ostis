package fr.ece.ostis.tracking;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;


import android.graphics.PointF;
import android.util.Log;

import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;


/**
 * TODO
 * @author Paul Bouillon
 * @version 2014-02-04
 */
public class Tracker {
	
    static int hueLowerR = 150;
    static int hueUpperR = 165;
    
    public static final int AR_VIDEO_WIDTH = 320;
    public static final int AR_VIDEO_HEIGHT = 240;
    
    static CvMat rgbImage = CvMat.create(AR_VIDEO_HEIGHT, AR_VIDEO_WIDTH, CV_8UC3);
    static CvMat hsvImage = CvMat.create(AR_VIDEO_HEIGHT, AR_VIDEO_WIDTH, CV_8UC3);
    static CvMat thresholdImage = CvMat.create(AR_VIDEO_HEIGHT, AR_VIDEO_WIDTH, CV_8UC1);
    
    public static PointF getTagPosition(IplImage bgr565Image) {

        cvCvtColor(bgr565Image, rgbImage, CV_BGR5652RGB);
        
        generateThresholdImage();
        PointF position = getCoordinates();
        
        //cvCvtColor(thresholdImage, bgr565Image, CV_GRAY2BGR565);
        //image.copyPixelsFromBuffer(bgr565Image.getByteBuffer());
        
        Log.d("Tracker", "Position calculated: " + position.toString());
        
        return position;
    }

    static PointF getCoordinates() {
        float posX = 0;
        float posY = 0;
        CvMoments moments = new CvMoments();
        
        cvMoments(thresholdImage, moments, 1);
        
        double momX10 = cvGetSpatialMoment(moments, 1, 0);
        double momY01 = cvGetSpatialMoment(moments, 0, 1);
        double area = cvGetCentralMoment(moments, 0, 0);
        
        posX = (float) (momX10 / area) / AR_VIDEO_WIDTH * 2 - 1;
        posY = (float) (momY01 / area) / AR_VIDEO_HEIGHT * 2 - 1;
        
        return new PointF(Float.isNaN(posX) ? -5 : posX, Float.isNaN(posY) ? -5 : posY);
    }

    static void generateThresholdImage() {
        cvCvtColor(rgbImage, hsvImage, CV_RGB2HSV);
        cvInRangeS(hsvImage, cvScalar(hueLowerR, 100, 100, 0), cvScalar(hueUpperR, 255, 255, 0), thresholdImage);
        cvSmooth(thresholdImage, thresholdImage, CV_MEDIAN, 13);
        Log.d("Tracker", "Threshold image generated !");
    }
}
