package pds.computerVisionService;

import static org.junit.Assert.*;

import org.junit.Test;
import pds.computerVisionService.CVStores;

public class CVStoresTest {

	@Test
	public void testGetNumStores() {
		CVStores s = new CVStores();
		assertTrue(s.getNumStores() < 6 
				&& s.getNumStores() > 0);
	}

}
