package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

import pds.input.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * This class runs tests ensuring that the runStatus of the input
 * console will stay true until given the correct input by the user
 * to change to false, thus terminating the program.
 * @author Bryan Schmier
 */
public class ConsoleInputTest {

	String quit = "quit";
	String message = "wrong command\nquit";
	String nothing = "\nquit";

	AtomicBoolean testRunStatus;

	@Test
	public void quitInputTest() {

		InputStream stdin = System.in;
		try {
			System.setIn(new ByteArrayInputStream(quit.getBytes()));

			testRunStatus = new AtomicBoolean(true);

			ConsoleInput test1 = new ConsoleInput(testRunStatus);
			
			test1.run();
			
			try{Thread.sleep(200);} //pause
			catch(Exception e){}
			
			assertEquals(false,test1.noQuitCommand());
			
			testRunStatus.set(false);

		}
		finally {
			System.setIn(stdin);
		}
	}

	@Test
	public void wrongInputTest() {

		InputStream stdin = System.in;
		PrintStream stdout = System.out;
		String outputText = ("Type 'quit' to close the application.\n");

		try {
			System.setIn(new ByteArrayInputStream(message.getBytes()));

			ByteArrayOutputStream outContent = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outContent));

			testRunStatus = new AtomicBoolean(true);

			ConsoleInput test2 = new ConsoleInput(testRunStatus);
			
			test2.run();
			
			try{Thread.sleep(200);} //pause
			catch(Exception e){}
			
			assertEquals(false,test2.noQuitCommand());//
			assertEquals(outputText, outContent.toString());
			
			testRunStatus.set(false);

		}
		finally {
			System.setIn(stdin);
			System.setOut(stdout);
		}

	}

	@Test
	public void nullInputTest() {

		InputStream stdin = System.in;
		PrintStream stdout = System.out;
		String outputText = ("Type 'quit' to close the application.\n");

		try {
			System.setIn(new ByteArrayInputStream(nothing.getBytes()));

			ByteArrayOutputStream outContent = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outContent));

			testRunStatus = new AtomicBoolean(true);

			ConsoleInput test3 = new ConsoleInput(testRunStatus);
			
			test3.run();
			
			try{Thread.sleep(200);} //pause
			catch(Exception e){}
			
			assertEquals(false,test3.noQuitCommand());//
			assertEquals(outputText, outContent.toString());
			
			testRunStatus.set(false);

		}
		finally {
			System.setIn(stdin);
			System.setOut(stdout);
		}

	}

}
