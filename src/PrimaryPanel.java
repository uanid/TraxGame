
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class PrimaryPanel extends JPanel implements ActionListener {

	private PrimaryPanel obj;
	private GameData data;
	private GameManagerV1 gameManagerV1;
	private GameManagerV2 gameManagerV2;
	private GameManagerV3 gameManagerV3;

	public JPanel menu;
	public JButton reset;
	public JButton undo;
	public JButton doAI;
	public JLabel aiStatus;
	public JLabel turnStatus;
	public JLabel focus;
	public JLabel tileSTatus;
	public JScrollPane logsScroll;
	public JTextArea logs;
	public String logText = "";
	
	public Font d2Coding;

	public JPanel game;
	public JScrollPane scroll;

	// private boolean gameover = false;
	public PrimaryPanel() {
		obj = this;

		this.setPreferredSize(new Dimension(960, 660));
		this.setBackground(Color.white);
		this.setLayout(null);
		
		try {
			d2Coding = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream("png/D2Coding.ttf"));
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		d2Coding = d2Coding.deriveFont(15F);
		
		
		
		// 메뉴패널 선언
		{
			menu = new JPanel();
			menu.setBounds(10, 10, 280, 640);
			menu.setBackground(Color.white);
			menu.setLayout(null);
			menu.setBorder(BorderFactory.createTitledBorder("메뉴"));

			reset = new JButton("Reset");
			reset.setFont(d2Coding);
			reset.setBounds(20, 20, 75, 50);
			reset.setBackground(Color.white);
			menu.add(reset);

			undo = new JButton("Undo");
			undo.setFont(d2Coding);
			undo.setBounds(102, 20, 75, 50);
			undo.setBackground(Color.white);
			menu.add(undo);
			
			doAI = new JButton("doAI");
			doAI.setFont(d2Coding);
			doAI.setBounds(185, 20, 75, 50);
			doAI.setBackground(Color.white);
			menu.add(doAI);
			
			//undo = new JButton("Undo");
			//undo.setFont(d2Coding);
			//undo.setBounds(140, 20, 90, 50);
			//undo.setBackground(Color.white);
			menu.add(undo);

			String[] headers = { "AI상태", "턴상태", "포커스 타일", "전체타일상태" };
			int index = 0;
			for (String head : headers) {
				JLabel label = new JLabel();
				label.setFont(d2Coding);
				label.setText(head);
				label.setBounds(20, 80 + 55 * index, 240, 50);
				label.setBorder(BorderFactory.createTitledBorder(head));
				menu.add(label);
				if (index == 0) {
					this.aiStatus = label;
				} else if (index == 1) {
					this.turnStatus = label;
				} else if (index == 2) {
					this.focus = label;
				} else if (index == 3) {
					this.tileSTatus = label;
				}
				index++;
			}

			logs = new JTextArea();
			logs.setFont(d2Coding);
			logs.setText(this.logText);

			logsScroll = new JScrollPane(logs);
			logsScroll.setBounds(20, 300, 240, 320);
			logsScroll.setBackground(Color.white);
			logsScroll.setBorder(BorderFactory.createTitledBorder("게임 로그"));
			menu.add(logsScroll);

			this.add(menu);
		}

		data = new GameData();
		gameManagerV1 = new GameManagerV1(this, data);
		gameManagerV2 = new GameManagerV2(this, data);
		gameManagerV3 = new GameManagerV3(this, data);

		gameManagerV2.setGameManagerV3(gameManagerV3);
		gameManagerV3.setGameManagerV2(gameManagerV2);

		// 게임패널 선언
		{
			game = new JPanel();
			game.setBounds(310, 10, 2816, 2816);
			game.setBorder(BorderFactory.createTitledBorder("TraxGame"));
			game.setLayout(new GridLayout(64, 64));

			for (int i = 0; i < data.tiles.length; i++) {
				for (int j = 0; j < data.tiles[i].length; j++) {
					game.add(data.tiles[i][j]);
				}
			}

			this.add(game);
		}

		// 스크롤패널 선언
		{
			scroll = new JScrollPane(game);
			scroll.setBounds(310, 20, 640, 640);
			this.add(scroll);

			Dimension size = scroll.getViewport().getViewSize();
			Rectangle bounds = scroll.getBounds();

			int x = (size.width - bounds.width) / 2;
			int y = (size.height - bounds.height) / 2;
			scroll.getViewport().setViewPosition(new Point(x + 22, y + 22));
		}

		// 이벤트 등록
		{
			ActionListener buttonListener = this;
			MouseListener tileListener = gameManagerV2;

			reset.addActionListener(buttonListener);
			undo.addActionListener(buttonListener);
			doAI.addActionListener(buttonListener);

			for (int i = 0; i < data.tiles.length; i++) {
				for (int j = 0; j < data.tiles[i].length; j++) {
					data.tiles[i][j].addMouseListener(tileListener);
				}
			}
		}
		this.logLine("PrimaryPanel: 초기화 완료");
		this.logLine("=====================");
	}

	// 리셋 버튼
	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.equals("Reset")) {
			this.logLine("Reset Called");
			//this.logLine("=====================");
			gameManagerV1.actionPerformed(null);
			gameManagerV2.init();
			gameManagerV3.init();
		} else if (command.equals("Undo")) {
			this.logLine("Undo Called");
			//this.logLine("=====================");
			gameManagerV2.undo();
		}else if(command.equals("doAI")) {
			//this.logLine("doAI Called");
			//this.logLine("=====================");
			gameManagerV2.callAI();
		}
	}

	public void logLine(String line) {
		System.out.println(line);
		this.logText += line + "\n";
		this.logs.setText(logText);
		
		logs.setCaretPosition(logs.getDocument().getLength());
	}
}