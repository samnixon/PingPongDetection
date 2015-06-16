package test.pds.io.stubs;

import java.util.concurrent.atomic.AtomicBoolean;

import pds.input.ConsoleInput;

/**
 * 
 * @author Chris Guinnup
 *
 */
public class ConsoleInputStub extends ConsoleInput {

	public ConsoleInputStub(AtomicBoolean runStatus) {
		super(runStatus);
	}

	@Override
	public void run() {
	}
	
	public void setExecution(boolean in) {
		runStatus.set(in);
	}
}
