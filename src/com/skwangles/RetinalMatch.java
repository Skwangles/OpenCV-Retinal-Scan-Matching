package com.skwangles;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;
import java.util.List;

import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.*;


public class RetinalMatch
{




    public static void main(String[] args)
    {
        //Pipeline
        //Remember to clean up the image with smoothing filters
        // • Choose a sensible colour channel or space to work in
        // • Possible contrast equalization to ensure that variances in illumination don’t mess up the results
        // • Pick image transformations (e.g. edges, thresholded, etc) and features
        // • Hopefully get something that works

        //Proposed pipeline - Autocontrast, threshold, intensity histogram
        //unsharp masking?
        //comparing image histograms - Vision lab 2
        //Template matching??

        // load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String prefix = "RIDB/IM00000";
        for(int item1batch = 1; item1batch <= 5; item1batch++){
         for(int item1 = 1; item1 <= 20; item1++){
             for(int item2batch = 1; item2batch <= 5; item2batch++){
                 for (int item2 = 1; item2 <= 20; item2++){
                     CheckImages(prefix + item1batch + "_" + item1 + ".JPG",prefix + item2batch + "_" + item2 + ".JPG");
                 }
             }
         }
        }
    }


    public static double CheckImages(String src1path, String src2path){
        double matchThreshold = 0.9999545;
        // get the jpeg image from the internal resource folder
        Mat src1 = Imgcodecs.imread(src1path);
        Mat src2 = Imgcodecs.imread(src2path);

        if (src1.empty() || src2.empty()) {
            System.err.println("Cannot read the images");
            System.exit(0);
        }

        //To Grayscale
        cvtColor(src1, src1, COLOR_BGR2GRAY);
        cvtColor(src2, src2, COLOR_BGR2GRAY);

        //Create mask to avoid comparing edge of the image
        Mat blackWhite1 = Mat.zeros(src1.rows(), src1.cols(), src1.type());
        Mat blackWhite2 = Mat.zeros(src2.rows(), src2.cols(), src2.type());
        threshold(src1, blackWhite1, 15, 255, THRESH_BINARY);
        threshold(src2, blackWhite2, 15, 255, THRESH_BINARY);

        //Blur image
        GaussianBlur(src1, src1, new Size(7,7), 23);
        GaussianBlur(src2, src2, new Size(7,7), 23);

        equalizeHist(src1, src1);
        equalizeHist(src2, src2);

        GaussianBlur(src1, src1, new Size(7,7), 23);
        GaussianBlur(src2, src2, new Size(7,7), 23);

        adaptiveThreshold(src1, src1 , 255,ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV,7,4);
        adaptiveThreshold(src2, src2, 255,ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV,7,4);


        //Erode away the noise - dilate does what erode should, idk why
        int kernelSize = 1;
        int elementType = CV_SHAPE_RECT;
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        dilate(src1, src1, element );
        dilate(src2, src2, element );

        GaussianBlur(src1, src1, new Size(11,11), 24);
        GaussianBlur(src2, src2, new Size(11,11), 24);

        kernelSize = 2;
        element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        erode(src1, src1, element );
        erode(src2, src2, element );




        //Create image histograms for comparison

        List<Mat> hsvBaseList = Arrays.asList(src1);
        Imgproc.calcHist(hsvBaseList, new MatOfInt(0), blackWhite1, src1, new MatOfInt(new int[]{10}), new MatOfFloat(0, 256), false);
        Core.normalize(src1, src1, 0, 1, Core.NORM_MINMAX);

        List<Mat> hsvBaseList2 = Arrays.asList(src2);
        Imgproc.calcHist(hsvBaseList2, new MatOfInt(0), blackWhite2, src2, new MatOfInt(new int[]{10}), new MatOfFloat(0, 256), false);
        Core.normalize(src2, src2, 0, 1, Core.NORM_MINMAX);

        //Compare and check
        double output = compareHist(src1, src2, 0); //Chi-square comparison method
        //System.out.println(output);
        //if(output > 0.999945) System.out.println("Its a match @ " + output);
        String[] strs1 = src1path.split("_");
        String num1 = strs1[1].substring(0,strs1[1].length()-4);

        String[] strs2 = src2path.split("_");
        String num2 = strs2[1].substring(0,strs2[1].length()-4);
        if(output > matchThreshold){
           if(!num1.equals(num2)) {
           System.out.println("INCORRECT MATCH: " + output + " " + src1path + "&" + src2path);
           }
        }
        else{
            if(num1.equals(num2)) {
                System.out.println("NON-MATCH: " + output + " " + src1path + "&" + src2path);
            }
        }
        return output;
    }



    public static void printSrcs(Mat src1, Mat src2){
        namedWindow("src2");
        imshow("src2", src2);
        namedWindow("src1");
        imshow("src1", src1);
        waitKey();
    }



}