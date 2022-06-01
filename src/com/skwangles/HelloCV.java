package com.skwangles;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.CV_16S;
import static org.opencv.core.CvType.CV_64F;
import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;

import org.opencv.imgproc.*;
import org.opencv.imgcodecs.*;

import java.util.ArrayList;
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
        // load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // create and print on screen a 3x3 identity matrix
        System.out.println("Create a 3x3 identity matrix...");
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());

        // prepare to convert a RGB image in gray scale
        String location = "RIDB/IM000001_1.jpg";
        String location2 = "RIDB/IM000002_2.jpg";
        System.out.println("Convert the image at " + location + " in gray scale... ");
        // get the jpeg image from the internal resource folder
        Mat src1 = Imgcodecs.imread(location);
        Mat src2 = Imgcodecs.imread(location2);

        if (src1.empty() || src2.empty()) {
            System.err.println("Cannot read the images");
            System.exit(0);
        }

        namedWindow("Original");
        imshow("Original", src1);
        namedWindow("Original2");
        imshow("Original2", src2);
        waitKey();

        //Make grayscale
        cvtColor(src1, src1, COLOR_BGR2GRAY);
        cvtColor(src2, src2, COLOR_BGR2GRAY);
        GaussianBlur(src1, src1, new Size(3,3), 1);
        GaussianBlur(src2, src2, new Size(3,3), 1);
        namedWindow("blur");
        imshow("blur", src1);
        namedWindow("blur2");
        imshow("blur2", src2);
        waitKey();
        Mat mask = src1;
        threshold(src1, mask, 10, 255, THRESH_BINARY);//Get mask area to focus on only the retina


//
//        Mat mask1 = Mat.zeros(src1.rows(), src1.cols(), src1.type());
//        Mat mask2 = Mat.zeros(src2.rows(), src2.cols(), src2.type());
//        threshold(src1, mask1, 10, 255, THRESH_BINARY);
//        threshold(src2, mask2, 10, 255, THRESH_BINARY);
//        src1 = src1-mask1;
//        adaptiveThreshold(src1, mask1 , 255,ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY,7,2);
//        adaptiveThreshold(src2, mask2, 255,ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY,7,2);
        src1.convertTo(src1, -1, 1.7, 0);
        src2.convertTo(src2, -1, 1.7, 0);
        //blur(src1, src1, new Size(3,3));



        namedWindow("contrast");
        imshow("contrast", src1);

        namedWindow("contrast2");
        imshow("contrast2", src2);
        waitKey();

        Canny(src1, src1, 30, 70);
        Canny(src2, src2, 30, 70);



        namedWindow("Final");
        imshow("Final", src1);

        namedWindow("Final2");
        imshow("Final2", src2);

        waitKey();
        System.out.println("Done!");
    }




}