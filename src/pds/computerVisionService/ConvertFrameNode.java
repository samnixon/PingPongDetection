package pds.computerVisionService;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import pds.videoService.FrameNode;
/**
 * Allows for fast conversion of a color VLC FrameNode to a grayscale Mat usable
 * in OpenCV
 * @author Sam Nixon
 *
 */
public abstract class ConvertFrameNode {

	/**
	 * Takes as input a FrameNode and produces a Mat of equal dimensions. 
	 * Data type of the produced Mat is CVType_CVU8, single channel.
	 * Conversion is done in parallel to increase performance
	 * @param input
	 * @return Mat
	 */
	public static Mat convertFrameNodeToCV_GRAY(FrameNode input){

		int rows = FrameNode.getRows();
		int cols = FrameNode.getCols();
		Mat temp = new Mat();
		//create allocates space in the Mat for rows*cols*(sizeof(dataType))
		temp.create(rows, cols, CvType.CV_8U);
		ExecutorService executor = Executors.newFixedThreadPool(4);
		try {
			executor.invokeAll(asList(
					new ParallelConversion(0, rows/4, temp, input), 
					new ParallelConversion(rows/4,  rows/2, temp, input),
					new ParallelConversion(rows/2,  3*(rows/4), temp, input),
					new ParallelConversion(3*(rows/4),  rows, temp, input)
					));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdown();
		return temp;
	}



	/**
	 * Loads data from input FrameNode into destination Mat.
	 * Used to execute a load in parallel
	 */
	private static class ParallelConversion implements Callable<Object> {
		private final int lowerBound;
		private final int upperBound;
		private final Mat dest;
		private FrameNode input;
		/**
		 * 
		 * @param lowerBound
		 * @param upperBound
		 * @param dest
		 * @param input
		 */
		private ParallelConversion(int lowerBound, int upperBound, Mat dest, FrameNode input) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			this.dest = dest;
			this.input = input;
		}

		/**
		 * Performs a grayscale conversion on rows lowerBound through upperBound.
		 * Grayscale conversion is a simple weighted sum
		 */
		@Override
		public Object call() {
			int[] inputData = input.getData();
			for (int y = lowerBound; y < upperBound; y++){
				for (int x = 0; x < FrameNode.getCols(); x++){
					int argb = inputData[y*FrameNode.getCols() + x];
					int b = (argb & 0xFF);
					int g = ((argb >> 8 ) & 0xFF);
					int r = ((argb >> 16 ) & 0xFF);
					dest.put( y, x,(.21*r + .72*g +.07*b));
				}
			}

			return null;
		}                
	}
}
