package pds.computerVisionService;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Offers a means of detecting a moving ball
 * @author Sam Nixon, contributions by Chris Guinnup
 *
 */
public class BallDetect implements Runnable {

	private final CVFrame previousFrame;
	private final CVFrame currentFrame;
	private final BallStore ballStore;
	private final CountDownLatch latch;
	private final static MatDisplay display = new MatDisplay("Detect Ball", 0, MatDisplay.DEFAULT_HEIGHT+32);
	
	private final static double MAX_ELLIPSE_DEVIATION = 0.2;
	private final static int MIN_BALL_AREA = 50;
	private final static int MAX_BALL_AREA = 1000;

	public BallDetect(CVFrame previousFrame, CVFrame currentFrame, BallStore ballStore, CountDownLatch latch){
		this.previousFrame = previousFrame;
		this.currentFrame = currentFrame;
		this.ballStore = ballStore;
		this.latch = latch;
	}

	
	/**
	 * Attempts to detect ping-pong balls based on shape and size
	 */
	@Override
	public void run() {
		Mat converted1 = previousFrame.getSmallData();
		Mat converted2 = currentFrame.getSmallData();
		Mat dif = new Mat();
		Mat edges = new Mat();
		ArrayList<RotatedRect> rectangleList = new ArrayList<RotatedRect>();
		
		edges.create(currentFrame.getDimensions(), CvType.CV_8U);
		Core.subtract(converted2, converted1, dif);
		Core.multiply(dif, new Scalar(3), dif);
		//dif has our standard difference
		Imgproc.threshold(dif, dif, 100, 255, Imgproc.THRESH_TOZERO);
		
		//Apply a blur to smooth out foreground objects
		Imgproc.GaussianBlur(dif, edges, new Size(45,45), 0);
		//Brighten the image a bit
		Core.multiply(edges, new Scalar(5), edges);
		//Remove darker pixels (cleans up edges)
		Imgproc.threshold(edges, edges, 75, 255, Imgproc.THRESH_TOZERO);

		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		ArrayList<MatOfPoint> toDraw = new ArrayList<MatOfPoint>();
		//ArrayList<MatOfPoint> notEllipse = new ArrayList<MatOfPoint>();
		//ArrayList<MatOfPoint> wrongSize = new ArrayList<MatOfPoint>();
		ArrayList<MatOfPoint2f> approxPolygons = new ArrayList<MatOfPoint2f>();
		
		Imgproc.findContours(edges.clone(), contours, new Mat(), 
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		for (int i = 0; i < contours.size(); i++){
			MatOfPoint2f approxCurve = new MatOfPoint2f();
			MatOfPoint2f  temp = new MatOfPoint2f();
			contours.get(i).convertTo(temp, CvType.CV_32F);
			Imgproc.approxPolyDP(temp, approxCurve,
					Imgproc.arcLength(temp, true)*.01, true);
			//fitEllipse needs 5 or more points in the approximated polygon
			if(approxCurve.rows() >= 5 ){
				//The rectangle that the ellipse inscribes
				RotatedRect rr = Imgproc.fitEllipse(approxCurve);
				//Area of Ellipse = PI*(width/2)*(height/2)
				double areaOfFittedEllipse = Math.PI*(rr.size.height*rr.size.width)/4;
				double areaOfContour = Imgproc.contourArea(contours.get(i));
				//The actual and fitted areas should be close
				if ( Math.abs((areaOfContour/areaOfFittedEllipse) - 1) < MAX_ELLIPSE_DEVIATION
						&& areaOfFittedEllipse > MIN_BALL_AREA && areaOfFittedEllipse <= MAX_BALL_AREA) {
					toDraw.add(contours.get(i));
					rectangleList.add(rr);
					approxPolygons.add(approxCurve);
				} // BELOW CONDITIONS: TESTING CODE
				/*else if (Math.abs((areaOfContour/areaOfFittedEllipse) - 1) >= MAX_ELLIPSE_DEVIATION 
						&& areaOfFittedEllipse > MIN_BALL_AREA && areaOfFittedEllipse <= MAX_BALL_AREA) {
					notEllipse.add(contours.get(i));
				}
				else if (Math.abs((areaOfContour/areaOfFittedEllipse) - 1) < MAX_ELLIPSE_DEVIATION	
						&& areaOfFittedEllipse <= MIN_BALL_AREA || areaOfFittedEllipse > MAX_BALL_AREA) {
					wrongSize.add(contours.get(i));
				}*/
			}
		}
		
		// add resulting RotatedRectanges (they have easy-to-find center point)
		ballStore.addResult(currentFrame.getFrameNumber(), rectangleList);
		
		//launch thread to display 
		final ArrayList<MatOfPoint> threadContours = contours;
		/*final ArrayList<MatOfPoint> threadNotEllipse = notEllipse;
		final ArrayList<MatOfPoint> threadWrongSize = wrongSize;*/
		final ArrayList<MatOfPoint> threadToDraw = toDraw;
	
		new Thread(){ public void run() {
			Mat result = new Mat();
			//result now has currentFrame in a color format so that we can overlay features
			Imgproc.cvtColor(currentFrame.getSmallData(), result, Imgproc.COLOR_GRAY2BGR);
			
			Imgproc.drawContours(result, threadContours, -1, new Scalar(0,0,255),2);
			//Uncomment to have non-ball candidates outlined in various colors based on what criteria they lack
			/*Imgproc.drawContours(result, threadNotEllipse, -1, new Scalar(128,0,128),2);
			Imgproc.drawContours(result, threadWrongSize, -1, new Scalar(255,0,0),2);*/
			Imgproc.drawContours(result, threadToDraw, -1, new Scalar(0,255,0),8);
			display.updateResult(result);
		}}.start();
		
		// let main program loop know that this thread is done
		latch.countDown();
	}

}
