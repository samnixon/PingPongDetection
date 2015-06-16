package pds.situationAnalysis;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Point;

import pds.situationAnalysis.AggregateStatus;
import pds.situationAnalysis.ImmediateNode;
import pds.videoService.FrameNode;
import test.pds.computerVisionService.stubs.CVStoresStub;
import test.pds.situationAnalysis.stubs.StatusStoresStub;

/**
 * 
 * @author Christopher Guinnup
 *
 */
public class MovingBallDetectedTest {
	Random random = new Random(127);
	ImmediateNode[] statuses;
	
	@Before
	public void before() {
		FrameNode.initDimensions(640, 480);
		statuses = new ImmediateNode[AggregateStatus.SMOOTHING_FACTOR];
	}

	
	@Test
	public void movingBallDetected_StraightLineTest() {
		for (int i=0; i < AggregateStatus.SMOOTHING_FACTOR; i++) {
			ImmediateNode status = new ImmediateNode();
			status.frameNum = i;
			status.potentialBallPoints = new ArrayList<Point>();
			status.potentialBallPoints.add(new Point(i*320/AggregateStatus.SMOOTHING_FACTOR, 
					i*240/AggregateStatus.SMOOTHING_FACTOR));
			statuses[i] = status;
		}
		
		AggregateStatus toTest = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), new StatusStoresStub());
		double result = toTest.movingBallDetected(statuses);
		assertTrue( result >= 0.5 );
	}
	
	
	@Test
	public void movingBallDetected_SkipFramesTest() {
		for (int i=0; i < AggregateStatus.SMOOTHING_FACTOR; i++) {
			ImmediateNode status = new ImmediateNode();
			status.frameNum = i;
			status.potentialBallPoints = new ArrayList<Point>();
			if (i%2 == 0) {
				status.potentialBallPoints.add(new Point(i*320/AggregateStatus.SMOOTHING_FACTOR, 
						i*240/AggregateStatus.SMOOTHING_FACTOR));
			}
			statuses[i] = status;
		}

		AggregateStatus toTest = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), new StatusStoresStub());
		assertTrue( toTest.movingBallDetected(statuses) >= 0.5 );
	}
	
	
	@Test
	public void movingBallDetected_MultiplePerFrameTest() {
		for (int i=0; i < AggregateStatus.SMOOTHING_FACTOR; i++) {
			ImmediateNode status = new ImmediateNode();
			status.frameNum = i;
			status.potentialBallPoints = new ArrayList<Point>();
			for (int j=random.nextInt(3); j>=0; j--) {
				status.potentialBallPoints.add(new Point(random.nextInt(320), random.nextInt(240)));
			}
			status.potentialBallPoints.add(new Point(i*320/AggregateStatus.SMOOTHING_FACTOR, 
					i*240/AggregateStatus.SMOOTHING_FACTOR));
			for (int j=random.nextInt(3); j>=0; j--) {
				status.potentialBallPoints.add(new Point(random.nextInt(320), random.nextInt(240)));
			}
			statuses[i] = status;
		}

		AggregateStatus toTest = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), new StatusStoresStub());
		assertTrue( toTest.movingBallDetected(statuses) >= 0.5 );
	}
	
	
	@Test
	public void movingBallDetected_SkippedAndMultipleTest() {
		for (int i=0; i < AggregateStatus.SMOOTHING_FACTOR; i++) {
			ImmediateNode status = new ImmediateNode();
			status.frameNum = i;
			status.potentialBallPoints = new ArrayList<Point>();
			for (int j=random.nextInt(3); j>=0; j--) {
				status.potentialBallPoints.add(new Point(random.nextInt(320), random.nextInt(240)));
			}
			if ((i-1)%3 <= 0) {
			status.potentialBallPoints.add(new Point(i*320/AggregateStatus.SMOOTHING_FACTOR, 
					i*240/AggregateStatus.SMOOTHING_FACTOR));
			}
			for (int j=random.nextInt(3); j>=0; j--) {
				status.potentialBallPoints.add(new Point(random.nextInt(320), random.nextInt(240)));
			}
			statuses[i] = status;
		}

		AggregateStatus toTest = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), new StatusStoresStub());
		assertTrue( toTest.movingBallDetected(statuses) >= 0.5 );
	}

	
	@Test
	public void movingBallDetected_JustNoiseTest() {
		for (int i=0; i < AggregateStatus.SMOOTHING_FACTOR; i++) {
			ImmediateNode status = new ImmediateNode();
			status.frameNum = i;
			status.potentialBallPoints = new ArrayList<Point>();
			if (i%2 == 0) {
				for (int j=random.nextInt(5); j>=0; j--) {
					status.potentialBallPoints.add(new Point(random.nextInt(320), random.nextInt(240)));
				}
			}
			statuses[i] = status;
		}

		AggregateStatus toTest = new AggregateStatus(AggregateStatus.SMOOTHING_FACTOR-1, new CVStoresStub(), new StatusStoresStub());
		assertTrue( toTest.movingBallDetected(statuses) < 0.5 );
	}
}