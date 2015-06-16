package pds.videoService;

import java.io.FileNotFoundException;
/**
 * 
 * @author Sam Nixon
 *
 */
public interface VideoServiceIF {

	public FrameStore getStorage();
	
	public void startVideoSvc() throws FileNotFoundException;
}
