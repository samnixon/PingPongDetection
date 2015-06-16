package test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import pds.output.*;
import pds.situationAnalysis.AggregateStore.GameStatus;

import org.junit.Test;

/**
 * This class runs tests for correct output to the console,
 * as well as a separate test, proving that the timestamp
 * is up-to-date on every print statement.
 * @author Bryan Schmier
 */
public class ConsoleOutputTest {

	String inUseText = ("Ping pong game is in progress.\n");
	String freeText = ("The ping pong table is available.\n");
	String noTableText = ("Camera no longer pointing at the table.\n");
	String cameraErrorText = ("Error: Camera.\n");

	@Test
	public void InUseOutputTest() {
		PrintStream stdout = System.out;

		try {
			ByteArrayOutputStream outContent = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outContent));

			ConsoleOutput test1 = new ConsoleOutput();

			test1.printStatus(GameStatus.InUse);

			assertTrue(outContent.toString().endsWith(inUseText));
		}
		finally {
			System.setOut(stdout);
		}
	}
	
	@Test
	public void freeOutputTest() {
		PrintStream stdout = System.out;

		try {
			ByteArrayOutputStream outContent = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outContent));

			ConsoleOutput test1 = new ConsoleOutput();

			test1.printStatus(GameStatus.Free);

			assertTrue(outContent.toString().endsWith(freeText));
		}
		finally {
			System.setOut(stdout);
		}
	}
	
	@Test
	public void noTableOutputTest() {
		PrintStream stdout = System.out;

		try {
			ByteArrayOutputStream outContent = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outContent));

			ConsoleOutput test1 = new ConsoleOutput();

			test1.printStatus(GameStatus.NoTable);

			assertTrue(outContent.toString().endsWith(noTableText));
		}
		finally {
			System.setOut(stdout);
		}
	}
	
	@Test
	public void cameraErrourOutputTest() {
		PrintStream stdout = System.out;

		try {
			ByteArrayOutputStream outContent = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outContent));

			ConsoleOutput test1 = new ConsoleOutput();

			test1.printStatus(GameStatus.CameraError);

			assertTrue(outContent.toString().endsWith(cameraErrorText));
		}
		finally {
			System.setOut(stdout);
		}
	}
	
	@Test
	public void timestampTest() {
		PrintStream stdout = System.out;

		try {
			ByteArrayOutputStream outContent = new ByteArrayOutputStream();
			System.setOut(new PrintStream(outContent));

			ConsoleOutput test1 = new ConsoleOutput();

			test1.printStatus(GameStatus.NoTable);
			
			try{Thread.sleep(100);} //pause for 100 milliseconds
			catch(Exception e){}
			
			ByteArrayOutputStream out2Content = new ByteArrayOutputStream();
			System.setOut(new PrintStream(out2Content));
			
			ConsoleOutput test2 = new ConsoleOutput();
			
			test2.printStatus(GameStatus.NoTable);
			
			assertTrue(!(outContent.toString().equals(out2Content.toString())));
		}
		finally {
			System.setOut(stdout);
		}
	}

}
