package pds.videoService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * A data structure that acts as the interface between VLCJ and OpenCV.
 * @author Sam Nixon
 *
 */
public class FrameStore {

	protected long frameNumber;
	private BlockingQueue<FrameNode> storage;
	private final int CAPACITY = 5;
	
	public FrameStore(int rows, int cols){
		this.storage = new LinkedBlockingQueue<FrameNode>();
		this.frameNumber = 0;
		FrameNode.initDimensions(cols, rows);		
	}
	/**
	 * @return A Queue containing FrameNodes
	 */
	public BlockingQueue<FrameNode> getStorage(){
		return storage;
	}
	/**
	 * @return The oldest frame currently in the Queue
	 */
	public FrameNode getFrame(){
		return storage.poll(); 	
	}
	/**
	 * @return true if the maximum number of FrameNodes allowed is held
	 */
	public boolean atCapacity(){
		return storage.size() >= CAPACITY;
	}
	/**
	 * Creates and adds a new FrameNode to storage. Old FrameNodes are removed
	 * if the Queue is at capacity
	 * @param data
	 * @throws Exception
	 */
	public void putFrame(int[] data) throws Exception{
		if (atCapacity()){
			storage.poll();
		}
		FrameNode frame = new FrameNode(++frameNumber, data);
		storage.offer(frame);
	}
	/**
	 * @return Number of elements in the Queue
	 */
	public int getSize(){
		return storage.size();
	}

}
