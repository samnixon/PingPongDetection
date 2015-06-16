package pds.computerVisionService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.opencv.core.RotatedRect;

/**
 * A data structure for storing the raw contours of moving objects.
 * @author Chris Guinnup
 *
 */

public class BallStore {
	private ConcurrentSkipListMap<Long, List<RotatedRect>> ballStorage;
	
	private AtomicInteger size;
	private static final int CAPACITY = 60;
	private static final List<RotatedRect> JOB_SKIPPED = new ArrayList<RotatedRect>(); 

	
	/**
	 * Initializes the data structures
	 */
	public BallStore() {
		ballStorage = new ConcurrentSkipListMap<Long, List<RotatedRect>>();
		size = new AtomicInteger(0);
	}
	
	
	/**
	 * Add new frame difference value to the store
	 * @param frameNum The greater frame number of the two frames compared
	 * @param result Total gated difference
	 */
	public void addResult(Long frameNum, List<RotatedRect> result) {
		ballStorage.put(frameNum, result);
		trim();
	}
	
	
	/**
	 * Inform the store that a particular frame's calculation was skipped
	 * @param frameNum The greater frame number of the two frames (which would have been) compared
	 */
	public void skipJob(Long frameNum) {
		ballStorage.put(frameNum, JOB_SKIPPED);
		trim();
	}
	
	
	/***
	 * Updates size and removes any over-capacity node (oldest first)
	 */
	private void trim() {
		if (size.getAndIncrement() >= CAPACITY) {
			ballStorage.pollFirstEntry();
			size.decrementAndGet();
		}
	}
	
	
	/**
	 * Queries whether a particular frame's calculation was skipped
	 * @param frameNum The greater frame number of the two frames (which would have been) compared
	 * @return True if the calculation was skipped, false if the calculation is available
	 */
	public boolean wasSkipped(Long frameNum) {
		return (ballStorage.get(frameNum) == JOB_SKIPPED);
	}
	
	
	/**
	 * Gets the summed difference value for a particular frame comparison
	 * @param frameNum The greater frame number of the two frames compared
	 * @return The summed difference value
	 */
	public List<RotatedRect> getBoundingRectangles(Long frameNum) {
		return ballStorage.get(frameNum);
	}
}
