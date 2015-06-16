package pds.situationAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Point;

import pds.PDSMain;
import pds.computerVisionService.CVStores;
import pds.output.ConsoleOutput;
import pds.situationAnalysis.AggregateStore.GameStatus;
import pds.videoService.FrameNode;

/**
 * Performs final calculations determining the game status, 
 * smoothing the result by averaging several immediate frame comparisons. 
 * @author Chris Guinnup
 *
 */
public class AggregateStatus{	
	/* Output */
	AggregateStore aggregateStore;
	/* Input */
	ImmediateStore immedStore;
	CVStores cvStores;
	long frameNumber; // max of any 2 compared
	private static ConsoleOutput out = null;

	//for testing & debugging
	private static long[] lastFramePrinted = {-999, -999};
	private static double aggregateBest = 0.0;


	/* Constants */
	protected static final int SMOOTHING_FACTOR = 30;  // Consecutive frames needed to declare a new status.
	protected static final int BALL_MAX_EXPLORE = 7;  // Must be <= SMOOTHING_FACTOR.  How many frames to explore to find actual balls.
	protected static final int IN_USE_INACTIVITY_BUFFER = 20*30;  // How many frames (30 fps) of inactivity can occur... 
																  // while we still consider the game in-progress. 
	//protected static final int REPRINT_STATUS_INTERVAL = 1*30;   // Interval between reprinting the same status (30fps).


	/**
	 * Initializes the thread with all necessary references
	 */
	public AggregateStatus(long frameNum, CVStores cvStores, StatusStores statusStores) {
		this.frameNumber = frameNum;
		this.cvStores = cvStores;
		this.immedStore = statusStores.immedStore;
		this.aggregateStore = statusStores.aggStore;
	}


	/**
	 * Takes a ConsoleOutput reference so that this class may call it.
	 * @return False if already initialized
	 */
	public static boolean initConsoleOutput(ConsoleOutput outArg) {
		if (out == null) {
			out = outArg;
			return true;
		}
		else {
			return false;
		}
	}


	/**
	 * Calculates the status of the game, collating and smoothing the immediate comparisons.
	 */
	protected void parseImmedStatus(){
		double sumTableProb = 0;

		ImmediateNode [] statuses = immedStore.getMultipleProbabilities(SMOOTHING_FACTOR);
		if (statuses != null) {
			for (int i=0; i<SMOOTHING_FACTOR; i++) {
				sumTableProb += statuses[i].probTable;
			}

			// sanity check
			/*if (frameNum % 30 == 0) {
				System.out.println("(Frames " + (frameNum-SMOOTHING_FACTOR+1) + "-" + frameNum 
						+ ") Avg Table Probs: "	+ (sumTableProb/SMOOTHING_FACTOR));
			}*/
			if (frameNumber - lastFramePrinted[0] >= 30) {
				lastFramePrinted[0] = frameNumber;
				PDSMain.displayStrings[0] = "Frames " + frameNumber + " to " + (frameNumber+1-SMOOTHING_FACTOR);
				PDSMain.displayStrings[1] = "Table Prob (Avg)";
				PDSMain.displayStrings[2] = "[" + repeat("*", (sumTableProb/SMOOTHING_FACTOR)*20) 
						+ repeat(" ", 20*(1.0 - sumTableProb/SMOOTHING_FACTOR)) + "] " + repeat("NO TABLE DETECTED!", 1-sumTableProb/SMOOTHING_FACTOR);
				PDSMain.statisticWindow.showText(PDSMain.displayStrings);
			}

			if (sumTableProb/SMOOTHING_FACTOR < 0.5) {
				update(GameStatus.NoTable);
			}
			else {
				if (potentialRacketDetected(statuses)){
					aggregateStore.setPotentialRacketEvent(frameNumber);
				}
				if (potentialBallDetected(statuses)) {
					aggregateStore.setPotentialBallEvent(frameNumber);
				}
			
				if (racketDetected(statuses) >= .9){
					update(GameStatus.Free);
				}

				else if (/*sumDiffProb/SMOOTHING_FACTOR*/ movingBallDetected(statuses) >= 0.5) {
					update(GameStatus.InUse);
				}
				else if (frameNumber - aggregateStore.getStatusFrameNum(GameStatus.InUse) > IN_USE_INACTIVITY_BUFFER) {
					update(GameStatus.Free);
				}
			}
		}
	}

	
	/**
	 * Detects if any rackets found
	 */
	private double racketDetected(ImmediateNode[] statuses) {
		int racketsFound = 0;
		for (ImmediateNode status : statuses){
			
			if (status.potentialRacketPoints.size() > 0){
				racketsFound++;
			}
		}
		return (double)racketsFound/statuses.length;
	}


