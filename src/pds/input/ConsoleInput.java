package pds.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class starts the run thread, and handles
 * input from the user for termination of the program.
 * @author Bryan Schmier
 */
public class ConsoleInput extends Thread{
	protected AtomicBoolean runStatus;

	/**
	 * Sets the the atomic boolean runStatus.
	 * @param runStatus
	 */
	public ConsoleInput(AtomicBoolean runStatus) {
		this.runStatus = runStatus;
	}

	/**
	 * Returns a boolean of the current runStatus.
	 * @return
	 */
	public boolean noQuitCommand() {
		return runStatus.get();
	}

	/**Launch single thread which checks for user input 
	 * and changes runStatus upon quit command.
	 */
	public void run(){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		String userInput;

		while(runStatus.get() == true){ //while runStatus = true
			try {
				userInput = reader.readLine();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				break;
			}

			if (userInput.equals("quit")){
				runStatus.set(false); //terminates application
			}
			if (!(userInput.equals("quit") || userInput == null)){
				//if not "quit", output "Type 'quit' to close the application"
				System.out.println("Type 'quit' to close the application.");
				try{Thread.sleep(1000);} //pause for 1 second
				catch(Exception e){}
			}
		}
	}
}
