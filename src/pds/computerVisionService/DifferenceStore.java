package pds.computerVisionService;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A data structure for storing the raw sum-values of the DiffMotion algorithm.
 * @author Chris Guinnup
 *
 */

public class DifferenceStore {
	private ConcurrentSkipListMap<Long, Double> diffStorage;
	
	private AtomicInteger size;
	private static final int CAPACITY = 60;
	private static final double JOB_SKIPPED = -1; //*must* be negative

	
	/**
	 * Initializes the data structures
	 */
	public DifferenceStore() {
		diffStorage = new ConcurrentSkipListMap<Long, Double>();
		size = new AtomicInteger(0);
	}
	
	
	/**
	 * Add new frame difference value to the store
	 * @param frameNum The greater frame number of the two frames compared
	 * @param result Total gated difference
	 */
	public void addResult(Long frameNum, Double result) {
		diffStorage.put(frameNum, result);
		trim();
	}
	
	
	/**
	 * Inform the store that a particular frame's calculation was skipped
	 * @param frameNum The greater frame number of the two frames (which would have been) compared
	 */
	public void skipJob(Long frameNum) {
		diffStorage.put(frameNum, JOB_SKIPPED);
		trim();
	}
	
	
	/***
	 * Updates size and removes any over-capacity node (oldest first)
	 */
	private void trim() {
		if (size.getAndIncrement() >= CAPACITY) {
			diffStorage.pollFirstEntry();
			size.decrementAndGet();
		}
	}
	
	
	/**
	 * Queries whether a particular frame's calculation was skipped
	 * @param frameNum The greater frame number of the two frames (which would have been) compared
	 * @return True if the calculation was skipped, false if the calculation is available
	 */
	public boolean wasSkipped(Long frameNum) {
		return (diffStorage.get(frameNum) == JOB_SKIPPED);
	}
	
	
	/**
	 * Gets the summed difference value for a particular frame comparison
	 * @param frameNum The greater frame number of the two frames compared
	 * @return The summed difference value
	 */
	public double getDifference(Long frameNum) {
		return diffStorage.get(frameNum);
	}
}
