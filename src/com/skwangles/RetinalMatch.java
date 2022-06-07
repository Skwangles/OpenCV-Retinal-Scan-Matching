package com.skwangles;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.List;

import static org.opencv.core.Core.minMaxLoc;
import static org.opencv.highgui.HighGui.*;
import static org.opencv.imgproc.Imgproc.*;


public class RetinalMatch
{
    private static int countOfFails = 0;
    private static int totalComparisons = 0;
    private static Random rand = new Random();


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

        //CheckAll(prefix);
        //CheckAllSame(prefix);
        //CheckSpecific(prefix, 3, 4, 4, 4);
        CheckCompletelyRandom(prefix, 500);
        System.out.println("Stats: Failure "+countOfFails/totalComparisons + "% - From " + countOfFails + "/" + totalComparisons + " Comparisons");
    }

    public static void CheckSpecific(String prefix, int batch1, int src1, int batch2, int src2){
        CheckImages(prefix + batch1 + "_" + src1 + ".JPG",prefix + batch2 + "_" + src2 + ".JPG");
    }

    public static void CheckAll(String prefix){
        //Test all images here
        HashSet<String> compared = new HashSet<>();
        for(int item1batch = 1; item1batch <= 5; item1batch++){
         for(int item1 = 1; item1 <= 20; item1++){
             for(int item2batch = 1; item2batch <= 5; item2batch++){
                 for (int item2 = 1; item2 <= 20; item2++){
                     if(compared.contains(item2batch + "_" + item2 + "__"+item1batch + "_" + item1)){//Will only be in if in reverse order
                         continue;
                     }
                     compared.add(item1batch + "_" + item1 + "__"+item2batch + "_" + item2);
                     CheckImages(prefix + item1batch + "_" + item1 + ".JPG",prefix + item2batch + "_" + item2 + ".JPG");
                        totalComparisons++;
                 }
             }
         }
        }
    }

    public static void CheckCompletelyRandom(String prefix, int comparisons){
        //Test all images here
        HashSet<String> compared = new HashSet<>();
        for(int i = 0; i < comparisons; i++){
            int item1batch = 0;
            int item2batch = 0;
            int item1 = 0;
            int item2 = 0;
            do {
                item1batch = rand.nextInt(5) + 1;
                item2batch = rand.nextInt(5) + 1;
                item1 = rand.nextInt(20) + 1;
                item2 = rand.nextInt(20) + 1;
            }
            while(compared.contains(item2batch + "_" + item2 + "__"+item1batch + "_" + item1) || compared.contains(item1batch + "_" + item1 + "__"+item2batch + "_" + item2));

            compared.add(item1batch + "_" + item1 + "__"+item2batch + "_" + item2);
            CheckImages(prefix + item1batch + "_" + item1 + ".JPG",prefix + item2batch + "_" + item2 + ".JPG");
            totalComparisons++;
        }
    }



    public static void CheckAllSame(String prefix){
        //Test all images here
        HashSet<String> compared = new HashSet<>();
        for(int item1batch = 1; item1batch <= 5; item1batch++){
            for(int item1 = 1; item1 <= 20; item1++){
                for(int item2batch = 1; item2batch <= 5; item2batch++){
                    for (int item2 = 1; item2 <= 20; item2++){
                        if(item1 != item2) continue;
                        if(compared.contains(item2batch + "_" + item2 + "__"+item1batch + "_" + item1)){//Will only be in if in reverse order
                            continue;
                        }
                        compared.add(item1batch + "_" + item1 + "__"+item2batch + "_" + item2);
                        CheckImages(prefix + item1batch + "_" + item1 + ".JPG",prefix + item2batch + "_" + item2 + ".JPG");
                        totalComparisons++;
                    }
                }
            }
        }
    }


    public static void  CheckImages(String src1path, String src2path){
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
        Core.bitwise_and(src1, blackWhite1, src1);
        Core.bitwise_and(src2, blackWhite2, src2);

        Core.bitwise_not(src1, src1);
        Core.bitwise_not(src2,src2);

        //Eliminate some noise
        medianBlur(src1, src1, 5);
        medianBlur(src2, src2, 5);

        //--remove small strctures then grow remaining--
        //Do OPENING - not closing
        //Note, dilate does what erode should - idk why...
        int kernelSize = 1;
        int elementType = CV_SHAPE_RECT;
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        dilate(src1, src1, element);
        dilate(src2, src2, element);


        kernelSize = 1;
        element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1), new Point(kernelSize, kernelSize));
        erode(src1, src1, element);
        erode(src2, src2, element);

        compareCleanedRetinas(src1, src2, src1path, src2path, blackWhite2);

    }


    private static void compareCleanedRetinas(Mat src1, Mat src2, String src1path, String src2path, Mat mask2){

        double matchThreshold = 0.15;
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

        Mat result = new Mat(), img_display = new Mat();
        src1.copyTo(img_display);
        int result_cols = src1.cols() - templ.cols() + 1;
        int result_rows = src1.rows() - templ.rows() + 1;
        result.create(result_rows, result_cols, CvType.CV_32FC1);
        int match_method = TM_CCOEFF_NORMED;
        matchTemplate(src1, templ, result, match_method);
        //Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Point matchLoc;

        Core.MinMaxLocResult mmr = minMaxLoc(result);
//
//        if (match_method == TM_SQDIFF || match_method == TM_SQDIFF_NORMED) {
//            matchLoc = mmr.minLoc;
//        } else {
//            matchLoc = mmr.maxLoc;
//            System.out.println(mmr.maxVal);
//        }
//
//        //Drawing the matches
//        rectangle(img_display, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
//                new Scalar(0, 0, 0), 2, 8, 0);
//        rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
//                new Scalar(0, 0, 0), 2, 8, 0);
//        result.convertTo(result, CvType.CV_8UC1, 255.0);
//
//
//                namedWindow("templ");
//                imshow("templ", src2);
//                namedWindow("dis");
//                imshow("dis", img_display);
//                namedWindow("res");
//                imshow("res", result);
//                waitKey();

        //Determining if the match should be a match
        String[] strs1 = src1path.split("_");
        String num1 = strs1[1].substring(0,strs1[1].length()-4);
        String[] strs2 = src2path.split("_");
        String num2 = strs2[1].substring(0,strs2[1].length()-4);
        if(mmr.maxVal > matchThreshold){
            if(!num1.equals(num2)) {
                System.out.println("INCORRECT MATCH: " + mmr.maxVal + " " + src1path + "&" + src2path);
                countOfFails++;
            }
        }
        else{
            if(num1.equals(num2)) {
                System.out.println("NON-MATCH: " + mmr.maxVal + " " + src1path + "&" + src2path);
                countOfFails++;
            }
        }
    }


    public static void printSrcs(Mat src1, Mat src2){
        namedWindow("src2");
        imshow("src2", src2);
        namedWindow("src1");
        imshow("src1", src1);
        waitKey();
    }
}