	/**
	 * Checks if BallDetect has detected any ball candidates in the most recent frame
	 */
	protected boolean potentialBallDetected(ImmediateNode [] statuses) {
		if (statuses[SMOOTHING_FACTOR-1].potentialBallPoints.size() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	
	/**
	 * Checks if RacketDetection has detected any ball candidates in the most recent frame
	 */
	protected boolean potentialRacketDetected(ImmediateNode [] statuses) {
		if (statuses[SMOOTHING_FACTOR-1].potentialBallPoints.size() > 0) {
			return true;
		}
		else {
			return false;
		}
	}


	enum BallCandidate {
		ONE, TWO, THREE;
	}

	// Constants for detecting appropriate ball motion:
	// Good fit & distance factor at which function will stop searching for more
	static final double FIT_DISTANCE_THRESHOLD = 0.9;
	// Deviation from predicted line at which the fit probability...
	// is assigned 0, expressed in proportion of the line's length.
	static final double FIT_MAX_DEVIANCE = 0.2; 
	// Distance to qualify for full distance probability...
	// expressed as multiple of y-axis screen-length per frame.
	static final double DISTANCE_THRESHOLD = 0.018; 

	/**
	 * Attempts to detect balls moving in a linear-like path
	 * @param statuses The data from ImmediateStatus processing
	 * @return Double representing the probability that a ball was detected
	 */
	double movingBallDetected(ImmediateNode [] statuses) {
		double linearFitDeviation;
		double linearFit = 0.0; // set to 1.0 if perfect line found in 3+ frames
		double bestFitAndDistance = 0.0;
		double currentFitAndDistance;
		double distance, deltaX, deltaY, slope, intercept;
		double adjustedDistance;
		double distanceMeasure;
		//debugging vars
		double bestLinearFit = 0.0;
		double bestDistanceMeasure = 0.0;
		//end debugging vars
		int a=0, b=0, c=0; // array positions of our current 3 ball candidates
		int screenX = FrameNode.getCols()/2;
		int screenY = FrameNode.getRows()/2;
		Map<BallCandidate, Point> threeToTry = new HashMap<BallCandidate, Point>();
		Map<BallCandidate, List<Point>> remainingPoints = new HashMap<BallCandidate, List<Point>>();
		boolean newTripletFound = false;

		// select three potential ball points
		for (a = SMOOTHING_FACTOR-1; a >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; a--) {
			if (statuses[a].potentialBallPoints.size() > 0) {
				remainingPoints.put(BallCandidate.ONE, deepCopy(statuses[a].potentialBallPoints));
				threeToTry.put(BallCandidate.ONE, statuses[a].potentialBallPoints.get(0));
				remainingPoints.get(BallCandidate.ONE).remove(0);
				for (b = a-1; b >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; b--) {
					if (statuses[b].potentialBallPoints.size() > 0) {
						remainingPoints.put(BallCandidate.TWO, deepCopy(statuses[b].potentialBallPoints));
						threeToTry.put(BallCandidate.TWO, statuses[b].potentialBallPoints.get(0));
						remainingPoints.get(BallCandidate.TWO).remove(0);
						for (c = b-1; c >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; c--) {
							if (statuses[c].potentialBallPoints.size() > 0) {
								remainingPoints.put(BallCandidate.THREE, deepCopy(statuses[c].potentialBallPoints));
								threeToTry.put(BallCandidate.THREE, statuses[c].potentialBallPoints.get(0));
								remainingPoints.get(BallCandidate.THREE).remove(0);
								newTripletFound = true;
							}
						}	
					}
				}	
			}
		}

		a++; // decremented one time too many
		b++;
		c++;

		// evaluate line fit and distance of the three points
		while (bestFitAndDistance < FIT_DISTANCE_THRESHOLD ) {	
			// no more 3-point sets found so exit and return best	
			if (!newTripletFound) {
				break;
			}

			// make sure points' x, y coordinates are consistently ordered
			if (areOrdered(threeToTry)) {
				// see how good of a line fit these three points make:
				// draw line between potential ball points 1 & 3, then see how close point 2 is to predicted line
				// (this should be quicker than calculating regression fit)
				deltaX = threeToTry.get(BallCandidate.ONE).x - threeToTry.get(BallCandidate.THREE).x;
				deltaY = threeToTry.get(BallCandidate.ONE).y - threeToTry.get(BallCandidate.THREE).y;
				slope = deltaY / deltaX;
				intercept = -1*slope*threeToTry.get(BallCandidate.ONE).x + threeToTry.get(BallCandidate.ONE).y;
				distance = Math.sqrt(Math.pow(deltaY, 2) + Math.pow(deltaY, 2));

				// normalize the line-predicted nearness by the distance of the line
				linearFitDeviation = Math.abs( (slope*threeToTry.get(BallCandidate.TWO).x 
						+ intercept - threeToTry.get(BallCandidate.TWO).y) / distance );

				if (linearFitDeviation <= FIT_MAX_DEVIANCE) {
					linearFit = Math.abs(linearFitDeviation-FIT_MAX_DEVIANCE)/FIT_MAX_DEVIANCE;
				}
				else {
					linearFit = 0.0;
				}

				// check the distance between the points
				// normalize distance: by the spanning number of frames
				adjustedDistance = distance / (double)(statuses[a].frameNum - statuses[c].frameNum);
				// normalize distance: by the width of the frame
				adjustedDistance = adjustedDistance / ((double)(screenX+screenY)/2.0);
				distanceMeasure = Math.min(1.0, adjustedDistance/DISTANCE_THRESHOLD);

				// calculate distance & fit goodness
				currentFitAndDistance = linearFit * distanceMeasure;

				//sanity check
				//System.out.println("Frame " + frameNum + " currentFitAndDistance = " + currentFitAndDistance);

				// compare to best distance-fit goodness so far & replace if better
				if (currentFitAndDistance > bestFitAndDistance) {
					bestFitAndDistance = currentFitAndDistance;
					//debug:
					bestLinearFit = linearFit;
					bestDistanceMeasure = distanceMeasure;
				}
				
				// if good enough, break loop & return value
				if (currentFitAndDistance >= FIT_DISTANCE_THRESHOLD) {
					break;
				}
			}

			// now find new set of three ball candidates

			newTripletFound = false;
			// first check if any additional ball candidates are in current frames
			for (BallCandidate n: BallCandidate.values()) {
				if (remainingPoints.get(n).size() > 0) {
					threeToTry.put(n, remainingPoints.get(n).get(0));
					remainingPoints.get(n).remove(0);
					newTripletFound = true;
					break;
				}
			}

			// then try moving ball candidate c's frame
			if (!newTripletFound) {
				for (--c; c >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; c--) {
					if (statuses[c].potentialBallPoints.size() > 0) {
						remainingPoints.put(BallCandidate.THREE, deepCopy(statuses[c].potentialBallPoints));
						threeToTry.put(BallCandidate.THREE, statuses[c].potentialBallPoints.get(0));
						remainingPoints.get(BallCandidate.THREE).remove(0);
						newTripletFound = true;
						c++;
					}
				}	
			}

			// then try moving ball candidate b's frame (and resetting c)
			if (!newTripletFound) {
				for (--b; b >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; b--) {
					if (statuses[b].potentialBallPoints.size() > 0) {
						remainingPoints.put(BallCandidate.TWO, deepCopy(statuses[b].potentialBallPoints));
						threeToTry.put(BallCandidate.TWO, statuses[b].potentialBallPoints.get(0));
						remainingPoints.get(BallCandidate.TWO).remove(0);
						for (c = b-1; c >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; c--) {
							if (statuses[c].potentialBallPoints.size() > 0) {
								remainingPoints.put(BallCandidate.THREE, deepCopy(statuses[c].potentialBallPoints));
								threeToTry.put(BallCandidate.THREE, statuses[c].potentialBallPoints.get(0));
								remainingPoints.get(BallCandidate.THREE).remove(0);
								newTripletFound = true;
								c++; b++;
							}
						}	
					}
				}	
			}

			// then try moving ball candidate a's frame (and resetting b & c)
			if (!newTripletFound) {
				for (--a; a >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; a--) {
					if (statuses[a].potentialBallPoints.size() > 0) {
						remainingPoints.put(BallCandidate.ONE, deepCopy(statuses[a].potentialBallPoints));
						threeToTry.put(BallCandidate.ONE, statuses[a].potentialBallPoints.get(0));
						remainingPoints.get(BallCandidate.ONE).remove(0);
						for (b = a-1; b >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; b--) {
							if (statuses[b].potentialBallPoints.size() > 0) {
								remainingPoints.put(BallCandidate.TWO, deepCopy(statuses[b].potentialBallPoints));
								threeToTry.put(BallCandidate.TWO, statuses[b].potentialBallPoints.get(0));
								remainingPoints.get(BallCandidate.TWO).remove(0);
								for (c = b-1; c >= SMOOTHING_FACTOR-BALL_MAX_EXPLORE && !newTripletFound; c--) {
									if (statuses[c].potentialBallPoints.size() > 0) {
										remainingPoints.put(BallCandidate.THREE, deepCopy(statuses[c].potentialBallPoints));
										threeToTry.put(BallCandidate.THREE, statuses[c].potentialBallPoints.get(0));
										remainingPoints.get(BallCandidate.THREE).remove(0);
										newTripletFound = true;
										c++; b++; a++;
									}
								}	
							}
						}	
					}
				}
			}
			// restart primary function loop
		}

		// display code for statistics window
		if (frameNumber - lastFramePrinted[1] >= 30 || bestFitAndDistance > aggregateBest) {
			lastFramePrinted[1] = frameNumber;
			aggregateBest = bestFitAndDistance;

			PDSMain.displayStrings[4] = "Frame " + frameNumber;
			PDSMain.displayStrings[5] = "Ball Prob";
			PDSMain.displayStrings[6] = "[" + repeat("*", 20*bestFitAndDistance) 
					+ repeat(" ", 20*(1 -bestFitAndDistance)) + "] " + repeat("Ball detected", bestFitAndDistance);
			PDSMain.displayStrings[7] = "  Fit";
			PDSMain.displayStrings[8] = "  [" + repeat("*", 20*bestLinearFit) 
					+ repeat(" ", 20*(1-bestLinearFit)) + "] " + repeat("> 50%", bestLinearFit);
			PDSMain.displayStrings[9] = "  Distance";
			PDSMain.displayStrings[10] = "  [" + repeat("*", 20*bestDistanceMeasure) 
					+ repeat(" ", 20*(1-bestDistanceMeasure)) + "] " + repeat("> 50%", bestDistanceMeasure);
			PDSMain.statisticWindow.showText(PDSMain.displayStrings);
		}

		return bestFitAndDistance;
	}


	static final double ORDERING_ROUGHNESS = 0.1; // expressed in fraction of screen dimension

	/**
	 * Determines whether a Map of 3 points are roughly ordered
	 * @param points List of points (must be 3)
	 * @return Whether points are ordered
	 */
	private boolean areOrdered(Map<BallCandidate, Point> points) {
		double xSign = (points.get(BallCandidate.ONE).x - points.get(BallCandidate.TWO).x) < 0 ? -1 : 1;
		double ySign = (points.get(BallCandidate.TWO).y - points.get(BallCandidate.TWO).y) < 0 ? -1 : 1;

		if ((points.get(BallCandidate.TWO).x - points.get(BallCandidate.THREE).x)*xSign 
				< -1.0*ORDERING_ROUGHNESS*FrameNode.getCols()/2) {
			return false;
		}
		if ((points.get(BallCandidate.TWO).y - points.get(BallCandidate.THREE).y)*ySign 
				< -1.0*ORDERING_ROUGHNESS*FrameNode.getRows()/2) {
			return false;
		}
		return true;
	}


	/**
	 * Returns a deep-cloned / deep-copied List of Point types
	 */
	protected List<Point> deepCopy(List<Point> points) {
		List<Point> newList = new ArrayList<Point>();
		for (Point p: points) {
			newList.add(p.clone());
		}
		return newList;
	}


	/**
	 * Updates program data with the calculated game status.  
	 * Calls ConsoleOutput if the status has changed or if a time interval has expired.
	 * @param status The decided-upon status of the ping-pong table
	 */
	void update(GameStatus status) {
		if (aggregateStore.getGameStatus() != status) {  
			out.printStatus(status);
		}
		aggregateStore.setStatus(status, frameNumber);
	}


	/**
	 * Utility method for GUI display code
	 */
	private String repeat(String string, double roundedCount) {
		String repeated = new String("");
		long count = Math.round(roundedCount);
		for (int i=0; i<count; i++) {
			repeated += string;
		}
		return repeated;
	}
}