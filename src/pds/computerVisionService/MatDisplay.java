package pds.computerVisionService;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 * Provides a means to easily display OpenCV Mats
 * @author Sam Nixon
 *
 */
public class MatDisplay {
	private JFrame frame;
	//Thanks to user 'mar' of StackOverflow for the bulk of this class
	//http://stackoverflow.com/questions/16494916/equivalent-method-for-imshow-in-opencv-java-build

	public static final int DEFAULT_WIDTH = 640;
	public static final int DEFAULT_HEIGHT = 360;
	
	/**
	 * Creates a JFrame to display any input image, at the desired x,y location.  
	 * Exit button will free up resources but will not shut down the entire program.
	 */
	public MatDisplay(String title, int x, int y) {
		try {
			frame = new JFrame(title);
			frame.getContentPane().add(new JLabel(new ImageIcon(new BufferedImage(5, 5, 5))), 0);
			frame.pack();
			frame.setVisible(true);
			frame.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
			frame.setResizable(true);
			frame.setLocation(x, y);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent evt) {
					frame.dispose();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Creates a JFrame to display any input image. Location defaults to (0,0).
	 * @param title Title of the window
	 */
	public MatDisplay(String title) {
		this(title, 0, 0);
	}

	
	/**
	 * Removes the previous Mat from the display and adds the input in its place
	 * @param img  Image to show
	 */
	public void updateResult(Mat img) {
		Mat scaled = new Mat();
		Imgproc.resize(img, scaled, new Size(frame.getSize().getWidth(), frame.getSize().getHeight()));
		MatOfByte matOfByte = new MatOfByte();

		Highgui.imencode(".jpg", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			if (frame.getContentPane().getComponentCount() > 0)
				frame.getContentPane().remove(0);
			frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)), 0);
			frame.pack();
		} catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
			// these errors are not problematic, and application will not rely on display in final version anyway
		} catch (Exception e) { 
			e.printStackTrace(); // will catch I/O exceptions
		}
	}


	/**
	 * Creates a matrix that represents the difference between the two input FrameNodes.
	 * This is down through a difference filter and subsequent edge detection. This method
	 * calls updateResult.
	 */
	public void displayDifference(CVFrame previousFrame, CVFrame currentFrame){
		Mat converted1 = previousFrame.getData();
		Mat converted2 = currentFrame.getData();
		Mat dif = new Mat();
		Mat edges = new Mat();
		Core.subtract(converted2, converted1, converted2);
		Core.multiply(converted2, new Scalar(3), converted2);
		Imgproc.threshold(converted2, dif, 100, 255, Imgproc.THRESH_TOZERO);
		Imgproc.Canny(dif, edges, 100, 255);
		updateResult(edges);
	}
	
	public void showRacketCandidates(CVFrame currentFrame){
		Mat image = currentFrame.getData();
		List<MatOfPoint> toDraw = ExtractObjectCandidates.getRacketCandidates(image);
		
		Mat result = Mat.zeros(image.size(), CvType.CV_8UC3);
		Imgproc.drawContours(result, toDraw, -1, new Scalar(0, 255, 0));
		for (MatOfPoint mp : toDraw){
			Rect r = Imgproc.boundingRect(mp);
			
			Core.rectangle(result, r.br(), r.tl(), new Scalar(255, 0 ,0 ));
		}
		updateResult(result);
	}




	public void showText(String[] text){
		Mat m = Mat.zeros(640, 360, CvType.CV_8UC3);
		int offset = 20;
		for (String s : text){
				Core.putText(m, s, new Point (20, offset += 20), 
			Core.FONT_HERSHEY_SIMPLEX, .5, new Scalar(0,0,255));
		}
		updateResult(m);
	}

}
