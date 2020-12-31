package VisualPuzzleSolver;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import Tools.Tools;

public class VisualPuzzleSolver {

	private static final Rect inputRect = new Rect(1082, 400, 539, 726);
	private static final Rect outputRect = new Rect(396, 436, 504, 461);
	private static final double _sensitivity = 0.92;
	private static final double _scale = 1;
//	private static final Size nullSize = new org.opencv.core.Size();

	String[] input_paths = new String[] { "images/input_1c.png", "images/input_55.png", "images/input_e9.png",
			"images/input_bd.png" };

	String[] output_paths = new String[] { "images/output_1c.png", "images/output_55.png", "images/output_e9.png",
			"images/output_bd.png" };

	private Mat img_input;
	private Mat[] output_images, input_images;

	private Mat screenshotImage;

	public VisualPuzzleSolver() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		img_input = new Mat(1, 1, CvType.CV_8UC3);

		initSearchImages();

//		var target = Imgcodecs.imread("images/cb1.png");

		Thread searchThread = new Thread(new Runnable() {

			private List<CodeSymbol> output_codes;
			private List<CodeSymbol> input_codes, lastInputCodes; // Assume new puzzles have new inputs
			private CodeSymbol codeMatrixs[][];
			private LinkedList<LinkedList<CodeSymbol>> sequences;
			private LinkedList<CodeSymbol> buffer;
			private LinkedList<LinkedList<CodeSymbol>> allPossibilities;
			private LinkedList<LinkedList<CodeSymbol>> validSolutions;

			@Override
			public void run() {
				lastInputCodes = new LinkedList<VisualPuzzleSolver.CodeSymbol>();
				
				while (true) {

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					try {
						screenshotImage = Tools.bufferedImageToMat(Tools.GetFullscreenScreenshot());
						Imgproc.resize(screenshotImage, screenshotImage, new Size(2560, 1440), 0, 0);
//						Tools.showImage(screenshotImage);
						Imgproc.cvtColor(screenshotImage, img_input, Imgproc.COLOR_BGR2GRAY);

						input_codes = FindTemplates(img_input, inputRect, input_images);

						if (input_codes.size() == 0) {
							Tools.hideFullscreenImage();
							lastInputCodes.clear();
							continue;
						}

						boolean newInputs = false;
						if (lastInputCodes.size() != input_codes.size()) {
							newInputs = true;
						} else {
							for (int i = 0; i < input_codes.size(); i++) {
								if (!input_codes.get(i).equals(lastInputCodes.get(i))) {
									newInputs = true;
									break;
								}
							}
						}

						if (!newInputs) {
							continue;
						} else {
							lastInputCodes.clear();
							lastInputCodes.addAll(input_codes);
						}

						output_codes = FindTemplates(img_input, outputRect, output_images);

						int xx = 0;
						for (CodeSymbol o : output_codes) {
							System.out.print(o.ID + " " + o.x + " " + o.y + " | ");
							if (++xx % 5 == 0)
								System.out.println();
						}

						if (output_codes.size() != 25) {
							Tools.hideFullscreenImage();
							continue;
						}

						sequences = new LinkedList<LinkedList<CodeSymbol>>();
						buffer = new LinkedList<CodeSymbol>();

						for (int i = 0; i < input_codes.size(); i++) {
							CodeSymbol cur = input_codes.get(i);
							if (buffer.size() > 0) {
								if (Math.abs(buffer.getLast().y - cur.y) > 20) {
									sequences.add(buffer);
									buffer = new LinkedList<CodeSymbol>();
								}
							}

							buffer.addLast(cur);
						}
						sequences.add(buffer);

						codeMatrixs = new CodeSymbol[5][5];
						for (int y = 0; y < 5; y++) {
							for (int x = 0; x < 5; x++) {
								codeMatrixs[x][y] = output_codes.get(y * 5 + x);
							}
						}

						allPossibilities = new LinkedList<LinkedList<CodeSymbol>>();
						findAllCodes(codeMatrixs, allPossibilities, 0, 0, new LinkedList<CodeSymbol>(), false, true);

//					var solution = allPossibilities.stream()
//						.filter(e -> Collections.indexOfSubList(e, sequences.getLast()) >= 0)
//						.findFirst();

						// TODO Find Easiest solution e.g. closest symbols

						validSolutions = new LinkedList<LinkedList<CodeSymbol>>();

						for (int i = sequences.size() - 1; i >= 0; i--) {
							LinkedList<CodeSymbol> solution = sequences.get(i);

							for (LinkedList<CodeSymbol> p : allPossibilities) {

								for (int j = 0, s = 0; j < p.size(); j++) {

									if (p.size() - j < solution.size() - s) {
										break;
									}

									if (p.get(j).ID == solution.get(s).ID) {
										s++;
										if (s == solution.size()) {
											validSolutions.add(p);
											break;
										}
									} else {
										s = 0;
									}
								}

							}

							// We found a solution, keep it
							if (validSolutions.size() > 0) {
								allPossibilities.clear();
								allPossibilities.addAll(validSolutions);
								validSolutions.clear();
							}

						}

//					var result = new LinkedList<CodeSymbol>();

						LinkedList<CodeSymbol> result = allPossibilities.get(0);

						BufferedImage bi = new BufferedImage((int) (screenshotImage.width() * _scale),
								(int) (screenshotImage.height() * _scale), BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = (Graphics2D) bi.getGraphics();
						g.setStroke(new BasicStroke(2));
						g.setFont(new Font("Calibri", 1, 30));
						g.setColor(Color.red);

						for (int i = 0; i < result.size(); i++) {
							if (i == 0)
								g.setColor(Color.green);
							else
								g.setColor(Color.red);

							CodeSymbol e = result.get(i);
							g.drawRect(e.x - 5, e.y - 5, 45, 40);
							g.drawString("" + (i + 1), e.x - 20, e.y - 13);
						}

						Tools.showFullscreenImageOnTop(bi);
						System.out.println(result);

					} catch (AWTException e) {
						e.printStackTrace();
					}
				}
			}
		});

		searchThread.start();

		if (true)
			return;

//		var<Thread> threads = new LinkedList<Thread>();
//
//		for (var s : input_paths) {
//			var t = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					Mat result = new Mat(1, 1, CvType.CV_8UC3);
//					var identifier = Imgcodecs.imread(s);
//					Imgproc.cvtColor(identifier, identifier, Imgproc.COLOR_BGR2GRAY);
//					Imgproc.matchTemplate(img_input, identifier, result, Imgproc.TM_CCOEFF_NORMED);
//
//					markOcurrences(result, target, identifier);
//
//				}
//			});
//			threads.add(t);
//			t.start();
//		}
//
//		for (var s : output_paths) {
//			var t = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					Mat result = new Mat(1, 1, CvType.CV_8UC3);
//					var identifier = Imgcodecs.imread(s);
//					Imgproc.cvtColor(identifier, identifier, Imgproc.COLOR_BGR2GRAY);
//					Imgproc.matchTemplate(img_input, identifier, result, Imgproc.TM_CCOEFF_NORMED);
//					markOcurrences(result, target, identifier);
//
//				}
//			});
//			threads.add(t);
//			t.start();
//		}
//
//		while (threads.stream().anyMatch(t -> t.isAlive())) {
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
//		Tools.showImage(target);
//
//		Tools.showFullscreenImageOnTop(Tools.matToImg(target));

//		var minmax = Core.minMaxLoc(result);
//
//		var matchLoc = minmax.maxLoc;
//		System.out.println(matchLoc);
//		Imgproc.rectangle(target, matchLoc, new Point(matchLoc.x + identifier.width(), matchLoc.y + identifier.height()), 
//				new Scalar(0, 0, 255), 5);

	}

