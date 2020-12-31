package Tools;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.UnknownFormatConversionException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class Tools {

	public static Robot robot;

	static {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public static BufferedImage screenshotRegion(Rectangle rect) {
		return robot.createScreenCapture(rect);
	}

	public static BufferedImage GetFullscreenScreenshot() throws AWTException {
//		var dimensions = getMonitorSizes();
//		var s = dimensions.getFirst();		

		Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		BufferedImage capture = new Robot().createScreenCapture(screenRect);

		return capture;
	}

	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		DataBuffer buffer = bi.getRaster().getDataBuffer();
		if (buffer instanceof DataBufferByte) {
			byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
			mat.put(0, 0, data);
			return mat;
		} else if (buffer instanceof DataBufferInt) {
//			int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ByteBuffer bBuffer = java.nio.ByteBuffer.allocate(data.length * 4);
//			IntBuffer iBuffer = bBuffer.asIntBuffer();
//			iBuffer.put(data);
//			mat.put(0, 0, bBuffer.array());

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			try {
				ImageIO.write(bi, "jpg", byteArrayOutputStream);
				byteArrayOutputStream.flush();
				return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()),
						Imgcodecs.IMREAD_UNCHANGED);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return mat;
		} else {
			throw new UnknownFormatConversionException("Screenshot format unimplemented: " + buffer.getDataType());
		}
	}

	public static BufferedImage matToImg(Mat img) {
//		Imgproc.resize(img, img, new Size(1000, 1000));
		MatOfByte matOfByte = new MatOfByte();
		Imgcodecs.imencode(".jpg", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
			return bufImage;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static JFrame jf = null;
	public static JQuickDrawPanel qdp;

	public static void showImage(BufferedImage bi) {
		if (jf == null) {
			jf = new JFrame("Image");
			jf.setResizable(false);
			qdp = new JQuickDrawPanel(bi);
			qdp.setFocusable(true);
			jf.add(qdp);
			jf.pack();
			jf.setVisible(true);
			jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		} else {
			qdp._img = bi;
			jf.repaint();
		}
	}

	public static void showImage(Mat mat) {
		showImage(matToImg(mat));
	}

	public static double colorDistance(Color c1, Color c2) {
		double sum = 0.0;
		sum += Math.pow(c1.getBlue() - c2.getBlue(), 2);
		sum += Math.pow(c1.getGreen() - c2.getGreen(), 2);
		sum += Math.pow(c1.getRed() - c2.getRed(), 2);
		return Math.sqrt(sum);
	}

	public static JFrame jf_full = null;
	private static MainFrameSpielerei imageDisplay;

	public static void hideFullscreenImage() {
		if (jf_full != null)
			jf_full.setVisible(false);
	}

	public static void showFullscreenImageOnTop(BufferedImage img) {
//		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		if (jf_full == null) {
//			LinkedList<Dimension> sizes = getMonitorSizes();

			jf_full = new JFrame();
			imageDisplay = new MainFrameSpielerei(img);
			jf_full.add(imageDisplay);
			jf_full.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			jf_full.setUndecorated(true);
			jf_full.setExtendedState(JFrame.MAXIMIZED_BOTH);
//			jf_full.setSize(sizes.get(0));
			jf_full.setResizable(false);
//			f.setOpacity((float) 0.5);
			jf_full.setBackground(new Color(0, 0, 0, 0));
			jf_full.setAlwaysOnTop(true);
		} 

		imageDisplay.setImage(img);
		jf_full.setVisible(true);
	}

	@SuppressWarnings("unused")
	private static LinkedList<Dimension> getMonitorSizes() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		LinkedList<Dimension> dimensions = new LinkedList<Dimension>();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < gs.length; i++) {
			DisplayMode dm = gs[i].getDisplayMode();
			sb.append(i + ", width: " + dm.getWidth() + ", height: " + dm.getHeight() + "\n");
			dimensions.add(new Dimension(dm.getWidth(), dm.getHeight()));
		}

		dimensions.sort((a, b) -> b.width - a.width);
		return dimensions;
	}

	public static BufferedImage TransformColorToTransparency(BufferedImage image, Color c1, Color c2) {
		// Primitive test, just an example
		final int r1 = c1.getRed();
		final int g1 = c1.getGreen();
		final int b1 = c1.getBlue();
		final int r2 = c2.getRed();
		final int g2 = c2.getGreen();
		final int b2 = c2.getBlue();
		ImageFilter filter = new RGBImageFilter() {
			public final int filterRGB(int x, int y, int rgb) {
				int r = (rgb & 0xFF0000) >> 16;
				int g = (rgb & 0xFF00) >> 8;
				int b = rgb & 0xFF;
				if (r >= r1 && r <= r2 && g >= g1 && g <= g2 && b >= b1 && b <= b2) {
					// Set fully transparent but keep color
					return rgb & 0xFFFFFF;
				}
				return rgb;
			}
		};

		ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
		return ImageToBufferedImage(Toolkit.getDefaultToolkit().createImage(ip), image.getWidth(), image.getHeight());
	}

	private static BufferedImage ImageToBufferedImage(Image image, int width, int height) {
		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
		return dest;
	}

}
