package test.pds.videoService.stubs;

import java.util.Random;

import pds.videoService.FrameNode;
import pds.videoService.FrameStore;

/**
 * 
 * @author Chris Guinnup
 *
 */

public class FrameStoreStub extends FrameStore {

	private Random rand;
	
	public FrameStoreStub(int rows, int cols) {
		super(rows, cols);
		rand = new Random();
		rand.setSeed(System.currentTimeMillis());
	}
	
	@Override
	public FrameNode getFrame() {
		FrameNode frame = null;
		int [] data;
		int rows = FrameNode.getRows();
		int cols = FrameNode.getCols();
		data = new int[cols*rows];
		
		for (int x=0; x<cols*rows; x++) {
			data[x] = rand.nextInt();
		}
		try {
			frame = new FrameNode(frameNumber, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		frameNumber++;
		return frame;
	}
	
	/*public void putFrameStub() {
	frameNumber++;	
	}*/
}
