package com.skwangles;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.BORDER_DEFAULT;
import static org.opencv.core.Core.convertScaleAbs;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgproc.Imgproc.*;

import org.opencv.imgproc.*;
import org.opencv.imgcodecs.*;

import java.util.Arrays;
import java.util.List;

/**
 * A simple class that demonstrates/tests the usage of the OpenCV library in
 * Java. It prints a 3x3 identity matrix and then converts a given image in gray
 * scale.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @since 2013-10-20
 *
 */
public class HelloCV
{
    public static void main(String[] args)
    {
        int kernel_size = 3;
        int scale = 1;
        int delta = 0;
        int ddepth = CV_16S;
        // load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // create and print on screen a 3x3 identity matrix
        System.out.println("Create a 3x3 identity matrix...");
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());

        // prepare to convert a RGB image in gray scale
        String location = "RIDB/IM000001_1.jpg";
        String location2 = "RIDB/IM000002_2.jpg";
        String location3 = "RIDB/IM000003_1.jpg";
        System.out.println("Convert the image at " + location + " in gray scale... ");
        // get the jpeg image from the internal resource folder
        Mat srcBase = Imgcodecs.imread(location);
        Mat srcTest1 = Imgcodecs.imread(location2);
        Mat srcTest2 = Imgcodecs.imread(location3);

        if (srcBase.empty() || srcTest1.empty() || srcTest2.empty()) {
            System.err.println("Cannot read the images");
            System.exit(0);
        }
        Mat hsvBase = new Mat(), hsvTest1 = new Mat(), hsvTest2 = new Mat();
        Imgproc.cvtColor( srcBase, hsvBase, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( srcTest1, hsvTest1, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( srcTest2, hsvTest2, Imgproc.COLOR_BGR2HSV );
        int hBins = 50, sBins = 60;
        int[] histSize = { hBins, sBins };
        // hue varies from 0 to 179, saturation from 0 to 255
        float[] ranges = { 0, 180, 0, 256 };
        // Use the 0-th and 1-st channels
        int[] channels = { 0, 1 };
        Mat histBase = new Mat(), histTest1 = new Mat(), histTest2 = new Mat();
        List<Mat> hsvBaseList = Arrays.asList(hsvBase);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(channels), new Mat(), histBase, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histBase, histBase, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvTest1List = Arrays.asList(hsvTest1);
        Imgproc.calcHist(hsvTest1List, new MatOfInt(channels), new Mat(), histTest1, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histTest1, histTest1, 0, 1, Core.NORM_MINMAX);
        List<Mat> hsvTest2List = Arrays.asList(hsvTest2);
        Imgproc.calcHist(hsvTest2List, new MatOfInt(channels), new Mat(), histTest2, new MatOfInt(histSize), new MatOfFloat(ranges), false);
        Core.normalize(histTest2, histTest2, 0, 1, Core.NORM_MINMAX);
        for( int compareMethod = 0; compareMethod < 4; compareMethod++ ) {
            double baseBase = Imgproc.compareHist( histBase, histBase, compareMethod );
            double baseTest1 = Imgproc.compareHist( histBase, histTest1, compareMethod );
            double baseTest2 = Imgproc.compareHist( histBase, histTest2, compareMethod );
            System.out.println("Method " + compareMethod + " Perfect, Base-Test(1), Base-Test(2) : " + baseBase + " / " + baseTest1 + " / " + baseTest2);
        }
        System.out.println("Done!");
    }
}