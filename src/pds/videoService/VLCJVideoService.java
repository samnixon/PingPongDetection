package pds.videoService;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

/**
 *Uses VLCHooks to open a media file and store frame data in a FrameStore
 * @author Sam Nixon
 *
 */
public class VLCJVideoService implements VideoServiceIF {
	private VLCHooks vlcj;
	private FrameStore storage;

	public static VideoServiceIF createVideoSvc(String[] args) {
		if (args.length >= 1){
			VideoServiceIF result = new VLCJVideoService(args);
			return result;
		}
		else{
			return null;
		}
	}

	private VLCJVideoService(String[] args){
		try {
			this.vlcj = new VLCHooks(args[0],args);
			this.storage = this.vlcj.getStorage();
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void startVideoSvc() throws FileNotFoundException {
		vlcj.start();
	}
	/**
	 * Returns the FrameStore with the video's individual frames
	 */
	@Override
	public FrameStore getStorage() {
		return storage;
	}

}
