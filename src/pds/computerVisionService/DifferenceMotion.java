package pds.computerVisionService;

import java.util.concurrent.CountDownLatch;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Quick method of detecting motion between two frames based on summed, gated difference.
 * @author Chris Guinnup
 *
 */

public class DifferenceMotion implements Runnable {

	CVFrame frame1, frame2;
	DifferenceStore diffStore;
	CountDownLatch cdLatch;
	
	/**
	 * Initializes the thread with its necessary references
	 */
	public DifferenceMotion(CVFrame previousFrame, CVFrame currentFrame, DifferenceStore diffStore, CountDownLatch cdLatch) {
		this.diffStore = diffStore;
		this.frame1 = previousFrame;
		this.frame2 = currentFrame;
		this.cdLatch = cdLatch;
	}
	
	/**
	 * Runs algorithm for weighted difference between a pair of frames
	 */
	@Override
	public void run() {
		// algorithmic code here 
		// using frame1 and frame2 as input and diffStore as output
		long frameNum = frame2.getFrameNumber();
		Scalar sum;
		
		Mat difference = new Mat(); 
		Mat multiplied = new Mat();
		Mat postThreshold = new Mat();
		
		Core.subtract(frame1.getData(), frame2.getData(), difference);
		Core.multiply(difference, new Scalar(3), multiplied);
		Imgproc.threshold(multiplied, postThreshold, 100, 255, Imgproc.THRESH_TOZERO);
		sum = Core.sumElems(postThreshold);
		
		diffStore.addResult(frameNum, sum.val[0]);
		
		// sanity check
		/*if (frameNum%10 == 0) {
			System.out.println("(Frame "+frameNum+") WeightedDiff = " + sum.val[0]); 
		}*/
	
		// tells main thread that this method is finished
		cdLatch.countDown();
	}

}

