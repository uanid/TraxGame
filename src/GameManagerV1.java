import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GameManagerV1 implements MouseListener, ActionListener {

	private static Tile temp;

	private GameData data;
	private PrimaryPanel panel;

	public Vector2 focus = new Vector2();
	public boolean firstSet = true;
	public static int num = 0;
	public static int player_num = 0;
	public boolean running = true;

	public GameManagerV1(PrimaryPanel panel, GameData data) {
		this.data = data;
		this.panel = panel;
	}

	private Tile getTile(int x, int y) {
		if (x == -1 || y == -1 || x == 64 || y == 64) {
			return null;
		} else {
			return data.tiles[y][x];
		}
	}

	private State getTileState(int x, int y) {
		Tile tb = this.getTile(x, y);
		if (tb == null) {
			return State.GRASS;
		}
		if (!tb.isFixed()) {
			return State.GRASS;
		}
		return tb.getState();
	}

	// 사이드 타일이랑 메인 타일이랑 연결할 수 있는가
	private boolean isAvailableConnect(int side, int main) {
		// System.out.println("side: " + side + ", main: " + main);
		if (side == 0) {
			return true;
		}
		if (side == main)
			return true;
		return false;
	}

	private List<State> getAvailableTileState(Tile targetTile) {
		List<State> states = new ArrayList<>();

		int x = targetTile.getLocX();
		int y = targetTile.getLocY();

		State left = this.getTileState(x - 1, y);
		State right = this.getTileState(x + 1, y);
		State top = this.getTileState(x, y - 1);
		State bot = this.getTileState(x, y + 1);
		// TileState main = targetTile.getState();

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
			} else {
				// System.out.println(state.name() + ":" + a + "," + b + "," + c + "," + d);
			}
		}

		return states;
	}

	private int CircleWin(Tile tempState, Tile main, int x, int y, int Color) {
		Tile left = this.getTile(x - 1, y);
		Tile right = this.getTile(x + 1, y);
		Tile top = this.getTile(x, y - 1);
		Tile bot = this.getTile(x, y + 1);

		State lefts = this.getTileState(x - 1, y);
		State rights = this.getTileState(x + 1, y);
		State tops = this.getTileState(x, y - 1);
		State bots = this.getTileState(x, y + 1);

		// System.out.println("main "+main);
		boolean a = lefts.getRight() == main.getState().getLeft();
		boolean b = rights.getLeft() == main.getState().getRight();
		boolean c = tops.getBottom() == main.getState().getTop();
		boolean d = bots.getTop() == main.getState().getBottom();
		int r = 0;

		if (left != tempState && a && lefts.getRight() == Color) {
			if (left == temp)
				return Color;
			else
				r = CircleWin(main, left, x - 1, y, Color);
		}
		if (right != tempState && b && rights.getLeft() == Color) {
			if (right == temp)
				return Color;
			else
				r = CircleWin(main, right, x + 1, y, Color);
		}
		if (top != tempState && c && tops.getBottom() == Color) {
			if (top == temp)
				return Color;
			else
				r = CircleWin(main, top, x, y - 1, Color);
		}
		if (bot != tempState && d && bots.getTop() == Color) {
			if (bot == temp)
				return Color;
			else
				r = CircleWin(main, bot, x, y + 1, Color);
		}
		return r;
	}

	private int LineWin(int color) {
		int l = -1, r = -1, t = -1, b = -1; // 타일들의 상하좌우 끝 인덱스 값 변수
		int i, j;
		int cnt = 0;
		int X, Y;
		Tile temp2, temp3;
		for (i = 0; i < 64; i++) { // 타일들의 위 아래 끝 인덱스 구하기 (매번 구해야 하므로 비효율적)
			for (j = 0; j < 64; j++) {
				if (data.tiles[i][j].getState() != State.GRASS && t == -1) {
					t = i;
					b = i;
					break;
				} else if (data.tiles[i][j].getState() != State.GRASS) {
					b = i;
					break;
				}
			}
		}
		for (i = 0; i < 64; i++) { // 타일들의 좌 우 끝 인덱스 구하기
			for (j = 0; j < 64; j++) {
				if (data.tiles[j][i].getState() != State.GRASS && l == -1) {
					l = i;
					r = i;
					break;
				} else if (data.tiles[j][i].getState() != State.GRASS) {
					r = i;
					break;
				}
			}
		}
		// System.out.printf("left : %d right : %d top : %d bot : %d\n",l,r,t,b); //모여있는
		// 타일들의 상하좌우 끝 인덱스 값

		for (j = l; j <= r; j++) { // 맨 위쪽 타일들에 대해 판별 (세로줄판별)
			cnt = 0; // 타일 길이를 저장하는 변수

			// 위에서 아래방향으로 탐색
			if (data.tiles[t][j].getState().getTop() == color) // 위쪽으로 타일이 열려있어야한다.
			{
				temp2 = data.tiles[t][j];
				temp3 = temp2; // temp3는 밑의 반복문에서 이동할 때 왔던 타일로 다시 못가게 하는 변수
				X = temp2.getLocX();
				Y = temp2.getLocY();

				// 맨처음 타일이 상하좌우 맨 끝 라인이 아니라는 조건안에서 동작
				while (temp2.getState() != State.GRASS) // GRASS가 아닐동안 반복
				{
					// 아래방향으로 이동
					if (temp2.getState().getBottom() == this.getTileState(X, Y + 1).getTop()
							&& temp2.getState().getBottom() == color && this.getTile(X, Y + 1) != temp3) {
						cnt++; // 개수 증가
						Y++;
						temp3 = temp2;
					}
					// 왼쪽방향으로 이동
					else if (temp2.getState().getLeft() == this.getTileState(X - 1, Y).getRight()
							&& temp2.getState().getLeft() == color && this.getTile(X - 1, Y) != temp3) {
						X--;
						temp3 = temp2; // 개수 변동x
					}
					// 오른쪽방향으로 이동
					else if (temp2.getState().getRight() == this.getTileState(X + 1, Y).getLeft()
							&& temp2.getState().getRight() == color && this.getTile(X + 1, Y) != temp3) {
						X++;
						temp3 = temp2; // 개수 변동x
					}
					// 위쪽방향으로 이동
					else if (temp2.getState().getTop() == this.getTileState(X, Y - 1).getBottom()
							&& temp2.getState().getTop() == color && this.getTile(X, Y - 1) != temp3) {
						cnt--; // 개수 감소
						Y--;
						temp3 = temp2;
					} else
						break;
					temp2 = this.getTile(X, Y);
					if (t + cnt >= b && cnt >= 7 && temp2.getState().getBottom() == color) // 아래방향이 얼려있으면서 위 아래 끝 타일이
																							// 바깥쪽 가장자리이면서 적어도 8행의 타일을
																							// 가져야한다.
						return color;
				}
			}
		}
		for (i = t; i <= b; i++) { // 맨 오른쪽 타일드렝 대해 판별 (가로줄 판별)
			cnt = 0; // 타일 길이를 저장하는 변수

			// 왼쪽에서 오른쪽 방향으로 탐색
			if (data.tiles[i][l].getState().getLeft() == color) // 위쪽으로 타일이 열려있어야한다.
			{
				temp2 = data.tiles[i][l];
				temp3 = temp2;
				X = temp2.getLocX();
				Y = temp2.getLocY();

				// 맨처음 타일이 상하좌우 맨 끝 라인이 아니라는 조건안에서 동작
				while (temp2.getState() != State.GRASS) // GRASS가 아닐동안 반복
				{
					if (temp2.getState().getBottom() == this.getTileState(X, Y + 1).getTop()
							&& temp2.getState().getBottom() == color && this.getTile(X, Y + 1) != temp3) {
						Y++;
						temp3 = temp2;
					} else if (temp2.getState().getLeft() == this.getTileState(X - 1, Y).getRight()
							&& temp2.getState().getLeft() == color && this.getTile(X - 1, Y) != temp3) {
						cnt--;
						X--;
						temp3 = temp2;
					}

					else if (temp2.getState().getRight() == this.getTileState(X + 1, Y).getLeft()
							&& temp2.getState().getRight() == color && this.getTile(X + 1, Y) != temp3) {
						cnt++;
						X++;
						temp3 = temp2;
					} else if (temp2.getState().getTop() == this.getTileState(X, Y - 1).getBottom()
							&& temp2.getState().getTop() == color && this.getTile(X, Y - 1) != temp3) {
						Y--;
						temp3 = temp2;
					} else
						break;
					temp2 = this.getTile(X, Y);
					if (l + cnt >= r && cnt >= 7 && temp2.getState().getRight() == color)
						return color;
				}
			}
		}
		return 0;
	}

	private void updateTile(Tile tile, List<Tile> updatedTiles) {
		// 주변 타일 자동완성
		int x = tile.getLocX();
		int y = tile.getLocY();

		List<Tile> relateTiles = new ArrayList<>();
		if (this.getTile(x - 1, y) != null) {
			relateTiles.add(this.getTile(x - 1, y));
		}
		if (this.getTile(x + 1, y) != null) {
			relateTiles.add(this.getTile(x + 1, y));
		}
		if (this.getTile(x, y - 1) != null) {
			relateTiles.add(this.getTile(x, y - 1));
		}
		if (this.getTile(x, y + 1) != null) {
			relateTiles.add(this.getTile(x, y + 1));
		}

		for (Tile tileButton : relateTiles) {
			if (!tileButton.isFixed()) {
				List<State> states = this.getAvailableTileState(tileButton);
				if (states.size() == 0) {
					System.out.println("긴급상황 가능한 타일 0개" + tileButton.toString());
				} else if (states.size() == 1) {
					tileButton.setState(states.get(0));
					tileButton.setFixed(true);
					// System.out.println("타일 하나 자동완성됨");
					updatedTiles.add(tileButton);
					num++;
					// 재귀적 구조
					this.updateTile(tileButton, updatedTiles);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		this.firstSet = true;
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				this.data.tiles[i][j].setState(State.GRASS);
				this.data.tiles[i][j].setStateIndex(0);
				this.data.tiles[i][j].setFixed(false);
			}
		}
		running = true;
		num = 0;
		player_num = 0;
		System.out.println("White to play");
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		boolean isRight = SwingUtilities.isRightMouseButton(event);
		boolean isLeft = SwingUtilities.isLeftMouseButton(event);
		Tile tile = (Tile) event.getSource();
		if (running) {
			if (isLeft) {
				if (!tile.isFixed()) {
					// 옆에 타일이 없으면 놓지 못하도록 함
					{
						int x = tile.getLocX();
						int y = tile.getLocY();
						boolean canPlaceTile = false;
						canPlaceTile = this.getTileState(x - 1, y) != State.GRASS
								|| this.getTileState(x + 1, y) != State.GRASS
								|| this.getTileState(x, y - 1) != State.GRASS
								|| this.getTileState(x, y + 1) != State.GRASS;

						canPlaceTile = canPlaceTile || this.firstSet;

						if (!canPlaceTile) {
							// 여기서 이벤트 종료
							System.out.println("이벤트 종료");
							return;
						}
					}

					// 포커스된 타일이랑 클릭한거랑 다를 경우 포커스 이동
					if (focus.x != tile.getLocX() || focus.y != tile.getLocY()) {
						Tile fTile = this.getTile(focus.x, focus.y);
						if (!fTile.isFixed()) {
							fTile.setState(State.GRASS);
							fTile.setStateIndex(0);
						}
						focus.x = tile.getLocX();
						focus.y = tile.getLocY();
						tile.setStateIndex(0);
					}

					// 타일 상태 변경
					{
						List<State> states = this.getAvailableTileState(tile);
						int index = (tile.getStateIndex() + 1) % states.size();
						tile.setStateIndex(index);
						tile.setState(states.get(index));
						// System.out.println("타일 상태 변경 size:" + states.size());
						// System.out.println(states.toString());
						// System.out.println(tile.toString());
					}

				}
			} else if (isRight) {
				if (!tile.isFixed() && tile.getState() != State.GRASS) {
					if (this.firstSet) {
						this.firstSet = false;
					}

					// 타일 고정
					tile.setFixed(true);

					num++; // 전체 타일 개수
					player_num++;
					player_num = player_num % 2; // 누가 마지막으로 두었는지(white가 마지막으로 둔 경우 1)
					// 타일 자동완성
					List<Tile> list = new ArrayList<>();
					list.add(tile);
					this.updateTile(tile, list); // 클릭하여 추가된 타일요소(자동생성된 타일들도 포함)들을 추가, 이후 circle 승리판별에 사용
					// System.out.println("num : " + num + " player_num : " + player_num);

					// 승리여부 판별
					if (player_num == 0) // 검정색 플레이어가 마지막으로 둔 경우
					{
						// 검정색 플레이어의 승리판별 먼저
						for (Tile t : list) {
							temp = t;
							if (this.CircleWin(temp, t, t.getLocX(), t.getLocY(), 1) == 1) // //검은색 circle 판별
							{
								JOptionPane.showMessageDialog(null, "Black Win!");
								running = false;
								break;
							}
						}
						if (LineWin(1) == 1) { // black line 판별
							JOptionPane.showMessageDialog(null, "Black Win!");
							running = false;
						}

						// 게임이 끝나지 않았다면
						if (running) {
							// 흰색 플레이어의 승리여부도 확인
							for (Tile t : list) {
								temp = t;
								if (this.CircleWin(temp, t, t.getLocX(), t.getLocY(), 2) == 2) {
									JOptionPane.showMessageDialog(null, "White Win!");
									running = false;
									break;
								}
							}
							if (LineWin(2) == 2) { // white line 판별
								JOptionPane.showMessageDialog(null, "White Win!");
								running = false;
							}
						}
					} else // 흰색 플레이어가 마지막으로 둔 경우
					{
						// 흰색 플레이어의 승리판별 먼저
						for (Tile t : list) {
							temp = t;
							if (this.CircleWin(temp, t, t.getLocX(), t.getLocY(), 2) == 2) { // 흰색 circle 판별
								JOptionPane.showMessageDialog(null, "White Win!");
								running = false;
								break;
							}
						}
						if (LineWin(2) == 2) { // white line 판별
							JOptionPane.showMessageDialog(null, "White Win!");
							running = false;
						}

						// 게임이 끝나지 않았다면
						if (running) {
							// 검정색 플레이어의 승리여부도 확인
							for (Tile t : list) {
								temp = t;
								if (this.CircleWin(temp, t, t.getLocX(), t.getLocY(), 1) == 1) {
									JOptionPane.showMessageDialog(null, "Black Win!");
									running = false;
									break;
								}
							}
							if (LineWin(1) == 1) { // black line 8칸
								JOptionPane.showMessageDialog(null, "Black Win!");
								running = false;
							}
						}
					}

					if (running) {
						if (player_num == 1)
							System.out.println("Black to play");
						else
							System.out.println("White to play");
					}

				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
