package pds;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * Tests that occur over the main program loop
 * @author Chris Guinnup
 *
 */
public class PDSMainTest {
	
	/**
	 * Tests the ability of the main program loop to successfully run without deadlocking
	 * @throws InterruptedException
	 */
	@Test
	public void testProgramLoop() throws InterruptedException {
		Thread mainLoop = new Thread() { 
			public void run() { 
				try {
					PDSMainStub.programLoop();
				} catch (InterruptedException e) {
					e.printStackTrace();
		}}};
		long time = 0;
		
		PDSMainStub.initialize();
		mainLoop.start();
		while (PDSMainStub.currentFrame == null || PDSMainStub.currentFrame.getFrameNumber() < 10) {
				Thread.sleep(25);
				time += 25;
				if (time >= 2000) {
					fail("Timeout elapsed!");
					break;
				}
		}
		assertTrue(PDSMainStub.currentFrame.getFrameNumber() >= 10);
		PDSMainStub.shutDown();
	}
	
	
	/**
	 * Tests output (and makes sure program terminates) when given bad filename
	 * @throws InterruptedException 
	 */
	@Test
	public void testBadInput() throws InterruptedException {
		final String badFileError = 
				"Could not decode the input file! Please specify a good media file location.";
		String [] args = new String[] { "9n2y39bb8y2bcx38" };
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		PrintStream stderr = System.err;
		
		try {
			System.setErr(new PrintStream(outContent));
			PDSMain.main(args);
		}
		finally {
			System.setErr(stderr);
		}
		
		assertTrue(outContent.toString().contains(badFileError));
	}
}
