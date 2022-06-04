package com.skwangles;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;


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
        //Pipeline
        //Remember to clean up the image with smoothing filters
        // • Choose a sensible colour channel or space to work in
        // • Possible contrast equalization to ensure that variances in illumination don’t mess up the results
        // • Pick image transformations (e.g. edges, thresholded, etc) and features
        // • Hopefully get something that works


        // load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // prepare to convert a RGB image in gray scale
        String location = "RIDB/IM000003_8.jpg";
        String location2 = "RIDB/IM000002_8.jpg";
        System.out.println("Convert the image at " + location + " in gray scale... ");
        // get the jpeg image from the internal resource folder
        Mat src1 = Imgcodecs.imread(location);
        Mat src2 = Imgcodecs.imread(location2);

        if (src1.empty() || src2.empty()) {
            System.err.println("Cannot read the images");
            System.exit(0);
        }


        cvtColor(src1, src1, COLOR_BGR2GRAY);
        cvtColor(src2, src2, COLOR_BGR2GRAY);


        Mat blackWhite1 = Mat.zeros(src1.rows(), src1.cols(), src1.type());
        Mat blackWhite2 = Mat.zeros(src2.rows(), src2.cols(), src2.type());
        threshold(src1, blackWhite1, 15, 255, THRESH_BINARY);
        threshold(src2, blackWhite2, 15, 255, THRESH_BINARY);
        printSrcs(blackWhite1, blackWhite2);


        equalizeHist(src1, src1);
        equalizeHist(src2, src2);
        printSrcs(src1, src2);

        GaussianBlur(src1, src1, new Size(7,7), 15);
        GaussianBlur(src2, src2, new Size(7,7), 15);

        printSrcs(src1, src2);


        Mat mask1 = Mat.zeros(src1.rows(), src1.cols(), src1.type());
        Mat mask2 = Mat.zeros(src2.rows(), src2.cols(), src2.type());
        adaptiveThreshold(src1, mask1 , 255,ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV,7,4);
        adaptiveThreshold(src2, mask2, 255,ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV,7,4);


        int kernelSize = 1;
        int elementType = Imgproc.CV_SHAPE_RECT;
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        dilate(mask1, mask1, element );
        dilate(mask2, mask2, element );
        printSrcs(mask1, mask2);

        GaussianBlur(mask1, mask1, new Size(11,11), 24);
        GaussianBlur(mask2, mask2, new Size(11,11), 24);
        printSrcs(mask1, mask2);

        kernelSize = 2;
        element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        erode(mask1, mask1, element );
        erode(mask2, mask2, element );
        printSrcs(mask1, mask2);

        List<Mat> hsvBaseList = Arrays.asList(mask1);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(0), blackWhite1, mask1, new MatOfInt(new int[]{10}), new MatOfFloat(0, 256), false);
        Core.normalize(mask1, mask1, 0, 1, Core.NORM_MINMAX);

        List<Mat> hsvBaseList2 = Arrays.asList(mask2);
        Imgproc.calcHist(hsvBaseList2, new MatOfInt(0), blackWhite2, mask2, new MatOfInt(new int[]{10}), new MatOfFloat(0, 256), false);
        Core.normalize(mask2, mask2, 0, 1, Core.NORM_MINMAX);

        double output = compareHist(mask1, mask2, 0); //Chi-square comparison method
        System.out.println(output);
        if(output > 0.99996) System.out.println("Its a match!");
        System.out.println("Done!");
    }

    public static void printSrcs(Mat src1, Mat src2){
        namedWindow("src2");
        imshow("src2", src2);
        namedWindow("src1");
        imshow("src1", src1);
        waitKey();
    }



}