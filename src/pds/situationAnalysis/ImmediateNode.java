package pds.situationAnalysis;

import java.util.List;

import org.opencv.core.Point;


/**
 * A struct-like container for immediate probabilities
 * @author Chris Guinnup
 *
 */
public class ImmediateNode {
	public long frameNum;
	public double probTable;
	public double probDiffMotion;
	public List<Point> potentialBallPoints;
	public List<Point> potentialRacketPoints;
	public double probAvailable;
	public double probInUse;
	public double probError;
}