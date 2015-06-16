package test.pds.io.stubs;

import pds.output.ConsoleOutput;
import pds.situationAnalysis.AggregateStore.GameStatus;

/**
 * 
 * @author Chris Guinnup
 *
 */
public class ConsoleOutputStub extends ConsoleOutput {
	
	@Override
	public void error(String errStr) {}
	
	@Override
	public void printStatus(GameStatus s) {}
}
