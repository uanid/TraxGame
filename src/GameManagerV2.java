import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GameManagerV2 implements MouseListener {
	public static final String WHITE = "White";
	public static final String BLACK = "Black";

	private GameData data;
	private PrimaryPanel panel;
	private GameManagerV3 gamev3;
	private GameManagerV2 gamev2;

	private Vector2 firstFocus = new Vector2(32, 32);
	private State[] firstStates = new State[] { State.IMAGE5, State.IMAGE3 };
	private Timer timer = new Timer();

	public List<Tile> fixedTiles;
	private boolean isFirstSetted;
	private String turn;
	private String aiColor;
	private boolean isAITurn;
	private Vector2 focus;
	private int turnCount;

	private Tile lastFixedTile = null;
	private List<Tile> lastFixedTiles;

	public GameManagerV2(PrimaryPanel panel, GameData data) {
		this.data = data;
		this.panel = panel;
		this.gamev2 = this;
		this.init();
	}

	public void setGameManagerV3(GameManagerV3 gamev3) {
		this.gamev3 = gamev3;
	}

	public void init() {
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				this.data.tiles[i][j].setState(State.GRASS);
				this.data.tiles[i][j].setStateIndex(0);
				this.data.tiles[i][j].setFixed(false);
				this.data.tiles[i][j].setBlue(false);
			}
		}
		this.fixedTiles = new ArrayList<>();
		this.lastFixedTiles = new ArrayList<>();
		this.focus = new Vector2(0, 0);
		this.isFirstSetted = false;
		this.turn = WHITE;
		this.aiColor = BLACK; // BLACK 이면 ai가 후턴 , WHITE이면 ai가 선턴
		this.isAITurn = this.aiColor.equals(turn);
		this.turnCount = 1;
		this.lastFixedTile = null;

		this.updateStatusLabel();
		panel.logLine("GameManagerV2: 초기화 완료");
	}

	public void undo() {
		for (Tile tile : lastFixedTiles) {
			tile.setState(State.GRASS);
			tile.setBlue(false);
			tile.setFixed(false);
		}

		if (this.turn.equals(WHITE)) {
			this.turn = BLACK;
		} else {
			this.turn = WHITE;
		}
		this.isAITurn = this.aiColor.equals(turn);

		if (this.isAITurn) {
			
		}
	}

	public void callAI() {
		Tile t = this.getTile(focus.x, focus.y);
		if(!t.isFixed()) {
			t.setState(State.GRASS);
		}
		
		if (!this.isFirstSetted) {
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					Tile tile = gamev2.getTile(firstFocus.x, firstFocus.y);
					tile.setState(firstStates[0]);
					gamev2.doTileFix(tile, "doAI.v0");
				}
			}, 1);
		} else {
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					try {
						gamev3.doAI2(gamev2.turn); // ai 컬러를 인자로 준다.						
					} catch (IllegalArgumentException e) {
						JOptionPane.showMessageDialog(null, "(" + gamev2.turn.charAt(0) + "+Resign) was added to the game information.", "doAI.v2 resign", JOptionPane.PLAIN_MESSAGE);
		//				JOptionPane.showMessageDialog(null, "doAI.v2 resign");
					}
				}
			}, 1);
		}
	}

	public boolean isAvailableLocation(int x, int y) {
		return !(x == -1 || y == -1 || x == 64 || y == 64);
	}

	public Tile getTile(int x, int y) {
		if (!isAvailableLocation(x, y)) {
			return data.outsideGrassTile;
		} else {
			return data.tiles[y][x];
		}
	}

	// center: 본체 타일, side: 옆 타일
	public boolean isAvailableConnect(int side, int center) {
		if (side == 0) {
			return true;
		}
		return side == center;
	}

	public Vector2 getSideLocation(Tile center, Direction direction) {
		switch (direction) {
		case TOP:
			return new Vector2(center.getLocX(), center.getLocY() - 1);
		case BOTTOM:
			return new Vector2(center.getLocX(), center.getLocY() + 1);
		case LEFT:
			return new Vector2(center.getLocX() - 1, center.getLocY());
		case RIGHT:
			return new Vector2(center.getLocX() + 1, center.getLocY());
		}
		return null;
	}

	public Tile getSideTile(Tile center, Direction direction) {
		Vector2 targetLoc = this.getSideLocation(center, direction);
		return this.getTile(targetLoc.x, targetLoc.y);
	}

	public List<Tile> getSideGrassTiles() {
		List<Tile> list = new ArrayList<>();
		for (Tile tile : this.fixedTiles) {
			for (Direction dir : Direction.values()) {
				Tile side = this.getSideTile(tile, dir);
				if (side.getState() == State.GRASS) {
					if (!list.contains(side)) {
						list.add(side);
					}
				}
			}
		}
		return list;
	}

	private List<State> getAvailableTileState(Tile center) {
		List<State> states = new ArrayList<>();

		int x = center.getLocX();
		int y = center.getLocY();

		State left = this.getTile(x - 1, y).getState();
		State right = this.getTile(x + 1, y).getState();
		State top = this.getTile(x, y - 1).getState();
		State bot = this.getTile(x, y + 1).getState();

		for (State state : State.values()) {
			if (state == State.GRASS) {
				continue;
			} // grass일때는 넘어감

			boolean a = this.isAvailableConnect(left.getRight(), state.getLeft());
			boolean b = this.isAvailableConnect(right.getLeft(), state.getRight());
			boolean c = this.isAvailableConnect(top.getBottom(), state.getTop());
			boolean d = this.isAvailableConnect(bot.getTop(), state.getBottom());

			if (a && b && c && d) {
				// System.out.println(state.name() + "리스트에 추가함");
				states.add(state);
			}
		}

		return states;
	}

	// 연결 여부 반환
	private boolean validCircleWinCondition(Tile start, Tile center, Direction direction, boolean isFirstCall) {
		if (start.equals(center) && !isFirstCall) {
			// System.out.println("end cycle connected!");
			return true;
		}

		Tile side = this.getSideTile(center, direction);
		State sideState = side.getState();
		Direction targetDir = sideState.getLinkDirection(direction.opposite());
		// System.out.println("side " + side.toSimpleString() + " to " +
		// targetDir.toString());

		if (sideState == State.GRASS) {
			// System.out.println("end cycle routin");
			return false;
		}

		return this.validCircleWinCondition(start, side, targetDir, false);
	}

	private int validLineWinCondition() {
		Vector2 min = new Vector2(32, 32);
		Vector2 max = new Vector2(32, 32);
		for (Tile tile : this.fixedTiles) {
			if (min.x > tile.getLocX()) {
				min.x = tile.getLocX();
			}
			if (min.y > tile.getLocY()) {
				min.y = tile.getLocY();
			}
			if (max.x < tile.getLocX()) {
				max.x = tile.getLocX();
			}
			if (max.y < tile.getLocY()) {
				max.y = tile.getLocY();
			}
		}

		List<Tile> topTiles = new ArrayList<>();
		List<Tile> leftTiles = new ArrayList<>();

		for (int x = 0; x < 64; x++) {
			Tile tile = this.getTile(x, min.y);
			if (tile.getState() != State.GRASS) {
				topTiles.add(tile);
			}
		}

		for (int y = 0; y < 64; y++) {
			Tile tile = this.getTile(min.x, y);
			if (tile.getState() != State.GRASS) {
				leftTiles.add(tile);
			}
		}

		boolean isBlackWin = false;
		boolean isWhiteWin = false;

		// System.out.println("--min: " + min.toString());
		// System.out.println("--max: " + max.toString());
		// System.out.println("##top: " + topTiles.size());
		for (Tile tile : topTiles) {
			State state = tile.getState();
			Direction dir = state.getLinkDirection(Direction.TOP);
			Tile endTile = this.getLineEndTile(tile, dir);
			if (endTile.getState().getTrack(Direction.BOTTOM) == state.getTrack(Direction.TOP)) {
				if (endTile.getLocY() == max.y) {
					int startY = tile.getLocY();
					int endY = endTile.getLocY();
					if (Math.abs(startY - endY) >= 7) {
						if (state.getTrack(Direction.TOP) == 1) {
							isBlackWin = true;
						} else {
							isWhiteWin = true;
						}
					}
				}
			}
		}

		// System.out.println("##left: " + leftTiles.size());
		for (Tile tile : leftTiles) {
			State state = tile.getState();
			Direction dir = state.getLinkDirection(Direction.LEFT);
			Tile endTile = this.getLineEndTile(tile, dir);
			// System.out.println("end " + endTile.toSimpleString());
			// System.out.println("start: left " + state.getTrack(Direction.LEFT));
			// System.out.println("end : right " +
			// endTile.getState().getTrack(Direction.RIGHT));
			// System.out.println("end : " + endTile.getLoc().toString());
			// System.out.println("max : " + max.toString());
			if (endTile.getState().getTrack(Direction.RIGHT) == state.getTrack(Direction.LEFT)) {
				if (endTile.getLocX() == max.x) {
					int startX = tile.getLocX();
					int endX = endTile.getLocX();
					// System.out.println("dis : " + Math.abs(startX - endX));
					if (Math.abs(startX - endX) >= 7) {
						if (state.getTrack(Direction.LEFT) == 1) {
							isBlackWin = true;
						} else {
							isWhiteWin = true;
						}
					}
				}
			}
		}

		int result = 0;
		if (isBlackWin) {
			result += 2;
		}
		if (isWhiteWin) {
			result += 1;
		}
		return result;
	}

	public Tile getLineEndTile(Tile startTile, Direction direction) {
		// System.out.println("start " + startTile.toSimpleString());
		// System.out.println("dir " + direction.toString());
		Tile moveTile = startTile;
		Direction moveDirection = direction;

		Tile endTile = startTile;
		while (true) {
			Tile sideTile = this.getSideTile(moveTile, moveDirection);
			State sideState = sideTile.getState();

			if (sideState == State.GRASS) {
				endTile = moveTile;
				break;

			}

			moveDirection = sideState.getLinkDirection(moveDirection.opposite());
			moveTile = sideTile;
			// System.out.println("move " + moveTile.toSimpleString());
			// System.out.println("dir " + moveDirection.toString());
		}
		return endTile;
	}

	private void autoCompletionTiles(Tile center, List<Tile> updatedTiles) {
		// 주변 타일 자동완성
		for (Direction dir : Direction.values()) {
			Tile tile = this.getSideTile(center, dir);
			if (tile.getState() != State.GRASS) {
				continue;
			}

			if (!tile.isFixed()) {
				// System.out.println(dir.toString());
				// System.out.println(tile.toSimpleString());
				List<State> states = this.getAvailableTileState(tile);
				if (states.size() == 0) {
					System.out.println("자동완성: 가능한 타일 0개" + tile.toString());
				} else if (states.size() == 1) {
					tile.setState(states.get(0));
					tile.setFixed(true);

					this.fixedTiles.add(tile);
					updatedTiles.add(tile);
					this.autoCompletionTiles(tile, updatedTiles);
				}
			}
		}
	}

	public void updateStatusLabel() {
		this.panel.aiStatus.setText("  " + (isAITurn ? "AI가 생각하는 중..." : "사람을 기다리는 중..."));
		this.panel.turnStatus.setText("  " + turn + " 차례 ");
		this.panel.focus.setText("  " + focus.toString());
		this.panel.tileSTatus.setText("  " + "놓인 타일: " + this.fixedTiles.size());

		// this.panel.playerInfo.setText(text);
	}

	public void doChangeState(Tile tile) {
		// 첫 번째 타일
		if (!this.isFirstSetted) {
			focus.x = firstFocus.x;
			focus.y = firstFocus.y;
			tile = this.getTile(firstFocus.x, firstFocus.y);
			int index = (tile.getStateIndex() + 1) % firstStates.length;
			tile.setStateIndex(index);
			tile.setState(firstStates[index]);
			tile.reDraw();
			this.updateStatusLabel();
			return;
		}

		// 옆에 타일이 없으면 놓지 못하도록 함
		{
			boolean canPlaceTile = false;
			for (Direction dir : Direction.values()) {
				Tile t = this.getSideTile(tile, dir);
				if (t.isFixed() && t.getState() != State.GRASS) {
					canPlaceTile = true;
				}
			}

			// 주변에 타일이 하나도 없음
			if (!canPlaceTile) {
				// System.out.println("엉뚱한 곳 클릭함");
				return;
			}
		}

		// 포커싱된 타일이랑 클릭한 타일이랑 다를 경우 포커스 이동
		if (!focus.equals(tile.getLoc())) {
			Tile fTile = this.getTile(focus.x, focus.y);
			if (!fTile.isFixed()) {
				// 포커싱된 타일을 풀어줌
				fTile.setState(State.GRASS);
				fTile.setStateIndex(0);
			}
			focus = tile.getLoc();
			tile.setStateIndex(0);
		}

		// 타일 상태 변경
		{
			List<State> states = this.getAvailableTileState(tile);
			int index = (tile.getStateIndex() + 1) % states.size();
			tile.setStateIndex(index);
			tile.setState(states.get(index));
		}

		this.updateStatusLabel();
	}

	public void doTileFix(Tile tile, String who) {
		if (lastFixedTile != null) {
			lastFixedTile.setBlue(false);
		}

		tile.setFixed(true);
		tile.setBlue(true);
		lastFixedTile = tile;
		this.fixedTiles.add(tile);
		panel.logLine("Turn " + this.turn + "(" + who + ")");

		if (this.turn.equals(WHITE)) {
			this.turn = BLACK;
		} else {
			this.turn = WHITE;
		}
		this.isAITurn = this.aiColor.equals(turn);

		if (!this.isFirstSetted) {
			this.isFirstSetted = true;
			// return;
		}

		// 타일 자동완성
		List<Tile> autoCompletedTiles = new ArrayList<>();
		autoCompletedTiles.add(tile);
		this.autoCompletionTiles(tile, autoCompletedTiles); // 클릭하여 추가된 타일요소(자동생성된 타일들도 포함)들을 추가, 이후 circle

		// undo를 위한 이전 상태 타일들
		this.lastFixedTiles.clear();
		autoCompletedTiles.stream().forEach(t -> lastFixedTiles.add(t));

		// System.out.println();
		panel.logLine("Fixed: " + tile.toSimpleString());
		for (int i = 1; i < autoCompletedTiles.size(); i++) {
			panel.logLine("Auto : " + autoCompletedTiles.get(i).toSimpleString());
		}
		panel.logLine("=====================");

		// System.out.println("자동완성 종료");

		int color = turn.equals(BLACK) ? 1 : 2;
		boolean isBlackWin = false;
		boolean isWhiteWin = false;

		// 자동완성된 타일에 대해서 승리판별 작업
		// 원 승리조건
		for (Tile t : autoCompletedTiles) {
			// 모든 방향에 대해서
			for (Direction dir : Direction.values()) {
				// System.out.println("sta cycle routin");
				// System.out.println("from " + t.toSimpleString());
				boolean isConnected = this.validCircleWinCondition(t, t, dir, true);
				if (isConnected) {
					if (t.getState().getTrack(dir) == 1) {
						isBlackWin = true;
						// System.out.println("Cycle Black Win");
					} else {
						isWhiteWin = true;
						// System.out.println("Cycle White Win");
					}
				}
			}
		}

		// 라인 승리조건
		int result = this.validLineWinCondition();
		if (result >= 2) {
			isBlackWin = true;
			// System.out.println("Line Black Win");
		}
		if (result % 2 == 1) {
			isWhiteWin = true;
			// System.out.println("Line White Win");
		}

		if (isBlackWin && color == 1) {
			JOptionPane.showMessageDialog(null, "Black Win!");
		} else if (isWhiteWin && color == 2) {
			JOptionPane.showMessageDialog(null, "White Win!");
		} else if (isBlackWin) {
			JOptionPane.showMessageDialog(null, "Black Win!");
		} else if (isWhiteWin) {
			JOptionPane.showMessageDialog(null, "White Win!");
		}

		this.turnCount++;
		this.updateStatusLabel();

		if (this.isAITurn) {
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (this.isAITurn) {
			//return;
		}

		boolean isRight = SwingUtilities.isRightMouseButton(event);
		boolean isLeft = SwingUtilities.isLeftMouseButton(event);
		Tile tile = (Tile) event.getSource();
		if (isLeft) {
			// 이미 고정된 타일이면 무시
			if (!tile.isFixed()) {
				this.doChangeState(tile);
			}
		} else if (isRight) {
			if (!tile.isFixed() && tile.getState() != State.GRASS) {
				this.doTileFix(tile, "사람");
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

}
