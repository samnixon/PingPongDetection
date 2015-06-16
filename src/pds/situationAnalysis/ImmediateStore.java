package pds.situationAnalysis;

import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A data structure which stores the estimated event probabilities from immediate frame analysis and comparisons.
 * @author Chris Guinnup
 *
 */
public class ImmediateStore {
	private ConcurrentSkipListMap<Long, ImmediateNode> immedStore;
	
	AtomicInteger size;
	static final int CAPACITY = 60;
	
	
	/**
	 * Initializes the data structures
	 */
	public ImmediateStore() {
		immedStore = new ConcurrentSkipListMap<Long, ImmediateNode>();
		size = new AtomicInteger(0);
	}
	
	
	/**
	 * Stores the passed immediate results for the passed frame number
	 */
	public void addResult(Long frameNum, ImmediateNode probs) {
		immedStore.put(frameNum, probs);
		trim();
	}
	
	
	/**
	 * Get the immediate results for a specified frame
	 * @param frameNum The frame number desired
	 * @return The immediate result's probabilities
	 */
	public ImmediateNode getProbabilities(Long frameNum) { 
		return immedStore.get(frameNum);
	}
	
	
	/**
	 * Get immediate results for the n most recent frames
	 * @param count Number of frames desired
	 * @return An array of immediate results.  Null if not enough exist.
	 */
	public ImmediateNode [] getMultipleProbabilities(int count) {
		if (count <= size.get()) {
			ImmediateNode [] result = new ImmediateNode [count];
			Iterator<Long> it = immedStore.descendingKeySet().iterator();
			for (int a=count-1; a>=0; a--) {
				result[a] = immedStore.get(it.next());
			}
			return result;
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Get the number of elements in the store
	 * @return The number of immediate results elements
	 */
	public int getSize() {
		return size.get();
	}
	
	
	/***
	 * Update size and remove any old over-capacity node
	 */
	private void trim() {
		if (size.getAndIncrement() >= CAPACITY) {
			immedStore.pollFirstEntry();
			size.decrementAndGet();
		}
	}
}