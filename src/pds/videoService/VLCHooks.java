package pds.videoService;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

/**
 * Starts a video player that stores every individual frame in its FrameStore
 * Created based heavily on TestDirectPlayer, an example of how to properly implement the
 * uk.co.caprica.vlcj.player.Direct.DirectPlayer
 * @author Sam Nixon
 *
 */
public class VLCHooks {
	private final BufferedImage image;
	
	private ImagePane imagePane;

	private final MediaPlayerFactory factory;
	private final DirectMediaPlayer mediaPlayer;
	int width = 640;
	int height = 360;
	private String media;
	private FrameStore storage = new FrameStore(height, width);
	
	public VLCHooks(String media, String[] args) throws InvocationTargetException, InterruptedException{
		
		this.media = media;
		loadVLClibrary(args);
		
		if (System.getProperty("os.name").contains("Linux") == false) {
			image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().
				  getDefaultConfiguration().createCompatibleImage(width, height);
		}
		else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
	
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("VLCJ");

				imagePane = new ImagePane(image);
				imagePane.setSize(width, height);
				imagePane.setMinimumSize(new Dimension(width, height));
				imagePane.setPreferredSize(new Dimension(width, height));
				frame.getContentPane().setLayout(new BorderLayout());
				frame.getContentPane().add(imagePane, BorderLayout.CENTER);
				frame.pack();
				frame.setResizable(false);
				frame.setVisible(true);
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent evt) {
						mediaPlayer.release();
						factory.release();
						System.exit(0);
					}
				});
			}
		});
		
		factory = new MediaPlayerFactory(args);
		mediaPlayer = factory.newDirectMediaPlayer(new TestBufferFormatCallback(), new InterceptRender());
	}
	
	public void start() throws FileNotFoundException {
		boolean success = mediaPlayer.startMedia(media);
		if (!success) {
			throw new FileNotFoundException("VLC will not play!");
		}
	}
	
	/**
	 * Attempt to obtain VLC library location from native OS info
	 */
	/*
	private void loadVLClibrary() throws IOException {
		String os = System.getProperty("os.name");
		if (os.contains("Windows")) {
			final String vlcRegistryLocation = "HKLM\\Software\\VideoLAN\\VLC\\";
			final String vlcRegistryKey = "InstallDir";
			final String tabSeparator = "\t";
			final String spacesSeparator = "    ";
			String output;
			
			Process registryProcess = Runtime.getRuntime().exec("reg query \""  
					+ vlcRegistryLocation + "\" /v " + vlcRegistryKey);
			output = readRegistry(registryProcess.getInputStream());
			
			
			if (output.contains(tabSeparator)) {
				//TODO
			}
			else if (output.contains(spacesSeparator)) {
				//TODO
			}
			else {
				System.err.println("Registry reading: output is not as expected!");
			}
		}
		else if (os.contains("Linux")) {
			
		}
	}
	
	private String readRegistry(InputStream in) {
		StringWriter stringWriter = new StringWriter();
		try {
			int charAsInt;
			while ((charAsInt = in.read()) != -1) {
				stringWriter.write( charAsInt );
			}
		}
		catch (IOException e) {
			System.err.println("Failure reading registry:");
			e.printStackTrace();
		}
		return stringWriter.toString();
	}*/
	
	
	/***
	 * Loads the VLC library files based on passed
	 * @param args Arguments sent with the program call
	 */
	private void loadVLClibrary(String args[]){
		if (args.length == 2) {
			NativeLibrary.addSearchPath(
	                RuntimeUtil.getLibVlcLibraryName(), args[1]
	            );
	        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		}
		else {
			System.err.println("Warning: Please specify the path to vlclib.dll\n");
		}
	}
	
	public FrameStore getStorage(){
		return storage;
	}

	/*
	 * Standard JPanel setup
	 */
	@SuppressWarnings("serial")
	private final class ImagePane extends JPanel {
		private final BufferedImage image;
		public ImagePane(BufferedImage image) {
			this.image = image;
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(image, null, 0, 0);
			// You could draw on top of the image here...
			
		}
	}

	/**
	 * On each frame render, the frame is added to storage
	 */
	private final class InterceptRender extends RenderCallbackAdapter {
		public InterceptRender() {
			super( ( (DataBufferInt) (image.getRaster().getDataBuffer()) ).getData() );
		}
		
		@Override
		public void onDisplay(DirectMediaPlayer mediaPlayer, int[] data) {
			
			int[] tempData = new int[data.length];
			for (int i = 0; i < data.length; i++){
				tempData[i] = data[i];
			}
			try {
				storage.putFrame(tempData);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			imagePane.repaint();
		}
	}

	private final class TestBufferFormatCallback implements BufferFormatCallback {
		@Override
		public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
			return new RV32BufferFormat(width, height);
		}
	}


}
