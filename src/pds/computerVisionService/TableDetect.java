package pds.computerVisionService;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Determines whether or not enough of a ping pong table is in view for further
 * analysis
 * @author Sam Nixon
 *
 */
public class TableDetect implements Runnable{
	
	private final TableStore tableStore;
	private final CountDownLatch latch;
	private final CVFrame currentFrame;
	private final static MatDisplay display = new MatDisplay("Table", MatDisplay.DEFAULT_WIDTH/2 + 16, MatDisplay.DEFAULT_HEIGHT+32);

	
	public TableDetect(CVFrame currentFrame, TableStore tableStore, CountDownLatch latch){
		this.currentFrame = currentFrame;
		this.latch = latch;
		this.tableStore = tableStore;
	}

	/**
	 * 
	 * @param grayscale: a grayscale image
	 * @return  A downsized Binary Mat with noise reduction and edge detection
	 */
	private Mat edgeDetection(Mat grayscale){
		Mat gray = grayscale.clone();
		Imgproc.blur(gray, gray, new Size(2,2));
		Imgproc.dilate(gray, gray, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15,15)), new Point(-1,-1), 3);
		Imgproc.threshold(gray, gray, 200, 255, Imgproc.THRESH_BINARY);
		Imgproc.pyrDown(gray, gray, new Size(gray.size().width/2, gray.size().height/2));
		Core.subtract(gray, new Scalar(80), gray);
		for (int i = 0; i < 10; i++){
			Core.multiply(gray, new Scalar(1.02), gray);
		}
		

		Mat edges = new Mat();
		Imgproc.Canny(gray, edges, 0, 255 );
		Imgproc.dilate(edges,  edges, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)), new Point(-1,-1),2);
		return edges;
	}
	
	/**
	 * Takes as input a grayscale image and inverts the colors. Does not modify
	 * original.
	 */
	private Mat invertImage(Mat input){

		Mat result = Mat.ones(input.size(), CvType.CV_8U);
		Core.multiply(result, new Scalar(255), result);
		Core.subtract(result, input, result );
		return result;
	}
	
	/**
	 * Runs various filters and edge detections to determine whether a ping pong
	 * table is in view
	 */
	@Override
	public void run() {
		Mat gray = currentFrame.getData();
	
		Mat edges = edgeDetection(gray);
		

		Mat inverseEdges = invertImage(edges);
		

		
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		ArrayList<MatOfPoint> toDraw = new ArrayList<MatOfPoint>();
		ArrayList<MatOfPoint2f> approxPolygons = new ArrayList<MatOfPoint2f>();
		Imgproc.findContours(inverseEdges.clone(), contours, new Mat(), 
				Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		for (int i = 0; i < contours.size(); i++){
			MatOfPoint2f approxCurve = new MatOfPoint2f();
			MatOfPoint2f  temp = new MatOfPoint2f();
			contours.get(i).convertTo(temp, CvType.CV_32F);
			Imgproc.approxPolyDP(temp, approxCurve,
					Imgproc.arcLength(temp, true)*.02, true);
			
			int numEdges = approxCurve.toList().size();
			Size rotatedRectSize = Imgproc.minAreaRect(approxCurve).size;
			int rotatedRectArea = (int) ((rotatedRectSize.width)*(rotatedRectSize.height));
			if (numEdges < 6 && numEdges > 3 && rotatedRectArea> 3000){	
				toDraw.add(contours.get(i));
				approxPolygons.add(approxCurve);
			}
		}
		Mat result = Mat.ones(edges.size(), CvType.CV_8U);
		Core.multiply(result, new Scalar(255), result);
		Core.fillPoly(result, toDraw, new Scalar(0));
		//Imgproc.drawContours(result, toDraw,-1,  new Scalar(0), 2);
		
		

		
		tableStore.addResult(currentFrame.getFrameNumber(), approxPolygons);
		latch.countDown();

		display.updateResult(result);
	}
}
