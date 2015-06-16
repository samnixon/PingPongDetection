package test.videoService;

import pds.videoService.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class VideoSvcTest {

	public VideoSvcTest(){
		
	}
	
	@Test
	public void testFrameStore() throws Exception{
		FrameStore fs = new FrameStore(720, 1280);
		assertTrue(fs.getSize() == 0);
		int[] frame1 = new int[25];
		for (int i = 0; i < 5; i++){
			for (int a = 0; a < frame1.length; a++){
				frame1[a] = a + i;
			}
			fs.putFrame(frame1);
		}
		assertTrue(fs.getSize() == 5);
		int[] frame2;
		for (int i = 0; i < 5; i++){
			frame2 = fs.getFrame().getData();
			for (int a = 0; a < frame1.length; a++){
				assertEquals(a + i, frame2[a]);
			}
			fs.putFrame(frame1);
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		VideoSvcTest vsc = new VideoSvcTest();
		vsc.testFrameStore();
	}
	
}
