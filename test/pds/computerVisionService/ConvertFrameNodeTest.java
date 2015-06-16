package pds.computerVisionService;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import pds.videoService.FrameNode;
import pds.computerVisionService.ConvertFrameNode;

public class ConvertFrameNodeTest {

	public ConvertFrameNodeTest(){
	}

	@Test
	public void TestConversion() throws Exception{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		FrameNode frame;

		Random r = new Random();
		int[] frameData;
		int rows;
		int cols;
		byte[] fromMat;
		Mat createdMat;
		int[] inputData;
		
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
		inputData = frame.getData();
		createdMat = ConvertFrameNode.convertFrameNodeToCV_GRAY(frame );
		assertEquals(rows, createdMat.rows());
		assertEquals(cols, createdMat.cols());
		fromMat = new byte[rows*cols];
		createdMat.get(0, 0, fromMat);
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				assertEquals(asGray(inputData[y*cols + x]), 0xFF&fromMat[y*cols + x],5);
			}
		}
		/*
		 * Test case with height greater than width
		 */
		rows = 20;
		cols = 10;
		FrameNode.initDimensions(cols, rows);
		frameData = new int[rows*cols];
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				frameData[y*cols + x] = r.nextInt();
			}
		}
		frame = new FrameNode(0, frameData);
		inputData = frame.getData();
		createdMat = ConvertFrameNode.convertFrameNodeToCV_GRAY(frame);
		assertEquals(rows, createdMat.rows());
		assertEquals(cols, createdMat.cols());
		fromMat = new byte[rows*cols];
		createdMat.get(0, 0, fromMat);
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				assertEquals(asGray(inputData[y*cols + x]), 0xFF&fromMat[y*cols + x],5);
			}
		}
		
		/*
		 * Test case with square matrix
		 */
		rows = 20;
		cols = 20;
		FrameNode.initDimensions(cols , rows);
		frameData = new int[rows*cols];
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				frameData[y*cols + x] = r.nextInt();
			}
		}
		frame = new FrameNode(0, frameData);
		inputData = frame.getData();
		createdMat = ConvertFrameNode.convertFrameNodeToCV_GRAY(frame);
		assertEquals(rows, createdMat.rows());
		assertEquals(cols, createdMat.cols());
		fromMat = new byte[rows*cols];
		createdMat.get(0, 0, fromMat);
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				assertEquals(asGray(inputData[y*cols + x]), 0xFF&fromMat[y*cols + x],5);
			}
		}
		
		/*
		 * Test case with very large (real use) matrix
		 */
		rows = 720;
		cols = 1280;
		FrameNode.initDimensions(cols, rows);
		frameData = new int[rows*cols];
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				frameData[y*cols + x] = r.nextInt();
			}
		}
		frame = new FrameNode(0, frameData);
		inputData = frame.getData();
		createdMat = ConvertFrameNode.convertFrameNodeToCV_GRAY(frame);
		assertEquals(rows, createdMat.rows());
		assertEquals(cols, createdMat.cols());
		fromMat = new byte[rows*cols];
		createdMat.get(0, 0, fromMat);
		for (int y = 0; y < rows; y++){
			for (int x = 0; x < cols; x++){
				assertEquals(asGray(inputData[y*cols + x]), 0xFF&fromMat[y*cols + x],5);
			}
		}
	}
	
	private int asGray(int in){
		int argb = in;
		int b = (argb & 0xFF);
		int g = ((argb >> 8 ) & 0xFF);
		int r = ((argb >> 16 ) & 0xFF);
		return (int)(.21*r + .72*g +.07*b);
		
	}

	public static void main(String[] args){

		ConvertFrameNodeTest cfnt = new ConvertFrameNodeTest();
		try {
			cfnt.TestConversion();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
