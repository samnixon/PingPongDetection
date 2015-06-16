package pds.computerVisionService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * Class detects racket
 * @author Rui Xu
 *
 */
public class RacketDetection implements Runnable {
	private static MatOfPoint racketModel = null;
	private CVFrame currentFrame;
	private CountDownLatch cdLatch;
	private RacketStore racketStore;
	//private static MatDisplay md;
	
	
	public RacketDetection(CVFrame curframe, RacketStore racketStore, CountDownLatch cdLatch) {
		if (racketModel == null) {
			Mat racket = Highgui.imread("res/racketModel.jpg");
			Imgproc.resize(racket, racket, new Size(640,360));
			Imgproc.cvtColor(racket, racket, Imgproc.COLOR_RGB2GRAY);
			Imgproc.Canny(racket, racket, 100, 255);
			ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Imgproc.findContours(racket, contours, new Mat(),Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
			racketModel = contours.get(0);
			ArrayList<MatOfPoint> temp1 = new ArrayList<MatOfPoint>();
			temp1.add(racketModel);
			Mat model = Mat.zeros(racket.size(), CvType.CV_8UC3);
			Imgproc.drawContours(model, temp1, -1, new Scalar(255,0,0));
//			Highgui.imwrite("E:/racketModel.jpg",model);
		}
		this.currentFrame = curframe;
		this.cdLatch = cdLatch;
		this.racketStore = racketStore;
		/*if(md == null) {
			RacketDetection.md = new MatDisplay("Rui's ball detection");
		}*/
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Mat curFrame = currentFrame.getData();
		Imgproc.resize(curFrame, curFrame, new Size(640,360));
		List<MatOfPoint> contours_original =  getRacketCandidates(curFrame);
		List<RotatedRect> rackets = new ArrayList<RotatedRect>();
		//Mat resultMat = Mat.zeros(curFrame.size(), CvType.CV_8UC3);
		
		for(int i = 0; i < contours_original.size(); i++) {
			double compareResult = Imgproc.matchShapes(contours_original.get(i), racketModel, Imgproc.CV_CONTOURS_MATCH_I1, 0);
//			System.out.println("the result of the matchshape : " + compareResult);
			if(compareResult < 0.2) {
				MatOfPoint2f approxCurve = new MatOfPoint2f();
				MatOfPoint2f  temp = new MatOfPoint2f();
				contours_original.get(i).convertTo(temp, CvType.CV_32F);
				Imgproc.approxPolyDP(temp, approxCurve,
						Imgproc.arcLength(temp, true)*.01, true);
				rackets.add(Imgproc.fitEllipse(temp));
//				result = true;
				//ArrayList<MatOfPoint> temp = new ArrayList<MatOfPoint>();
				//temp.add(contours_original.get(i));
				//Imgproc.drawContours(resultMat, temp, -1, new Scalar(255,0,0));
//				Highgui.imwrite("E:/racket_" + frame.getFrameNumber() +".jpg", resultMat);
				//md.updateResult(resultMat);
			}
		}
		racketStore.addResult(currentFrame.getFrameNumber(), rackets);
		
		cdLatch.countDown();
	}
	
	
	/** Runs the input image through several filters to obtain a List of object that
	 * are good racket candidates
	 * @param image
	 * @author Sam Nixon
	 * @return
	 */
	public static List<MatOfPoint> getRacketCandidates(Mat image){
		Mat edges = new Mat();
		List<MatOfPoint> toDraw = new ArrayList<MatOfPoint>();
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
				toDraw.add(contours.get(i));
				
			}
		}
		
		
		/*if (showResult){
			Mat viewOfRacketCandidates = Mat.zeros(edges.size(), CvType.CV_8UC3);
			Core.fillPoly(viewOfRacketCandidates, toDraw, new Scalar(0,255,0));
			for (MatOfPoint mp : toDraw){
				Rect r = Imgproc.boundingRect(mp);
				Core.rectangle(viewOfRacketCandidates, r.br(), r.tl(), new Scalar(255,0,0));
			}
			
			//display.updateResult(viewOfRacketCandidates);
		}*/
	
		
		return toDraw;
	}
}
