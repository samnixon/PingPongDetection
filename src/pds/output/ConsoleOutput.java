package pds.output;

import java.util.Calendar;
import java.sql.Timestamp;

import pds.situationAnalysis.AggregateStore.GameStatus;

/**
 * Class that handles the operations for output to the
 * console, notifying employees of the current table status.
 * @author Bryan Schmier
 */
public class ConsoleOutput {
	
	/**
	 * Prints an error message to error output
	 * @param errStr
	 */
	public void error(String errStr) {
		System.err.println("\nERROR: " + errStr + '\n');
	}
	
	/**
	 * Prints the status of the ping-pong game to the console
	 * depending on the current GameStatus case.
	 * @param s
	 */
	public void printStatus(GameStatus s) {
		Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
		switch(s) {
		case InUse:
			System.out.println(currentTimestamp +"  " +
		"Ping pong game is in progress.");
			break;

		case Free:
			System.out.println(currentTimestamp +"  " +
		"The ping pong table is available.");
			break;

		case NoTable:
			System.out.println(currentTimestamp +"  " +
		"Camera no longer pointing at the table.");
			break;

		case CameraError:
			System.out.println(currentTimestamp +"  " +
		"Error: Camera.");
			break;
			
		default:
			break;
		}
	}
}
