package pds.computerVisionService;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * 
 * @author Sam Nixon
 *
 */
public class ExtractObjectCandidates {
	
	//public static MatDisplay display = new MatDisplay("Racket Candidate Progress");;

	/**
	 * Runs the input image through several filters to obtain a List of object that
	 * are good racket candidates
	 * @param image
	 * @return
	 */
	public static List<MatOfPoint> getRacketCandidates(Mat image){
		Mat edges = new Mat();
		List<MatOfPoint> result = new ArrayList<MatOfPoint>();
		Imgproc.threshold(image, image, 160, 255, Imgproc.THRESH_TOZERO);
		//Imgproc.threshold(image, image, 220, 255, Imgproc.THRESH_TOZERO_INV);
		Imgproc.GaussianBlur(image, edges, new Size(5,5), 0);
		//Erosion removes most of the lines on the table
		Imgproc.erode(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3)), new Point(-1,-1), 5);
		Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2,2)), new Point(-1,-1), 3);
		Imgproc.threshold(edges, edges, 1, 255, Imgproc.THRESH_BINARY);
	
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
		Imgproc.findContours(edges.clone(), contours, new Mat(), 
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		for (int i = 0; i < contours.size(); i++){
			MatOfPoint2f approxCurve = new MatOfPoint2f();
			MatOfPoint2f  temp = new MatOfPoint2f();
			contours.get(i).convertTo(temp, CvType.CV_32F);
			Imgproc.approxPolyDP(temp, approxCurve,
					Imgproc.arcLength(temp, true)*.01, true);
			Rect r = Imgproc.boundingRect(contours.get(i));
			//Searches for complex shapes of medium size
			if (approxCurve.rows() > 7 && r.area() > 2000 && r.area() < 5000){
				result.add(contours.get(i));
			}
		}
		//toggle on and off to display
		boolean showResult = false;
		if (showResult){
			Mat viewOfRacketCandidates = Mat.zeros(edges.size(), CvType.CV_8UC3);
			Core.fillPoly(viewOfRacketCandidates, result, new Scalar(0,255,0));
			for (MatOfPoint mp : result){
				Rect r = Imgproc.boundingRect(mp);
				Core.rectangle(viewOfRacketCandidates, r.br(), r.tl(), new Scalar(255,0,0));
			}
			
			//display.updateResult(viewOfRacketCandidates);
		}
		
		
		
		return result;
	}
}
