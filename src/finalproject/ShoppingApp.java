/**
 * 
 */
package finalproject;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Main function in running the application
 * @author 
 * 
 */
public class ShoppingApp {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		final Shop s = new Shop();
		s.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		s.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (!s.hasLoggedOut()) {
					if (JOptionPane.showConfirmDialog(s,
							"You may lose unsaved work! Are you sure?",
							"Really Closing?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				} else
					System.exit(0);
			}
		});
	}

}
