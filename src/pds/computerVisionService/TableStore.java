package pds.computerVisionService;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.opencv.core.MatOfPoint2f;

/**
 * A data structure for storing raw contour data from the DetectTable algorithm.
 * @author Chris Guinnup
 *
 */

public class TableStore {
	private ConcurrentSkipListMap<Long, ArrayList<MatOfPoint2f>> tableStorage;
	
	private AtomicInteger size;
	private static final int CAPACITY = 150;
	private static final ArrayList<MatOfPoint2f> JOB_SKIPPED = null; 

	
	/**
	 * Initializes the data structures
	 */
	public TableStore() {
		tableStorage = new ConcurrentSkipListMap<Long, ArrayList<MatOfPoint2f>>();
		size = new AtomicInteger(0);
	}
	
	
	/**
	 * Add new frame difference value to the store
	 * @param frameNum The greater frame number of the two frames compared
	 * @param result Total gated difference
	 */
	public void addResult(Long frameNum, ArrayList<MatOfPoint2f> result) {
		tableStorage.put(frameNum, result);
		trim();
	}
	
	
	/**
	 * Inform the store that a particular frame's calculation was skipped
	 * @param frameNum The greater frame number of the two frames (which would have been) compared
	 */
	public void skipJob(Long frameNum) {
		tableStorage.put(frameNum, JOB_SKIPPED);
		trim();
	}
	
	
	/***
	 * Updates size and removes any over-capacity node (oldest first)
	 */
	private void trim() {
		if (size.getAndIncrement() >= CAPACITY) {
			tableStorage.pollFirstEntry();
			size.decrementAndGet();
		}
	}
	
	
	/**
	 * Queries whether a particular frame's calculation was skipped
	 * @param frameNum The greater frame number of the two frames (which would have been) compared
	 * @return True if the calculation was skipped, false if the calculation is available
	 */
	public boolean wasSkipped(Long frameNum) {
		return (tableStorage.get(frameNum) == JOB_SKIPPED);
	}
	
	
	/**
	 * Gets the summed difference value for a particular frame comparison
	 * @param frameNum The greater frame number of the two frames compared
	 * @return The summed difference value
	 */
	public ArrayList<MatOfPoint2f> getTableResults(Long frameNum) {
		return tableStorage.get(frameNum);
	}
}
