import javax.swing.JFrame;

public class Module3 {
	public static void main(String[] args) {
		JFrame frame = new JFrame("TRAX GAME.v10");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		PrimaryPanel primary = new PrimaryPanel();
		frame.getContentPane().add(primary);
		
		frame.pack();
		frame.setVisible(true);
	}
}
