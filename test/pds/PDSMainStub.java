package pds;

import java.util.concurrent.atomic.AtomicBoolean;

import pds.computerVisionService.CVStores;
import pds.situationAnalysis.AggregateStatus;
import pds.situationAnalysis.StatusStores;
import test.pds.io.stubs.ConsoleInputStub;
import test.pds.io.stubs.ConsoleOutputStub;
import test.pds.videoService.stubs.FrameStoreStub;

/**
 * @author Chris Guinnup
 */

public class PDSMainStub extends PDSMain {

	// can't override static methods
	/**
	 * Initializes as many stubs as possible in place of program classes
	 */
	public static void initialize() { 
		int rows = 8, cols = 8;
		
		runStatus = new AtomicBoolean(true);
		frameStore =  new FrameStoreStub(rows, cols); 
		in = new ConsoleInputStub(runStatus);
		statusStores = new StatusStores();
		cvStores = new CVStores();
		out = new ConsoleOutputStub();
		AggregateStatus.initConsoleOutput(out);

		// never launches these lines from original function:
		/*videoSvc = VLCJVideoSvc.createVideoSvc(args);
		in.start();
		videoSvc.startVideoSvc();*/
	}
}
