package pds.situationAnalysis;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

import pds.computerVisionService.BallStore;
import pds.computerVisionService.CVStores;
import pds.computerVisionService.DifferenceStore;
import pds.computerVisionService.RacketStore;
import pds.computerVisionService.TableStore;

/**
 * Converts the raw data from frame analysis and frame comparisons
 * into estimated probabilities that certain events are occuring.
 * @author Chris Guinnup
 *
 */
public class ImmediateStatus implements Runnable{
	/* Output */
	StatusStores statusStores;
	/* Input */
	CVStores cvStores;
	long frameNum; // max of any 2 compared
	
	
	/**
	 * Initializes the thread with all necessary references
	 */
	public ImmediateStatus(long frameNum, CVStores cvStores, StatusStores statusStores) {
		this.frameNum = frameNum;
		this.statusStores = statusStores;
		this.cvStores = cvStores;
	}
	
	
	/**
	 * Performs immediate probability assignment for just-analyzed frames
	 */
	public void run() {
		// analyze cvStores for probabilities of each event

		ImmediateNode probs = new ImmediateNode();
		probs.frameNum = frameNum;
		probs.probDiffMotion = analyzeDiffs(cvStores.diffStore);
		probs.probTable = analyzeTable(cvStores.tableStore);
		probs.potentialBallPoints = getPotentialBallPoints(cvStores.ballStore);
		probs.potentialRacketPoints = getPotentialRacketPoints(cvStores.racketStore);
		
		// sanity check
		/*if(frameNum%30 == 0)
			System.out.println("Frame " + frameNum + " Prob Table: " + probs.probTable);*/
		
		statusStores.immedStore.addResult(frameNum, probs);
	
		// run an AggregateStatus analysis 
		AggregateStatus agg = new AggregateStatus(frameNum, cvStores, statusStores);
		agg.parseImmedStatus();
	}
	
	
	/**
	 * Determines the center locations of potential ball points from the last frame comparison
	 * @param ballStore The calculated bounding rectangles on moving ball-like objects
	 * @return A list of the center points for all potential ball-objects
	 */
	private List<Point> getPotentialBallPoints(BallStore ballStore) {
		ArrayList<Point> potentialBallCenters = new ArrayList<Point>();
		List<RotatedRect> boundingRectangles = ballStore.getBoundingRectangles(frameNum);
		
		if (boundingRectangles != null) {
			for (RotatedRect r : boundingRectangles) {
				potentialBallCenters.add(r.center);
				//sanity check
				//System.out.println("Frame: " + frameNum + " Point: " + r.center);
			}
		}
		return potentialBallCenters;
	}
	/** Determines the center locations of potential racket points from the last frame comparison
	 * @param racketStore The calculated bounding rectangles on moving racket-like objects
	 * @return A list of the center points for all potential ball-objects
	 */
	private List<Point> getPotentialRacketPoints(RacketStore ballStore) {
		ArrayList<Point> potentialBallCenters = new ArrayList<Point>();
		List<RotatedRect> boundingRectangles = ballStore.getBoundingRectangles(frameNum);
		
		if (boundingRectangles != null) {
			for (RotatedRect r : boundingRectangles) {
				potentialBallCenters.add(r.center);
				//sanity check
				//System.out.println("Frame: " + frameNum + " Point: " + r.center);
			}
		}
		return potentialBallCenters;
	}
	
	
	/**
	 * Attempts to assign a probability to detected motion being a game in progress.
	 * High degree of motion is regarded as a sign that the table may not be in view,
	 * thus is assigned a lower probability.
	 * @param diffStore The calculated frame differences
	 * @return Probability ranging from 1.0 to 0.0
	 */
	private double analyzeDiffs(DifferenceStore diffStore) {

		if (diffStore.wasSkipped(frameNum) == false) {
			double weightedDiff = diffStore.getDifference(frameNum);
			
			final double LOWER_BOUND = 1234;
			final double UPPER_BOUND = 1234567;
			
			if (weightedDiff <= LOWER_BOUND)
				return weightedDiff / LOWER_BOUND;
			if (weightedDiff <= UPPER_BOUND)
				return 1.0;
			else // if (weightedDiff > UPPER_BOUND)
				return 1.0 - Math.log( (Math.E-1)*((weightedDiff-UPPER_BOUND)/weightedDiff)+1 ); 
				// formula keeps value between 1.0 and 0.0
		}
		else 
			return 0.0;
	}	
	
	/**
	 * Calculates binary probability for "is table present?"
	 */
	private double analyzeTable(TableStore tableStore) {
		if (findParallelRectangles( getRectangles( 
				tableStore.getTableResults(frameNum))) )
			return 1.0;
		else
			return 0.0;
	}
	
	
	/**
	 * Determines whether the input contains two Rectangles that are parallel
	 * @author Sam Nixon
	 */
	private boolean findParallelRectangles(ArrayList<RotatedRect> input) {
		if (input != null) {
			
			for (int i = 0; i < input.size(); i++){
				for(int j = 0; j < i; j++){
					RotatedRect rectangle1 = input.get(i);
					RotatedRect rectangle2 = input.get(j);
					double difference = Math.abs(rectangle1.angle-rectangle2.angle);
					if(difference < 3 || difference + 180 < 3) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Returns a collection containing the rectangles that bound the input contours
	 * @author Sam Nixon
	 */
	private ArrayList<RotatedRect> getRectangles(ArrayList<MatOfPoint2f> input){
		ArrayList<RotatedRect> result = new ArrayList<RotatedRect>();
		Iterator<MatOfPoint2f> i = input.iterator();
		while (i.hasNext()){
			result.add(Imgproc.minAreaRect(i.next()));
		}
		return result;
	}
	
	
	//TODO: analyze other factors
}
