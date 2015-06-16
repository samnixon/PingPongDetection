package pds.computerVisionService;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import pds.computerVisionService.CVFrame;
import pds.computerVisionService.ConvertFrameNode;
import pds.videoService.FrameNode;

public class CVFrameTest {

	@Test
	public void testCVFrame() throws Exception{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		FrameNode frame;

		Random r = new Random();
		int[] frameData;
		int rows;
		int cols;
		Mat createdMat;
		
		/*
		 * Test case with a rectangular input with width greater than height
		 */
		rows = 10;
		cols = 20;
		FrameNode.initDimensions(cols, rows);
		frameData = new int[rows*cols];
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				frameData[y*cols + x] = r.nextInt();
			}
		}
		frame = new FrameNode(0, frameData);
		createdMat = ConvertFrameNode.convertFrameNodeToCV_GRAY(frame );
		assertEquals(rows, createdMat.rows());
		assertEquals(cols, createdMat.cols());
		CVFrame cvFrame = CVFrame.make(frame);
		Mat cvFrameData = cvFrame.getData();
		Mat dif = new Mat();
		Core.subtract(createdMat, cvFrameData, dif);
		double[] differences = Core.sumElems(dif).val;
		for (double d : differences){
			assertEquals((int)d, 0);
		}
	}
}
