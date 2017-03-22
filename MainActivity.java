package com.example.ritwick.opencvtest3;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.appwidget.*;

import android.nfc.Tag;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.solve;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {


    private JavaCameraView mOpenCvCameraView;
    Mat imgGray, imgThreshold, imgConture, heirarchy,mrgba;


    private BaseLoaderCallback mBaseloaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.MainActivityCamera);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mBaseloaderCallBack);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mrgba = new Mat(height,width,CvType.CV_8UC4);
        imgGray = new Mat(height, width, CvType.CV_8SC1);
        imgThreshold = new Mat(height, width, CvType.CV_8SC1);
        imgConture = new Mat(height, width, CvType.CV_8SC1);


    }

    @Override
    public void onCameraViewStopped() {
        imgGray.release();
        imgThreshold.release();
        mrgba.release();


    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        imgGray = inputFrame.gray();
        Imgproc.adaptiveThreshold(imgGray, imgThreshold, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 11, 12);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        heirarchy = new Mat();
        Imgproc.findContours(imgGray, contours, heirarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
    /* Mat drawing = Mat.zeros( mIntermediateMat.size(), CvType.CV_8UC3 );
     for( int i = 0; i< contours.size(); i++ )
     {
    Scalar color =new Scalar(Math.random()*255, Math.random()*255, Math.random()*255);
     Imgproc.drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, new Point() );
     }*/


        heirarchy.release();
        Imgproc.drawContours(imgThreshold, contours, -1, new Scalar(Math.random() * 255, Math.random() * 255, Math.random() * 255));//, 2, 8, hierarchy, 0, new Point());
        // Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        double maxArea = 0;
        MatOfPoint largestContour = null;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                largestContour = contour;
            }
        }
        Rect boundingRect = Imgproc.boundingRect(largestContour);

        Mat lines = new Mat();
        Imgproc.HoughLinesP(imgThreshold,lines,1,Math.PI/180,80,30,10);

        for (int x = 0; x < lines.rows(); x++)
        {
            double[] vec = lines.get(x, 0);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            double dx = x1 - x2;
            double dy = y1 - y2;

            double dist = Math.sqrt (dx*dx + dy*dy);

            if(dist>300.d)  // show those lines that have length greater than 300
                Imgproc.line(imgGray, start, end, new Scalar(0,255, 0, 255),5);

        }
        return imgThreshold;
        /*Imgproc.findContours(imgThreshold,contours,heirarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(imgThreshold, contours, contourIdx, new Scalar(0, 0, 255), -1);
        }*/


    }


}