	private List<CodeSymbol> FindTemplates(Mat source, Rect rect, Mat[] templates) {
		Mat roi = source.submat(rect);
		List<CodeSymbol> codes = getPositions(roi, templates);

		Set<CodeSymbol> set = new HashSet<CodeSymbol>();
		codes.forEach(c -> {
			c.x += rect.x;
			c.y += rect.y;
		});
		set.addAll(codes);
		codes.clear();
		codes.addAll(set);

		// Sort the matrix and required sequences by x y
		codes.sort((a, b) -> {
			return a.x - b.x;
		});
		codes.sort((a, b) -> {
			return Math.abs(a.y - b.y) > 20 ? a.y - b.y : 0;
		});

		return codes;
	}

	private void initSearchImages() {
//		Imgproc.resize(img_input, img_input, nullSize, scale, scale);

//		Tools.showImage(img_gray);

		output_images = new Mat[output_paths.length];
		for (int i = 0; i < output_paths.length; i++) {
			output_images[i] = Imgcodecs.imread(output_paths[i]);
//			Imgproc.resize(output_images[i], output_images[i], nullSize, scale, scale);
			Imgproc.cvtColor(output_images[i], output_images[i], Imgproc.COLOR_BGR2GRAY);
		}

		input_images = new Mat[output_paths.length];
		for (int i = 0; i < output_paths.length; i++) {
			input_images[i] = Imgcodecs.imread(input_paths[i]);
//			Imgproc.resize(input_images[i], input_images[i], nullSize, scale, scale);
			Imgproc.cvtColor(input_images[i], input_images[i], Imgproc.COLOR_BGR2GRAY);
		}
	}

