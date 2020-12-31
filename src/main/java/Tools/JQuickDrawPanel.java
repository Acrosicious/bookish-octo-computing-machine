package Tools;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class JQuickDrawPanel extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		BufferedImage _img;
		Dimension preferredSize;

		public JQuickDrawPanel(BufferedImage img)
		{
			_img = img;
			setPreferredSize();
		}

		public Dimension getPreferredSize()
		{
			return preferredSize;
		}

		private void setPreferredSize()
		{
			preferredSize = new Dimension(_img.getWidth()/2, _img.getHeight()/2);
//			preferredSize = new Dimension(1000, 1000);
		}

		protected void paintComponent(Graphics g) 
		{
			super.paintComponent(g);
			g.drawImage(_img, 0, 0, getWidth(), getHeight(), null);
		}



	}