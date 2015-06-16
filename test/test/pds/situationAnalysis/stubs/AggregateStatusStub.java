package test.pds.situationAnalysis.stubs;

import pds.computerVisionService.CVStores;
import pds.situationAnalysis.AggregateStatus;
import pds.situationAnalysis.StatusStores;

public class AggregateStatusStub extends AggregateStatus {

	public AggregateStatusStub(long frameNum, CVStores cvStores,
			StatusStores statusStores) {
		super(frameNum, cvStores, statusStores);
	}
}
