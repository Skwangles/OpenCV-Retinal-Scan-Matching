package com.skwangles;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.List;

import static org.opencv.core.Core.minMaxLoc;
import static org.opencv.imgproc.Imgproc.*;


public class RetinalMatch
{
    public static void main(String[] args)
    {
        // load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        if(args.length != 2){
            System.err.println("Incorrect Usage, Must have 2 arguments.\n Usage: RetinalMatch <image 1 path> <image 2 path>");
            System.exit(0);
        }
        CheckImages(args[0],args[1]);
    }




    public static void  CheckImages(String src1path, String src2path){
        // get the jpeg image from the internal resource folder
        Mat src1 = Imgcodecs.imread(src1path);
        Mat src2 = Imgcodecs.imread(src2path);

        if (src1.empty() || src2.empty()) {
            System.err.println("Cannot read the images supplied.");
            System.exit(0);
        }

        //To Grayscale
        cvtColor(src1, src1, COLOR_BGR2GRAY);
        cvtColor(src2, src2, COLOR_BGR2GRAY);

        //Create mask to avoid comparing edge of the image
        Mat src1Mask = Mat.zeros(src1.rows(), src1.cols(), src1.type());
        Mat src2Mask = Mat.zeros(src2.rows(), src2.cols(), src2.type());
        threshold(src1, src1Mask, 15, 255, THRESH_BINARY);
        threshold(src2, src2Mask, 15, 255, THRESH_BINARY);


        src1.convertTo(src1, -1, 1.2, 0);//changing contrast//
        src2.convertTo(src2, -1, 1.2, 0);//changing contrast

        GaussianBlur(src1, src1, new Size(11,11), 0, 0, Core.BORDER_DEFAULT);
        GaussianBlur(src2, src2, new Size(11,11), 0, 0, Core.BORDER_DEFAULT);



        //Edge detection
        Mat dst = new Mat();
        Mat dst2 = new Mat();
        Imgproc.Laplacian( src1, dst, CvType.CV_16S, 3, 1, 40, Core.BORDER_DEFAULT );
        Imgproc.Laplacian( src2, dst2, CvType.CV_16S, 3, 1, 40, Core.BORDER_DEFAULT );
        Core.convertScaleAbs(dst, src1);
        Core.convertScaleAbs(dst2, src2);

        //Smooth edges generated by Laplacian
        GaussianBlur(src1, src1, new Size(11,11), 0, 0, Core.BORDER_DEFAULT);
        GaussianBlur(src2, src2, new Size(11,11), 0, 0, Core.BORDER_DEFAULT);

        //Get binarised white with Black marks
        threshold(src1, src1, 41, 255, THRESH_BINARY);//is 40, as that is the minimum of the Laplacian
        threshold(src2, src2, 41, 255, THRESH_BINARY);
        Core.bitwise_and(src1, src1Mask, src1);
        Core.bitwise_and(src2, src2Mask, src2);

        Core.bitwise_not(src1, src1);
        Core.bitwise_not(src2,src2);

        //Eliminate some noise
        medianBlur(src1, src1, 5);
        medianBlur(src2, src2, 5);

        //--remove small strctures then grow remaining--
        //Do OPENING - not closing
        //Note, dilate does what erode should - idk why...
        int kernelSize = 1;
        int elementType = CV_SHAPE_CROSS;
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        erode(src1, src1, element);
        erode(src2, src2, element);

        kernelSize = 1;
        element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        dilate(src1, src1, element);
        dilate(src2, src2, element);

        compareCleanedRetinas(src1, src2, src2Mask);

    }


    private static void compareCleanedRetinas(Mat src1, Mat src2, Mat mask2){

        double matchThreshold = 0.123;

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask2, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //Find the biggest area contour (i.e. the mask area)
        MatOfPoint cont = contours.get(0);
        for (MatOfPoint contour: contours) {
           if(contourArea(contour) > contourArea(cont)){
               cont = contour;
           }
        }
        Mat templ = new Mat(src2, boundingRect(cont));//Crop to maxsize contour

        Mat result = new Mat();
        int result_cols = src1.cols() - templ.cols() + 1;
        int result_rows = src1.rows() - templ.rows() + 1;
        result.create(result_rows, result_cols, CvType.CV_32FC1);

        int match_method = TM_CCOEFF_NORMED;
        matchTemplate(src1, templ, result, match_method);

        Core.MinMaxLocResult mmr = minMaxLoc(result);

        //Print result
        if(mmr.maxVal > matchThreshold)
          System.out.println('1');
        else
            System.out.println('0');

    }
}