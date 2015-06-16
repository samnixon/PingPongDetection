package pds.computerVisionService;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint2f;

import pds.situationAnalysis.ImmediateNode;
import pds.situationAnalysis.ImmediateStatus;
import pds.situationAnalysis.StatusStores;
import pds.videoService.FrameNode;
import test.pds.computerVisionService.stubs.DiffStoreStub;

/**
 * Tests for the successful detection of the table from a sample image
 * @author Chris Guinnup
 *
 */

public class TableDetectTest {

	@Test
	public void test() {
		BufferedImage image = null;
		TableStore tableStore = new TableStore();
		CountDownLatch cdLatch = new CountDownLatch(1);
		TableDetect tableDetect; 
		List<MatOfPoint2f> results;
		
		try {
			image = ImageIO.read(new File("test/pds/computerVisionService/TableTest.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (image != null) {
			FrameNode.initDimensions(image.getWidth(), image.getHeight());
			byte [] rgb = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
			int [] integerRGB = new int[rgb.length/3];
			//Arrays.fill(int_rgb, 0);
			for (int i = 0; i <= rgb.length-3; i += 3) {
				int a = (int) rgb[i];
				int b = (int)(rgb[i+1] << 8);
				int c = (int)(rgb[i+2] << 16);
				integerRGB[i/3] = a + b + c;
			}
			CVFrame cvFrame;
			try {
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				cvFrame = CVFrame.make( new FrameNode(0, integerRGB) );
				tableDetect = new TableDetect(cvFrame, tableStore, cdLatch);
				tableDetect.run();
				
				results = tableStore.getTableResults(0L);
				assertTrue(results.size() > 0);				
			}
			catch(Exception e){
				e.printStackTrace();
				fail("Exception failure");
			}
			
			CVStores cvStores = new CVStores();
			cvStores.tableStore = tableStore;
			cvStores.diffStore = new DiffStoreStub();
			cvStores.ballStore = new BallStore();
			cvStores.racketStore = new RacketStore();
			StatusStores statStores = new StatusStores();
			ImmediateStatus immStat = new ImmediateStatus(0, cvStores, statStores);
			immStat.run();
			
			ImmediateNode immNode = statStores.immedStore.getProbabilities(0L);
			assertTrue( immNode.probTable > 0.5 );
		}
		else {
			fail("Could not read image!");
		}
	}
}
