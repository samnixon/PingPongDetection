package test.pds.computerVisionService.stubs;

import java.util.concurrent.CountDownLatch;

import pds.computerVisionService.CVFrame;
import pds.computerVisionService.DifferenceMotion;
import pds.computerVisionService.DifferenceStore;

public class DiffMotionStub extends DifferenceMotion {

	public DiffMotionStub(CVFrame frame1, CVFrame frame2,
			DifferenceStore diffStore, CountDownLatch cdLatch) {
		super(frame1, frame2, diffStore, cdLatch);
	}
}
