package pds.computerVisionService;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import pds.videoService.FrameNode;
/**
 * 
 * Immutable class that allows for single conversion of a VLCJ FrameNode
 * to a CVFrame for use with OpenCV
 * @author Sam Nixon
 *
 */
public final class CVFrame {
	
	private final Mat data;
	private final Mat smallData;
	private final long frameNumber;
	private final Size dimensions;
	
	public static CVFrame make(FrameNode frame){
		return frame != null ? new CVFrame(frame) : null;
	}
	
	private CVFrame(FrameNode frame){
		Mat temp = new Mat();
		this.data = ConvertFrameNode.convertFrameNodeToCV_GRAY(frame);
		this.frameNumber = frame.getFrameNumber();
		this.dimensions = data.size();
		Imgproc.pyrDown(this.data, temp);
		this.smallData = temp;
	}
	
	/**
	 * @return A copy of the Mat contained within this class.
	 */
	public Mat getData(){
		return data.clone();
	}
	
	/**
	 * @return A copy of the Mat screen data, half-size on each axis.
	 */
	public Mat getSmallData() {
		return smallData.clone();
	}
	
	/**
	 * @return Frame number of the FrameNode that this class was made from
	 */
	public long getFrameNumber(){
		return frameNumber;
	}
	
	/**
	 * An OpenCV Size denoting the dimensions of the Mat data
	 * @return
	 */
	public Size getDimensions(){
		return dimensions;
	}
	
}
