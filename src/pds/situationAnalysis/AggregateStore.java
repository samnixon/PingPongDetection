package pds.situationAnalysis;

/**
 * A data structure which stores the final game status and 
 * any last occurrences of each status type.
 * @author Chris Guinnup
 *
 */
public class AggregateStore {
	public static final long NONE = -1;
	public enum GameStatus { InUse, Free, NoTable, CameraError, Start }
	
	private GameStatus lastStatus;
	private long statusFrame[];
	private long potentialBallEvent;
	
	/**
	 * Initializes the data structures
	 */
	public AggregateStore() {
		int length = GameStatus.values().length;
		statusFrame = new long[length];
		for (int i=0; i<length; i++) { 
			statusFrame[i] = NONE;
		}
		lastStatus = GameStatus.Start;
		//potentialNewEvent = NONE;
	}
	
	
	/**
	 * Gets the current game status
	 * @return Current game status
	 */
	public GameStatus getGameStatus() {
		return lastStatus;
	}
	
	
	/**
	 * Retrieves the most recent frame number at which a given status occurred.
	 * @param status Status sought 
	 * @return The frame number
	 */
	public long getStatusFrameNum(GameStatus status) {
		return statusFrame[status.ordinal()];
	}
		
	
	/**
	 * Records the fact that a certain game status occurred at a particular frame number.
	 */
	public void setStatus(GameStatus status, long frameNum) {
		lastStatus = status;
		statusFrame[status.ordinal()] = frameNum;
	}

	
	/**
	 * Gets the last frame number of a potential ball event
	 */
	public long potentialBallEventAt() {
		return potentialBallEvent;
	}

	/**
	 * Stores that a potential-ball was detected, so that the rest of the program can respond appropriately
	 * @param frameNum Frame number at which the potential-ball detection occurred
	 */
	protected void setPotentialBallEvent(long frameNum) {
		potentialBallEvent = frameNum;
	}
	/** Stores that a potential-racket was detected, so that the rest of the program can respond appropriately
	 * @param frameNum Frame number at which the potential-ball detection occurred
	 */
	protected void setPotentialRacketEvent(long frameNum) {
		potentialBallEvent = frameNum;
	}
}
