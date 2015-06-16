package pds.videoService;

/**
 * A simple class used to hold formatted image data obtained from VLCJ
 * @author Sam Nixon
 *
 */
public class FrameNode {

	protected static int cols = 0;
	protected static int rows = 0;

	private long frameNumber; 
	private int[] data;

	public FrameNode(long frameNumber, int[] tempData) throws Exception{
		if (cols != 0 && rows != 0) {
			this.frameNumber = frameNumber;
			this.data = tempData.clone();
		}
		else {
			throw new Exception("Initialize video dimensions via FrameNode.initDimensions(x,y) before creating any frames!");
		}
	}
	
	/**
	 * Get the frame's image data
	 * @return Video frame as an int array
	 */
	public int[] getData(){
		return data;
	}
	
	/**
	 * Get the sequence number of this frame
	 * @return Frame sequence number
	 */
	public long getFrameNumber(){
		return frameNumber;
	}
	
	/**
	 * Call only once to initialize the video dimensions
	 * @param inCols Columns in pixels
	 * @param inRows Rows in pixels
	 */
	public static void initDimensions(int inCols, int inRows) {
		//if (cols == 0 && rows == 0) {
			cols = inCols;
			rows = inRows;
		//}
	}
	
	/**
	 * Get column dimension
	 * @return Number of columns in pixels
	 */
	public static int getCols() {
		return cols;
	}
	
	/**
	 * Get row dimension
	 * @return Number of rows in pixels
	 */
	public static int getRows() {
		return rows;
	}
}
