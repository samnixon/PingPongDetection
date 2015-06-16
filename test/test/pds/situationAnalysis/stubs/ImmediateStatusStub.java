package test.pds.situationAnalysis.stubs;

import pds.computerVisionService.CVStores;
import pds.situationAnalysis.ImmediateStatus;
import pds.situationAnalysis.StatusStores;

public class ImmediateStatusStub extends ImmediateStatus {

	public ImmediateStatusStub(long frameNum, CVStores cvStores,
			StatusStores statusStores) {
		super(frameNum, cvStores, statusStores);
	}
	
	@Override
	public void run() {
	}
}
