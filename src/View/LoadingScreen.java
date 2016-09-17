package View;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * A temporary JFrame, which serves as a loading screen, when loading or saving when running the program.
 * In case of loading an OSM-file, it will notify the user of its parsing process.
 */
public class LoadingScreen extends JFrame {
	private JPanel contentPane;
	private JLabel loadingLbl;

	/**
	 * Initializes a loading screen with the specified loadingText.
	 * @param loadingText Notifying the user of the process of the loading.
     */
	public LoadingScreen(String loadingText) {
		setLocationRelativeTo(null);
		setPreferredSize(new Dimension(640,480));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ImagePanel img = new ImagePanel("Kronhjort.png");
		loadingLbl = new JLabel(loadingText);
		loadingLbl.setFont(new Font("Segoe UI Semilight", Font.PLAIN, 18));
		loadingLbl.setForeground(new Color(50,50,50));
		
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(10,10));
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5,5,10,5));
		contentPane.add(img, BorderLayout.CENTER);
		contentPane.add(loadingLbl, BorderLayout.SOUTH);
		setContentPane(contentPane);
		
		setUndecorated(true);
		pack();
		setLocation(getX() - getWidth()/2, getY() - getHeight()/2);
		setVisible(true);
		contentPane.paintImmediately(0, 0, (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
	}

	/**
	 * Used to update the loadingText of the loading screen.
	 * @param s the actual loading text.
     */
	public void loadingText(String s) {
		loadingLbl.setText(s);
		loadingLbl.repaint();
		contentPane.paintImmediately(0, 0, (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight());
	}

	/**
	 * This ImagePanel is an inner class of the LoadingScreen, which is used
	 * to fill an entire JPanel.
	 * This class is loosely based on the following StackOverflow guide:
	 * http://stackoverflow.com/questions/299495/how-to-add-an-image-to-a-jpanel
	 */
	class ImagePanel extends JPanel {
		private Image img;

		/**
		 * Takes a String, which defines the filepath of the image.
		 * @param fp the filepath of the image in our resources folder.
         */
		public ImagePanel(String fp) {
			try {
				InputStream in = getClass().getClassLoader().getResourceAsStream(fp);
				if(in == null) { throw new IOException(); }
				img = ImageIO.read(in);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Encountered a problem while loading the image for the loading screen.",
						"Load failed", JOptionPane.ERROR_MESSAGE);
			}
		}

		/**
		 * Paints the image on the JPanel.
		 * @param g the needed Graphics component, which is used to paint.
         */
		public void paintComponent(Graphics g) {
			g.drawImage(img, 00, 00, null);
		}
	}
}