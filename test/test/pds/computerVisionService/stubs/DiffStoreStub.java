package test.pds.computerVisionService.stubs;

import pds.computerVisionService.DifferenceStore;

public class DiffStoreStub extends DifferenceStore {

	public DiffStoreStub() {
	}
	
	@Override
	public boolean wasSkipped(Long frameNumber) {
		return true;
	}
}