	private void findAllCodes(CodeSymbol[][] codeMatrix, LinkedList<LinkedList<CodeSymbol>> allPossibilities, int X,
			int Y, LinkedList<CodeSymbol> linkedList, boolean searchY, boolean first) {

		if (linkedList.size() >= 4) {
			allPossibilities.add(linkedList);
			return;
		} else {

			int x = searchY ? X : 0;
			int y = searchY ? 0 : Y;

			for (int i = 0; i < 5; i++, x = searchY ? x : x + 1, y = searchY ? y + 1 : y) {
				if ((searchY && Y == i && !first) || (!searchY && X == i && !first)) {
					continue;
				}

				LinkedList<CodeSymbol> list = new LinkedList<CodeSymbol>(linkedList);
				list.add(codeMatrix[x][y]);
				findAllCodes(codeMatrix, allPossibilities, x, y, list, !searchY, false);

			}
		}
	}

	private List<CodeSymbol> getPositions(Mat imageRoi, Mat[] output_images) {
		List<CodeSymbol> symbols = new LinkedList<CodeSymbol>();
		Mat result = new Mat(1, 1, CvType.CV_8UC3);
		for (int i = 0; i < output_images.length; i++) {
			Mat mat = output_images[i];
			Imgproc.matchTemplate(imageRoi, mat, result, Imgproc.TM_CCOEFF_NORMED);
			symbols.addAll(findAllPointsInResult(result, i));
		}
		return symbols;
	}

	private List<CodeSymbol> findAllPointsInResult(Mat result, int id) {
		List<CodeSymbol> symbols = new LinkedList<CodeSymbol>();
		for (int y = 0; y < result.rows(); y++) {
			for (int x = 0; x < result.cols(); x++) {
				double d = result.get(y, x)[0];
				if (d > _sensitivity) {
					symbols.add(new CodeSymbol(id, x, y));
				}
			}
		}
		return symbols;
	}

	@SuppressWarnings("unused")
	private void markOcurrences(Mat result, Mat target, Mat identifier) {
		for (int y = 0; y < result.rows(); y++) {
			for (int x = 0; x < result.cols(); x++) {
				double d = result.get(y, x)[0];
				if (d > 0.9) {
					Imgproc.rectangle(target, new Point(x, y),
							new Point(x + identifier.width(), y + identifier.height()), new Scalar(0, 0, 255), 5);
					System.out.println(x);
				}
			}
		}
	}

	class SolutionSymbol {

	}

	class CodeSymbol {
		private static final int distance_for_equals = 20;

		public int ID;
		public int x;
		public int y;

		public CodeSymbol(int iD, int x, int y) {
			super();
			ID = iD;
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "CodeSymbol [ID=" + ID + "]";
		}

		@Override
		public int hashCode() {
			return ID;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CodeSymbol) {
				CodeSymbol other = (CodeSymbol) obj;
				double dist = Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
				return (dist < distance_for_equals) && (ID == other.ID);
			}
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CodeSymbol other = (CodeSymbol) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (ID != other.ID)
				return false;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			return true;
		}

		private VisualPuzzleSolver getEnclosingInstance() {
			return VisualPuzzleSolver.this;
		}

	}

}
