package Tools;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JPanel;


public class MainFrameSpielerei extends JPanel implements KeyListener, MouseListener, Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int R = 0, G = 0, B = 0;
	int r = 0, g = 0, b = 0;
	
	Rectangle R1 = new Rectangle(0, 0, 0, 0);

	private BufferedImage img;
	
	public MainFrameSpielerei(BufferedImage img)
	{
		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
		
		this.img = img;
		
		Thread Th = new Thread(this);
		Th.start();
	}
	
	public void setImage(BufferedImage img) {
		this.img = img;
		repaint();
	}

	protected void repainter() {
		repaint();
	}

	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g.create();
//		Composite c = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		
		g.clearRect(0,0,getWidth(), getHeight());
		g2.setColor(new Color(R, G, B, 125));

		if(img != null)
			g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
		
//		g2.setComposite(c);
	}

//	public static JFrame F;
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		JFrame f = new JFrame();
//		F = f;
//		f.add(new MainFrameSpielerei(null));
//		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		f.setUndecorated(true);
//		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
//		f.setResizable(false);
////		f.setOpacity((float) 0.5);
//		f.setBackground(new Color(0, 0, 0, 0));
//		f.setAlwaysOnTop(true);
//		f.setVisible(true);
//		
//	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			System.exit(1);
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {

		while(true)
		{
			Random gen = new Random();
			if(R == r && G == g && B == b)
			{
				r = gen.nextInt(255);
				g = gen.nextInt(255);
				b = gen.nextInt(255);
			}
			
			if(R < r)
				R++;
			else if (R > r)
				R--;
			
			if(G < g)
				G++;
			else if (G > g)
				G--;
			
			if(B < b)
				B++;
			else if (B > b)
				B--;
//			
//			if(R1.x < R2.x)
//				R1.x++;
//			else if (R1.x > R2.x)
//				R1.x--;
//			
//			if(R1.y < R2.y)
//				R1.y++;
//			else if (R1.y > R2.y)
//				R1.y--;
//			
//			if(R1.width< R2.width)
//				R1.width++;
//			else if (R1.width > R2.width)
//				R1.width--;
//			
//			if(R1.height < R2.height)
//				R1.height++;
//			else if (R1.height > R2.height)
//				R1.height--;
//			
			repainter();
			
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}
