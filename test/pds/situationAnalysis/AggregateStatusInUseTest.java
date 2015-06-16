package pds.situationAnalysis;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Point;

import pds.situationAnalysis.AggregateStore.GameStatus;
import test.pds.computerVisionService.stubs.CVStoresStub;
import test.pds.io.stubs.ConsoleOutputStub;


/**
 * 
 * @author Chris Guinnup
 *
 */
public class AggregateStatusInUseTest {
	AggregateStatus aggStat; 
	StatusStores statStores;
	ImmediateStore immStore;
	ImmediateNode immNode;

	
	@Before
	public void before() {
		statStores = new StatusStores();
		immStore = new ImmediateStore();
		AggregateStatus.initConsoleOutput(new ConsoleOutputStub());
	}

	
	@Test
	public void inUseConstantly() {		
		for (int i=0; i<AggregateStatus.SMOOTHING_FACTOR; i++) {
			immNode = new ImmediateNode();
			immNode.frameNum = i;
			immNode.probTable = 1.0;
			List<Point> ballPoints = new ArrayList<Point>();
			ballPoints.add(new Point(i*640/AggregateStatus.SMOOTHING_FACTOR, 
					i*320/AggregateStatus.SMOOTHING_FACTOR));
			immNode.potentialBallPoints = ballPoints;
			immNode.potentialRacketPoints = new ArrayList<Point>();
			immStore.addResult((long)i, immNode);
		}
		statStores.immedStore = immStore;
		aggStat = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), statStores);
		aggStat.parseImmedStatus();
		
		assertEquals(GameStatus.InUse, statStores.aggStore.getGameStatus());
	}
	
	
	@Test
	public void noTableConstantly() {
		for (int i=0; i<AggregateStatus.SMOOTHING_FACTOR; i++) {
			immNode = new ImmediateNode();
			immNode.frameNum = i;
			immNode.probTable = 0.0;
			List<Point> ballPoints = new ArrayList<Point>();
			ballPoints.add(new Point(i*640/AggregateStatus.SMOOTHING_FACTOR, 
					i*320/AggregateStatus.SMOOTHING_FACTOR));
			immNode.potentialBallPoints = ballPoints;
			immNode.potentialRacketPoints = new ArrayList<Point>();
			immStore.addResult((long)i, immNode);
		}
		statStores.immedStore = immStore;
		aggStat = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), statStores);
		aggStat.parseImmedStatus();
		
		assertEquals(GameStatus.NoTable, statStores.aggStore.getGameStatus());
	}
	
	
	@Test
	public void racketsOnTableConstantly() {
		for (int i=0; i<AggregateStatus.SMOOTHING_FACTOR; i++) {
			immNode = new ImmediateNode();
			immNode.frameNum = i;
			immNode.probTable = 1.0;
			immNode.potentialBallPoints = new ArrayList<Point>();
			immNode.potentialRacketPoints = new ArrayList<Point>();
			immNode.potentialRacketPoints.add(new Point(10, 45));
			immStore.addResult((long)i, immNode);
		}
		statStores.immedStore = immStore;
		aggStat = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), statStores);
		aggStat.parseImmedStatus();
		
		assertEquals(GameStatus.Free, statStores.aggStore.getGameStatus());
	}
}
