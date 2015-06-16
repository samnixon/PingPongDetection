package pds.computerVisionService;

import org.opencv.core.Core;

/**
 * A struct-like container for all the different OpenCV output stores
 * @author Chris Guinnup
 */
public class CVStores {
	
	public DifferenceStore diffStore;
	public BallStore ballStore;
	public TableStore tableStore;
	public RacketStore racketStore;
	
	private static final int NUMBER_OF_STORES = 4;  // for latch counter
	
	public CVStores() {
		loadOpenCV();
		diffStore = new DifferenceStore();
		ballStore = new BallStore();
		tableStore = new TableStore();
		racketStore = new RacketStore();
	}
	
	private void loadOpenCV() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	int getNumStores() {
		return NUMBER_OF_STORES;
	}
}