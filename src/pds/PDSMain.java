package pds;

import pds.computerVisionService.CVFrame;
import pds.computerVisionService.CVStores;
import pds.computerVisionService.BallDetect;
import pds.computerVisionService.TableDetect;
import pds.computerVisionService.DifferenceMotion;
import pds.computerVisionService.MatDisplay;
import pds.computerVisionService.RacketDetection;
import pds.input.ConsoleInput;
import pds.output.ConsoleOutput;
import pds.situationAnalysis.AggregateStatus;
import pds.situationAnalysis.ImmediateStatus;
import pds.situationAnalysis.StatusStores;
import pds.videoService.FrameStore;
import pds.videoService.VLCJVideoService;
import pds.videoService.VideoServiceIF;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Contains the main program loop
 * @author Christopher Guinnup, contributions from Sam Nixon
 */
public class PDSMain {

	protected static VideoServiceIF videoSvc;
	protected static FrameStore frameStore;
	protected static CVStores cvStores;
	protected static StatusStores statusStores;
	protected static ConsoleInput in;
	protected static ConsoleOutput out;
	
	protected static AtomicBoolean runStatus;
	protected static CVFrame currentFrame;
	
	// for manual testing & debugging
	public static final MatDisplay statisticWindow = new MatDisplay(
			"Aggregate Statistics", MatDisplay.DEFAULT_WIDTH+18, 0);
	public static String[] displayStrings = 
			new String[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "" };
	
	// constants
	protected static final int MAX_THREADS = 8;
	protected static final int RECHECK_PAUSE = 1000/(30*4); // in milliseconds (30 fps)
	
	protected static final int ALL_JOBS = 4;
	protected static final int NO_TABLE_JOBS = 3;

	/** 
	 * Initializes the program then starts the program loop.
	 */
	public static void main(final String[] args) throws InterruptedException {
		out = new ConsoleOutput();
		
		if (args != null && args.length > 0) {
			try {
				initialize(args);
				programLoop();
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				out.error("Could not decode the input file! Please specify a good media file location.");
				return;
			}
		}
		else {
			out.error("Please run PDS with arguments for media location and the vlclib.dll path!");
			return;
		}
	}
	
	
	/**
	 * The main program loop.  Responsible for launching image-processing & analysis threads.
	 */
	protected static void programLoop() throws InterruptedException {
		ExecutorService e = Executors.newFixedThreadPool(MAX_THREADS);
		CountDownLatch youShallNotPass = null;
		CVFrame previousFrame;
		// for manual video testing
		//MatDisplay display = new MatDisplay("Racket Display");
		long frameLastPrinted = 0;
		List<Long> framesSkipped = new ArrayList<Long>();
		
		// Check for frame & do first frame grab
		previousFrame = CVFrame.make(frameStore.getFrame());
		while (previousFrame == null) {
			Thread.sleep(RECHECK_PAUSE);
			previousFrame = CVFrame.make(frameStore.getFrame());
		}
		
		while (runStatus.get()) {
			
			// Grab frame, if not there then loop checking for it  
			currentFrame = CVFrame.make(frameStore.getFrame());
			while (currentFrame == null) {
				Thread.sleep(RECHECK_PAUSE);
				currentFrame = CVFrame.make(frameStore.getFrame());
			}
			
			// sanity check
			//if (currentFrame.getFrameNumber()%10 == 0)
			//	System.out.println("frame "+currentFrame.getFrameNumber());
			
			// process frame based on high-level situation 
			switch(statusStores.aggStore.getGameStatus())
			{
			case InUse:
			case Free:
			case Start:
				// check for all objects & indicators
				youShallNotPass = new CountDownLatch(ALL_JOBS);
				e.execute( new DifferenceMotion(previousFrame, currentFrame, cvStores.diffStore, youShallNotPass));
				e.execute( new TableDetect(currentFrame, cvStores.tableStore, youShallNotPass));
				e.execute( new BallDetect(previousFrame, currentFrame, cvStores.ballStore, youShallNotPass));
				e.execute( new RacketDetection(currentFrame, cvStores.racketStore, youShallNotPass));

				break;
			case NoTable:
			case CameraError:
				// check for both table & for camera error
				youShallNotPass = new CountDownLatch(NO_TABLE_JOBS);
				e.execute(new TableDetect(currentFrame, cvStores.tableStore, youShallNotPass));
				e.execute( new BallDetect(previousFrame, currentFrame, cvStores.ballStore, youShallNotPass));
				e.execute( new RacketDetection(currentFrame, cvStores.racketStore, youShallNotPass));


				//cvStores.ballStore.skipJob(currentFrame.getFrameNumber());
				cvStores.diffStore.skipJob(currentFrame.getFrameNumber());
				//TODO: check for camera error
				break;
			default:
				 out.error("Main loop switch: Unexpected enum!");
				 System.exit(-1);
			}
			
			// Wait until cvLib jobs done.  Then run status analysis (w/o blocking the fetching & processing of next frame).
			youShallNotPass.await();
			e.execute( new ImmediateStatus(currentFrame.getFrameNumber(), cvStores, statusStores) );
			//System.out.println("Frames skipped: "+(currentFrame.getFrameNumber()-previousFrame.getFrameNumber()-1));
			
			// for manual video test statistics
			framesSkipped.add(currentFrame.getFrameNumber() - previousFrame.getFrameNumber()-1);
			if (currentFrame.getFrameNumber() - frameLastPrinted >= 60) {
				double sumFramesSkipped = 0.0;
				for (long a: framesSkipped) {
					sumFramesSkipped += a;
				}
				displayStrings[12] = "Skipped Frames";
				displayStrings[13] = new DecimalFormat("#0.0").format(100.0*sumFramesSkipped
						/(currentFrame.getFrameNumber() - frameLastPrinted)) + "%";
				statisticWindow.showText(displayStrings);
				frameLastPrinted = currentFrame.getFrameNumber();
				framesSkipped.clear();
			}
			
			// prepare for next loop
			previousFrame = currentFrame;
		}
		//System.exit(0);  // aborts all JUnit testing, probably a bad idea
	}

	
	/**
	 * Performs program initialization
	 * @param args Command-line arguments 
	 */
	private static void initialize(final String [] args) throws FileNotFoundException {
		videoSvc = VLCJVideoService.createVideoSvc(args);
		frameStore = videoSvc.getStorage();
		runStatus = new AtomicBoolean(true);
		in = new ConsoleInput(runStatus);
		statusStores = new StatusStores();
		AggregateStatus.initConsoleOutput(out);
		cvStores = new CVStores();
		in.start();
		videoSvc.startVideoSvc();
	}
	

	/**
	 *  Signals program shut down - for JUnit testing 
	 */
	public static void shutDown() {
		runStatus.set(false);
	}
}
