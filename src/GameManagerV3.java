import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

//import javafx.util.Pair;

public class GameManagerV3 {
	private static final String WHITE = "White";
	private static final String BLACK = "Black";
	private GameData data;
	private PrimaryPanel panel;
	private GameManagerV2 gamev2;
	private SimpleTile outsideGrassTile;

	private List<SimpleTile> fixedTiles;
	private SimpleTile[][] dumpedTile;

	public GameManagerV3(PrimaryPanel panel, GameData data) {
		this.data = data;
		this.panel = panel;
		this.init();
	}

	public void setGameManagerV2(GameManagerV2 gamev2) {
		this.gamev2 = gamev2;
	}

	public void init() {
		this.fixedTiles = new ArrayList<>();
		this.outsideGrassTile = new SimpleTile(-10, -10);
		this.dumpedTile = new SimpleTile[64][64];
		for (int i = 0; i < 64; i++) {
			for (int j = 0; j < 64; j++) {
				this.dumpedTile[j][i] = new SimpleTile(i, j);
			}
		}
		panel.logLine("GameManagerV3: 초기화 완료");
	}

	public SimpleTile toSimpleTile(Tile tile) {
		return new SimpleTile(tile.getLocX(), tile.getLocY(), tile.isFixed(), tile.getState());
	}

	// 타일 복제
	public void dumpTile() {
		this.fixedTiles.clear();

		for (Tile tile : gamev2.fixedTiles) {
			Vector2 loc = tile.getLoc();
			SimpleTile stile = this.toSimpleTile(tile);
			dumpedTile[loc.y][loc.x] = stile;
			this.fixedTiles.add(stile);
		}
	}

	public void setGamev2(List<SimpleTile> tiles, boolean fix, boolean blue) {
		for (SimpleTile t2 : tiles) {
			Tile t = gamev2.getTile(t2.getLocX(), t2.getLocY());
			if (blue) {
				if (fix) {
					t.setState(State.IMAGE1);
					t.setFixed(true);
					t.setBlue(true);
				} else {
					t.setState(State.GRASS);
					t.setFixed(false);
					t.setBlue(false);
				}
			} else {
				if (fix) {
					t.setState(t2.getState());
					t.setFixed(true);
				} else {
					t.setState(State.GRASS);
					t.setFixed(false);
				}
			}
		}
	}

	public void recover2(List<SimpleTile> tiles) {
		for (SimpleTile t2 : tiles) {
			t2.setState(State.GRASS);
			t2.setFixed(false);
			fixedTiles.remove(t2);
		}
	}

	

	private SimpleTile c1 = null;
	private List<SimpleTile> cc1 = new ArrayList<>();
	private SimpleTile c2 = null;
	private SimpleTile[] c3 = new SimpleTile[3];
	private List<SimpleTile[]> cc3 = new ArrayList<>();
	private List<SimpleTile[]> cc4 = new ArrayList<>();

	private SimpleTile t1 = null;
	private SimpleTile t2 = null;
	private SimpleTile t3 = null;

	public int reculsive2(int count, String aiColor, List<SimpleTile> grassTiles) {
		boolean isAiBlack = aiColor.equals(gamev2.BLACK);
		boolean isAiWhite = aiColor.equals(gamev2.WHITE);

		for (int i = 0; i < grassTiles.size(); i++) {
			SimpleTile tile = grassTiles.get(i);
			List<State> states = this.getAvailableTileState(tile);
			for (int j = 0; j < states.size(); j++) {
				// 타일 설정
				State state = states.get(j);
				tile.setState(state);
				tile.setFixed(true);
				List<SimpleTile> updated = new ArrayList<>();
				updated.add(tile);
				this.autoCompletionTiles(tile, updated);

				boolean isBlackWin = false;
				boolean isWhiteWin = false;
				boolean aiWin = false;
				boolean oppoWin = false;

				// 선 승리조건
				int result = this.validLineWinCondition();
				if (result >= 2) {
					isBlackWin = true;
				}
				if (result % 2 == 1) {
					isWhiteWin = true;
				}

				// 원 승리조건
				for (SimpleTile t : updated) {
					for (Direction dir : Direction.values()) {
						if (this.validCircleWinCondition(t, t, dir, true)) {
							if (t.getState().getTrack(dir) == 1) {
								isBlackWin = true;
							} else {
								isWhiteWin = true;
							}
							if (isBlackWin && isWhiteWin) {
								break;
							}
						}
					}
					if (isBlackWin && isWhiteWin) {
						break;
					}
				}

				if (isBlackWin && isAiBlack) {
					aiWin = true;
				} else if (isWhiteWin && isAiWhite) {
					aiWin = true;
				}
				if (isBlackWin && isAiWhite) {
					oppoWin = true;
				} else if (isWhiteWin && isAiBlack) {
					oppoWin = true;
				}

				if (count == 1) {
					t1 = tile;
				} else if (count == 2) {
					t2 = tile;
				} else {
					t3 = tile;
				}

				boolean ignoreReculsive = false;

				if (count == 1) {
					if (aiWin) {
						c1 = tile.clone();
						throw new IllegalArgumentException("브브브브브");
					} else if (oppoWin) {
						// 무시
						ignoreReculsive = true;
					} else {
					}
				} else if (count == 2) {
					if (oppoWin) {
						c2 = t1.clone();
						this.recover2(updated);
						// System.out.println("# 지는 수 발견 cnt1 파괴함");
						return 2;
					} else if (aiWin) {
						ignoreReculsive = true;
					} else {
					}
				} else if (count == 3) {
					if (aiWin) {
						SimpleTile[] arr = new SimpleTile[] { t1.clone(), t2.clone(), t3.clone() };
						cc3.add(arr);

						if (c2 != null) {
							Iterator<SimpleTile[]> it = cc3.iterator();
							while (it.hasNext()) {
								SimpleTile[] a = it.next();
								if (a[0].equals(c2)) {
									// System.out.println("# cnt3 설정하려고 했으나 cnt1파괴되어있음2");
									it.remove();
								}
							}
						}

					} else if (oppoWin) {
					
					} else {
						
					}
				}

				if (!ignoreReculsive) {
					if (count <= 2) {
						List<SimpleTile> grassTilesR = this.getSideGrassTiles();
						int code = this.reculsive2(count + 1, aiColor, grassTilesR);
						if (code == 2) {
							if (cc3.size() != 0) {
								Iterator<SimpleTile[]> it = cc3.iterator();
								while (it.hasNext()) {
									SimpleTile[] a = it.next();
									if (a[0].equals(c2)) {
										// System.out.println("# cnt3 설정하려고 했으나 cnt1파괴되어있음1");
										it.remove();
									}
								}
							}
							
						} else if (count == 1) {
							cc1.add(tile.clone());
						}
					}
				}
				// 타일 복구
				this.recover2(updated);
			}
		}
		return 0;
	}

	public void doAI2(String aiColor) {
		panel.logLine("doAI.v2: Called (" + aiColor + ")");
		long time = System.currentTimeMillis();
		this.dumpTile();
		Random r = new Random();

		t1 = null;
		t2 = null;
		t3 = null;
		cc1.clear();
		c1 = null;
		c2 = null;
		c3 = new SimpleTile[3];
		cc3.clear();

		SimpleTile result = null;
		try {
			this.reculsive2(1, aiColor, this.getSideGrassTiles());
		} catch (IllegalArgumentException e) {
			panel.logLine("- c1 선택됨");
			result = c1;
		}

		if (result == null) {
			if (cc3.size() != 0) {
				panel.logLine("- cc3 선택됨");
				
				Map<SimpleTile, Integer> map = new HashMap<>();
				for (SimpleTile[] arr : cc3) {
					if (map.containsKey(arr[0])) {
						int c = map.get(arr[0]);
						map.put(arr[0], c + 1);
					} else {
						map.put(arr[0], 1);
					}
				}

				SimpleTile maxt = null;
				int max = 0;
				for (SimpleTile tile : map.keySet()) {
					int count = map.get(tile);
					if (count >= max) {
						max = count;
						maxt = tile;
					}
				}

				panel.logLine("- " + max + "회 중복되어서 선택됨 " + cc3.size() + ", " + map.size());
				result = maxt;
				// result = cc3.get(r.nextInt(cc3.size()))[0];
			}
		}

		if (result == null) {
			if (cc1.size() != 0) {
				panel.logLine("- cc1 랜덤 선택됨");
				result = cc1.get(r.nextInt(cc1.size()));
			}
		}
		

		// SimpleTile result = this.reculsive(1, aiColor);
		if (result == null) {
			panel.logLine("- 전체 랜덤타일 선택됨");
			panel.logLine("- AI Resign");
			this.dumpTile();
			List<SimpleTile> tiles = this.getSideGrassTiles();
			System.out.println(tiles.size() + "개 랜덤선택");
			result = tiles.get(r.nextInt(tiles.size()));
			List<State> states = this.getAvailableTileState(result);
			result.setState(states.get(r.nextInt(states.size())));

		}

		time = System.currentTimeMillis() - time;
		double delta = time / 1000D;
		panel.logLine(String.format("doAI.v2: JobEnd (%.2fs)", delta));
		panel.logLine("");

		Tile t = gamev2.getTile(result.getLocX(), result.getLocY());
		t.setState(result.getState());
		gamev2.doTileFix(t, "doAI.v2");
	}

	public SimpleTile reculsive(int count, String aiColor) {
		SimpleTile blockTarget = null;
		boolean blockOppo = false;

		SimpleTile sindex3Target = null;

		List<SimpleTile> updatedTiles = new ArrayList<>();
		List<SimpleTile> sideGrassTiles = this.getSideGrassTiles();
		for (int i = 0; i < sideGrassTiles.size(); i++) {
			SimpleTile tile = sideGrassTiles.get(i);
			List<State> states = this.getAvailableTileState(tile);
			blockOppo = false;
			for (State state : states) {
				tile.setState(state);
				tile.setFixed(true);
				updatedTiles.clear();
				updatedTiles.add(tile);
				this.autoCompletionTiles(tile, updatedTiles);

				boolean isBlackWin = false;
				boolean isWhiteWin = false;

				// 원 승리조건
				for (SimpleTile t : updatedTiles) {
					for (Direction dir : Direction.values()) {
						if (this.validCircleWinCondition(t, t, dir, true)) {
							if (t.getState().getTrack(dir) == 1) {
								isBlackWin = true;

							} else {
								isWhiteWin = true;
							}

							if (isBlackWin && isWhiteWin) {
								break;
							}
						}
					}
					if (isBlackWin && isWhiteWin) {
						break;
					}
				}

				// 선 승리조건
				int result = this.validLineWinCondition();
				if (result >= 2) {
					isBlackWin = true;
				}
				if (result % 2 == 1) {
					isWhiteWin = true;
				}

				boolean aiWin = false;
				boolean oppoWin = false;

				if (isBlackWin && aiColor.equals(gamev2.BLACK)) {
					aiWin = true;
				} else if (isWhiteWin && aiColor.equals(gamev2.WHITE)) {
					aiWin = true;
				}
				if (isBlackWin && aiColor.equals(gamev2.WHITE)) {
					oppoWin = true;
				} else if (isWhiteWin && aiColor.equals(gamev2.BLACK)) {
					oppoWin = true;
				}

				boolean isContinue = false;
				if (count == 1) {
					if (aiWin) {
						// 바로 이 수로 둔다 -> return
						tile.setStateIndex(1);
						System.out.println("바로 이기는 수 찾음 " + tile.toSimpleString());
						return tile;
					} else if (oppoWin) {
						System.out.println("여기 두면 짐 " + tile.toSimpleString());
						isContinue = true;
					}
				} else if (count == 2) {
					if (oppoWin) {
						blockOppo = true;
						blockTarget = new SimpleTile(tile.getLocX(), tile.getLocY());
						isContinue = true;
						System.out.println("상대가 이기는 수 찾음 " + tile.toSimpleString());
						// 이 수를 막아야 한다 -> 이 자리에 다른 state를 두도록 함
						for (SimpleTile tile2 : updatedTiles) {
							tile2.setState(State.GRASS);
							tile2.setFixed(false);
							this.fixedTiles.remove(tile2);
						}
						blockTarget.setStateIndex(1);
						return blockTarget;
					} else if (aiWin) {
						isContinue = true;
						// 가능성 없음 -> 다음 tile로 continue
					}
				} else if (count == 3) {
					if (aiWin) {
						// 되도록 이 수로 둔다 -> return
						blockTarget = new SimpleTile(tile.getLocX(), tile.getLocY());
						blockTarget.setStateIndex(3);
						blockTarget.setState(tile.getState());
						blockTarget.setFixed(true);
						System.out.println("최종 이기는 수 찾음3 " + blockTarget.toSimpleString());
						for (SimpleTile tile2 : updatedTiles) {
							tile2.setState(State.GRASS);
							tile2.setFixed(false);
							this.fixedTiles.remove(tile2);
						}
						return blockTarget;
					} else if (oppoWin) {
						isContinue = true;
						// 관망한다 -> continue
					} else {
						isContinue = true;
					}
				} else {
					System.out.println("일어나선 안되는 일");
				}

				if (blockOppo && blockTarget != null) {
					blockOppo = false;
					blockTarget.setStateIndex(1);
					blockTarget.setState(tile.getState());
					blockTarget.setFixed(true);
				}

				if (isContinue) {
					for (SimpleTile tile2 : updatedTiles) {
						tile2.setState(State.GRASS);
						tile2.setFixed(false);
						this.fixedTiles.remove(tile2);
					}
					continue;
				}

				if (count <= 1) {
					SimpleTile returnTile = this.reculsive(count + 1, aiColor);
					if (returnTile != null) {
						int sindex = returnTile.getStateIndex();
						if (sindex == 1) {
							for (SimpleTile tile2 : updatedTiles) {
								tile2.setState(State.GRASS);
								tile2.setFixed(false);
								this.fixedTiles.remove(tile2);
							}
							continue;
						} else if (sindex == 3) {
							System.out.println("최종 이기는 수 찾음2 " + tile.toSimpleString());
							sindex3Target = new SimpleTile(tile.getLocX(), tile.getLocY());
							sindex3Target.setStateIndex(1);
							sindex3Target.setState(tile.getState());
							sindex3Target.setFixed(true);
						}
					}
				}

				for (SimpleTile tile2 : updatedTiles) {
					tile2.setState(State.GRASS);
					tile2.setFixed(false);
					this.fixedTiles.remove(tile2);
				}

			}

		}

		if (!blockOppo) {
			if (blockTarget != null) {
				System.out.println("blockTarget " + blockTarget.toSimpleString());
				return blockTarget;
			}
		}

		if (sindex3Target != null) {
			System.out.println("sindex3Target " + sindex3Target.toSimpleString());
			return sindex3Target;
		}

		return null;
	}

	private Vector2 findtrack(SimpleTile start, SimpleTile center, Direction direction, boolean isFirstCall) {
		Vector2 a = new Vector2();
		a.x = -1;
		a.y = -1;
		if (start.equals(center) && !isFirstCall) {
			return a;
		}

		SimpleTile side = this.getSideTile(center, direction);
		State sideState = side.getState();
		Direction targetDir = sideState.getLinkDirection(direction.opposite());
		if (sideState == State.GRASS) {
			Vector2 pos = new Vector2();
			pos.x = side.getLocX();
			pos.y = side.getLocY();
			return pos;

		}

		return this.findtrack(start, side, targetDir, false);
	}

	public void doAI(String aiColor) { // ai color를 인자로 받는다
		this.dumpTile(); // 복사

		List<SimpleTile> tiles = this.getSideGrassTiles(); // 놓을 수 있는 타일 위치
		// this.reculsive(0, tiles);

		int n, nn; // 뒤에서 circle 승리 판별할 때 쓰일 변수
		if (aiColor == BLACK) {
			n = 1; // 검정이 1이기 때문 (State.java 참조)
			nn = 2;
		} else {
			n = 2; // 흰색이 2이기 때문
			nn = 1;
		}

		// 이기는 경우를 먼저 찾는다. (이기는 수와 지는 수 모두 있을 때 이기는 수를 두어야 하므로)
		for (SimpleTile tile : tiles) { // 놓을 수 있는 타일 위치들을 돌면서
			List<State> availableState = this.getAvailableTileState(tile); // 타일에 대해 놓을 수 있는 타일상태 리스트

			for (State stat : availableState) { // 상태를 돌면서
				tile.setState(stat); // 타일 위치에 타일 상태를 상태를 두어본다.
				tile.setFixed(true);

				List<SimpleTile> autoCompletedTiles = new ArrayList<>();
				autoCompletedTiles.add(tile);
				this.autoCompletionTiles(tile, autoCompletedTiles); // 클릭하여 추가된 타일요소(자동생성된 타일들도 포함)들 리스트 ->
																	// autoCompletedTiles (setState + setFixed 되었다)

				boolean AI_win = false;

				for (SimpleTile t : autoCompletedTiles) { // 원 모양의 승리인지 판단
					// 모든 방향에 대해서
					for (Direction dir : Direction.values()) {
						boolean isConnected = this.validCircleWinCondition(t, t, dir, true);
						if (isConnected) {
							if (t.getState().getTrack(dir) == n) { // AI가 검정이면 검정색 서클인지 판별, 흰색이면 화이트 서클인지 판별
								AI_win = true;
							}
						}
					}
				}

				int result = this.validLineWinCondition(); // 디테일한 수정 필요 (만약 새로운 행 또는 열을 생성하면서 둬서 이기는 수는 작동 x , 최외각 쪽의
															// 타일이 있을 때만 작동한다.)
				if (result >= 2 && n == 1) {
					AI_win = true;
					System.out.println("Black AI Line Win");
				} else if (result % 2 == 1 && n == 2) {
					AI_win = true;
					System.out.println("White AI Line Win");
				}

				if (AI_win) { // 이기면 원래의 판에 타일을 두고 함수 종료
					Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
					t.setState(stat);
					panel.logLine("GameManagerV3: AI 결정 완료");
					gamev2.doTileFix(t, "doAI.v1");
					if (result == 0)
						System.out.println("마무리! " + "Circle Win");
					else
						System.out.println("마무리!" + "result 값 = " + result);
					return; // 해당하는 타일을 찾으면 착수 후 함수 종료
				}

				for (SimpleTile t : autoCompletedTiles) { // 지우기
					t.setState(State.GRASS);
					t.setFixed(false);
					this.fixedTiles.remove(t);
				}
			}
		}

		// 지는 수를 나중에 찾는다.
		for (SimpleTile tile : tiles) { // 놓을 수 있는 타일 위치들을 돌면서
			List<State> availableState = this.getAvailableTileState(tile); // 타일에 대해 놓을 수 있는 타일상태 리스트

			for (State stat : availableState) { // 상태를 돌면서
				tile.setState(stat); // 타일 위치에 타일 상태를 상태를 두어본다.
				tile.setFixed(true);

				List<SimpleTile> autoCompletedTiles = new ArrayList<>();
				autoCompletedTiles.add(tile);
				this.autoCompletionTiles(tile, autoCompletedTiles); // 클릭하여 추가된 타일요소(자동생성된 타일들도 포함)들 리스트 ->
																	// autoCompletedTiles (setState + setFixed 되었다)

				boolean AI_lose = false;

				for (SimpleTile t : autoCompletedTiles) { // 원 모양의 승리인지 판단
					// 모든 방향에 대해서
					for (Direction dir : Direction.values()) {
						boolean isConnected = this.validCircleWinCondition(t, t, dir, true);
						if (isConnected) {
							if (t.getState().getTrack(dir) == nn) {
								AI_lose = true;
							}
						}
					}
				}

				int result = this.validLineWinCondition(); // 8칸 라인의 승리인지 판단
				if (result >= 2 && n == 2) {
					AI_lose = true;
					System.out.println("AI Line lose");
				}
				if (result % 2 == 1 && n == 1) {
					AI_lose = true;
					System.out.println("AI Line lose");
				}

				if (AI_lose) {
					Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
					// System.out.println("t = "+ result);
					if (stat != availableState.get(0))
						stat = availableState.get(0);
					else if (stat != availableState.get(1))
						stat = availableState.get(1); // 그 위치의 다른 타일 stat 세팅
					t.setState(stat);

					panel.logLine("GameManagerV3: AI 결정 완료");
					gamev2.doTileFix(t, "doAI.v1");
					System.out.println("수비!");
					for (SimpleTile temp : autoCompletedTiles) { // 지우기
						temp.setState(State.GRASS);
						temp.setFixed(false);
						this.fixedTiles.remove(temp);
					}
					return; // 해당하는 타일을 찾으면 착수 후 함수 종료
				}

				for (SimpleTile t : autoCompletedTiles) { // 지우기
					t.setState(State.GRASS);
					t.setFixed(false);
					this.fixedTiles.remove(t);
				}
			}
		}

		// 승리할수잇는 경우를 찾는다(3수 앞 보기)
		for (SimpleTile tile : tiles) { // 놓을 수 있는 타일 위치들을 돌면서
			List<State> availableState = this.getAvailableTileState(tile); // 타일에 대해 놓을 수 있는 타일상태 리스트

			for (State stat : availableState) { // 상태를 돌면서
				tile.setState(stat); // 타일 위치에 타일 상태를 상태를 두어본다.
				tile.setFixed(true);

				List<SimpleTile> autoCompletedTiles = new ArrayList<>();
				autoCompletedTiles.add(tile);
				this.autoCompletionTiles(tile, autoCompletedTiles); // 클릭하여 추가된 타일요소(자동생성된 타일들도 포함)들 리스트 ->
																	// autoCompletedTiles (setState + setFixed 되었다)
				int bcnt = 0;
				int wcnt = 0;
				for (SimpleTile t : autoCompletedTiles) {
					Vector2 wpos = new Vector2(); // 검정색 트랙 끝부분
					wpos.x = -1;
					wpos.y = -1; // 흰색의 트랙 사이거리
					Vector2 bpos = new Vector2(); // 흰색 트랙 끝부분
					bpos.x = -1;
					bpos.y = -1; // 초기화
					int brange = 0;
					int wrange = 0; // 검정색의 트랙 사이 거리
					for (Direction dir : Direction.values()) {
						Vector2 pos = findtrack(t, t, dir, true);
						if (t.getState().getTrack(dir) == 1) {
							if (bpos.x == -1) // 트랙끝을 맨처음 찾앗다면 pos 그대로 넣음
								bpos = pos;
							else { // 두번째 찾을 것이라면 트랙 끝과 끝의 거리를 잼
								bpos.x -= pos.x;
								bpos.y -= pos.y;
								bpos.x = Math.abs(bpos.x);
								bpos.y = Math.abs(bpos.y);
								brange = bpos.x + bpos.y;
								if (brange == 1) { // 끝과 끝의 차이가 1만 난다면 이길수있다
									bcnt++;
									System.out.println("black이" + pos.x + "," + pos.y + "에 두면 이깁니다");
								}
							}
						} else { // 흰색이 이길수 있는 경우도 생각한다
							if (wpos.x == -1)
								wpos = pos;
							else {
								wpos.x -= pos.x;
								wpos.y -= pos.y;
								wpos.x = Math.abs(wpos.x);
								wpos.y = Math.abs(wpos.y);
								wrange = wpos.x + wpos.y;
								if (wrange == 1) {
									wcnt++;
								}

							}
						}

					}
				}
				int Line_attack = validLineWinCondition2();

				if (aiColor == BLACK) {
					if (Line_attack >= 2) {
						System.out.println("Black AI Line attack");
						Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
						t.setState(stat);
						panel.logLine("GameManagerV3: AI 결정 완료");
						gamev2.doTileFix(t, "doAI.v1");
						System.out.println("black can win");
						for (SimpleTile t2 : autoCompletedTiles) { // 지우기
							t2.setState(State.GRASS);
							t2.setFixed(false);
							this.fixedTiles.remove(t2);
						}
						return;
					}
					if (wcnt >= 1) { // 해당 타일에 두면 흰색이 다음턴에 이길수있으므로 패스해야함
						bcnt = 0;
					}
					if (bcnt >= 1) { // 이길 수 있는 타일을 생성한다
						Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
						t.setState(stat);
						panel.logLine("GameManagerV3: AI 결정 완료");
						gamev2.doTileFix(t, "doAI.v1");
						System.out.println("black can win");
						for (SimpleTile t2 : autoCompletedTiles) { // 지우기
							t2.setState(State.GRASS);
							t2.setFixed(false);
							this.fixedTiles.remove(t2);
						}
						return; // 해당하는 타일을 찾으면 착수 후 함수 종료
					}
				} else {
					if (bcnt >= 1) { // 해당 타일에 두면 흰색이 다음턴에 이길수있으므로 패스해야함
						wcnt = 0;
					}
					if (wcnt >= 1) { // 이길 수 있는 타일을 생성한다
						Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
						t.setState(stat);
						panel.logLine("GameManagerV3: AI 결정 완료");
						gamev2.doTileFix(t, "doAI.v1");
						System.out.println("AI can win");
						for (SimpleTile t2 : autoCompletedTiles) { // 지우기
							t2.setState(State.GRASS);
							t2.setFixed(false);
							this.fixedTiles.remove(t2);
						}
						return; // 해당하는 타일을 찾으면 착수 후 함수 종료
					}
				}

				for (SimpleTile t : autoCompletedTiles) { // 지우기
					t.setState(State.GRASS);
					t.setFixed(false);
					this.fixedTiles.remove(t);
				}
			}
		}
		// 미리 수비하는 경우를 찾는다(3수 앞 보기)
		for (SimpleTile tile : tiles) { // 놓을 수 있는 타일 위치들을 돌면서
			List<State> availableState = this.getAvailableTileState(tile); // 타일에 대해 놓을 수 있는 타일상태 리스트

			for (State stat : availableState) { // 상태를 돌면서
				tile.setState(stat); // 타일 위치에 타일 상태를 상태를 두어본다.
				tile.setFixed(true);

				List<SimpleTile> autoCompletedTiles = new ArrayList<>();
				autoCompletedTiles.add(tile);
				this.autoCompletionTiles(tile, autoCompletedTiles); // 클릭하여 추가된 타일요소(자동생성된 타일들도 포함)들 리스트 ->
																	// autoCompletedTiles (setState + setFixed 되었다)
				int bcnt = 0;
				int wcnt = 0;
				for (SimpleTile t : autoCompletedTiles) {
					Vector2 wpos = new Vector2(); // 검정색 트랙 끝부분
					wpos.x = -1;
					wpos.y = -1; // 흰색의 트랙 사이거리
					Vector2 bpos = new Vector2(); // 흰색 트랙 끝부분
					bpos.x = -1;
					bpos.y = -1; // 초기화
					int brange = 0;
					int wrange = 0; // 검정색의 트랙 사이 거리
					for (Direction dir : Direction.values()) {
						Vector2 pos = findtrack(t, t, dir, true);
						if (t.getState().getTrack(dir) == 1) {
							if (bpos.x == -1) // 트랙끝을 맨처음 찾앗다면 pos 그대로 넣음
								bpos = pos;
							else { // 두번째 찾을 것이라면 트랙 끝과 끝의 거리를 잼
								bpos.x -= pos.x;
								bpos.y -= pos.y;
								bpos.x = Math.abs(bpos.x);
								bpos.y = Math.abs(bpos.y);
								brange = bpos.x + bpos.y;
								if (brange == 1) { // 끝과 끝의 차이가 1만 난다면 이길수있다
									bcnt++;
									System.out.println("black이" + pos.x + "," + pos.y + "에 두면 이깁니다");
								}
							}
						} else { // 흰색이 이길수 있는 경우도 생각한다
							if (wpos.x == -1)
								wpos = pos;
							else {
								wpos.x -= pos.x;
								wpos.y -= pos.y;
								wpos.x = Math.abs(wpos.x);
								wpos.y = Math.abs(wpos.y);
								wrange = wpos.x + wpos.y;
								if (wrange == 1) {
									wcnt++;
								}

							}
						}

					}
				}
				if (wcnt >= 1 && aiColor == BLACK) { // 이길 수 있는 가능성이 없는 경우 미리 질 수 있는 가능성의 수를 방해한다.
					Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
					if (stat != availableState.get(0))
						stat = availableState.get(0);
					else if (stat != availableState.get(1))
						stat = availableState.get(1); // 그 위치의 다른 타일 stat 세팅
					t.setState(stat);
					panel.logLine("GameManagerV3: AI 결정 완료");
					gamev2.doTileFix(t, "doAI.v1");
					System.out.println("미리 수비한다");
					for (SimpleTile t2 : autoCompletedTiles) { // 지우기
						t2.setState(State.GRASS);
						t2.setFixed(false);
						this.fixedTiles.remove(t2);
					}
					return; // 해당하는 타일을 찾으면 착수 후 함수 종료
				}
				if (bcnt >= 1 && aiColor == WHITE) { // 이길 수 있는 가능성이 없는 경우 미리 질 수 있는 가능성의 수를 방해한다.
					Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
					if (stat != availableState.get(0))
						stat = availableState.get(0);
					else if (stat != availableState.get(1))
						stat = availableState.get(1); // 그 위치의 다른 타일 stat 세팅
					t.setState(stat);
					panel.logLine("GameManagerV3: AI 결정 완료");
					gamev2.doTileFix(t, "doAI.v1");
					System.out.println("미리 수비한다");
					for (SimpleTile t2 : autoCompletedTiles) { // 지우기
						t2.setState(State.GRASS);
						t2.setFixed(false);
						this.fixedTiles.remove(t2);
					}
					return; // 해당하는 타일을 찾으면 착수 후 함수 종료
				}
				for (SimpleTile t : autoCompletedTiles) { // 지우기
					t.setState(State.GRASS);
					t.setFixed(false);
					this.fixedTiles.remove(t);
				}
			}
		}
		// 1. 끝내기 수가 존재하는가 (이기는 수 우선, 이기는 수 없는 경우 지는 수 방어)
		// 2. circle로 공격할 수 있는 수를 찾늩다. (수가 없는 경우 상대가 공격할 만한 곳을 미리 방어한다.)

		// 3. 위의 해당하는 경우가 없는 경우 랜덤 착수 -> 이를 줄여나가는 것이 목표
		Random r = new Random();
		int index = r.nextInt(tiles.size());
		SimpleTile tile = tiles.get(index);
		List<State> states = this.getAvailableTileState(tile);
		index = r.nextInt(states.size());

		State s = states.get(index);
		tile.setState(s);
		Tile t = gamev2.getTile(tile.getLocX(), tile.getLocY());
		t.setState(s);

		panel.logLine("GameManagerV3: AI 결정 완료");
		gamev2.doTileFix(t, "doAI.v1");

		System.out.println("랜덤입니다!");
		return;
	}

	private boolean isAvailableLocation(int x, int y) {
		return !(x == -1 || y == -1 || x == 64 || y == 64);
	}

	private SimpleTile getTile(int x, int y) {
		if (!isAvailableLocation(x, y)) {
			return this.outsideGrassTile;
		} else {
			return this.dumpedTile[y][x];
		}
	}

	// center: 본체 타일, side: 옆 타일
	private boolean isAvailableConnect(int side, int center) {
		if (side == 0) {
			return true;
		}
		return side == center;
	}

	public Vector2 getSideLocation(SimpleTile center, Direction direction) {
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

	public SimpleTile getSideTile(SimpleTile center, Direction direction) {
		Vector2 targetLoc = this.getSideLocation(center, direction);
		return this.getTile(targetLoc.x, targetLoc.y);
	}

	public List<SimpleTile> getSideGrassTiles() {
		List<SimpleTile> list = new ArrayList<>();
		for (SimpleTile tile : this.fixedTiles) {
			for (Direction dir : Direction.values()) {
				SimpleTile side = this.getSideTile(tile, dir);
				if (side.getState() == State.GRASS) {
					if (!list.contains(side)) {
						list.add(side);
					}
				}
			}
		}
		return list;
	}

	private List<State> getAvailableTileState(SimpleTile center) {
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
				states.add(state);
			}
		}

		return states;
	}

	private boolean validCircleWinCondition(SimpleTile start, SimpleTile center, Direction direction,
			boolean isFirstCall) {
		if (start.equals(center) && !isFirstCall) {
			return true;
		}

		SimpleTile side = this.getSideTile(center, direction);
		State sideState = side.getState();
		Direction targetDir = sideState.getLinkDirection(direction.opposite());
		if (sideState == State.GRASS) {
			return false;
		}

		return this.validCircleWinCondition(start, side, targetDir, false);
	}

	private int validLineWinCondition() {
		Vector2 min = new Vector2(32, 32);
		Vector2 max = new Vector2(32, 32);
		for (SimpleTile tile : this.fixedTiles) {
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

		List<SimpleTile> topTiles = new ArrayList<>();
		List<SimpleTile> leftTiles = new ArrayList<>();

		for (int x = 0; x < 64; x++) {
			SimpleTile tile = this.getTile(x, min.y);
			if (tile.getState() != State.GRASS) {
				topTiles.add(tile);
			}
		}

		for (int y = 0; y < 64; y++) {
			SimpleTile tile = this.getTile(min.x, y);
			if (tile.getState() != State.GRASS) {
				leftTiles.add(tile);
			}
		}

		boolean isBlackWin = false;
		boolean isWhiteWin = false;
		for (SimpleTile tile : topTiles) {
			State state = tile.getState();
			Direction dir = state.getLinkDirection(Direction.TOP);
			SimpleTile endTile = this.getLineEndTile(tile, dir);
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

		for (SimpleTile tile : leftTiles) {
			State state = tile.getState();
			Direction dir = state.getLinkDirection(Direction.LEFT);
			SimpleTile endTile = this.getLineEndTile(tile, dir);
			if (endTile.getState().getTrack(Direction.RIGHT) == state.getTrack(Direction.LEFT)) {
				if (endTile.getLocX() == max.x) {
					int startX = tile.getLocX();
					int endX = endTile.getLocX();
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

	private int validLineWinCondition2() {
		Vector2 min = new Vector2(32, 32);
		Vector2 max = new Vector2(32, 32);
		for (SimpleTile tile : this.fixedTiles) {
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

		List<SimpleTile> topTiles = new ArrayList<>();
		List<SimpleTile> leftTiles = new ArrayList<>();

		for (int x = 0; x < 64; x++) {
			SimpleTile tile = this.getTile(x, min.y);
			if (tile.getState() != State.GRASS) {
				topTiles.add(tile);
			}
		}

		for (int y = 0; y < 64; y++) {
			SimpleTile tile = this.getTile(min.x, y);
			if (tile.getState() != State.GRASS) {
				leftTiles.add(tile);
			}
		}

		boolean isBlackWin = false;
		boolean isWhiteWin = false;
		for (SimpleTile tile : topTiles) {
			State state = tile.getState();
			Direction dir = state.getLinkDirection(Direction.TOP);
			SimpleTile endTile = this.getLineEndTile(tile, dir);
			if (endTile.getState().getTrack(Direction.BOTTOM) == state.getTrack(Direction.TOP)) {
				if (endTile.getLocY() == max.y) {
					int startY = tile.getLocY();
					int endY = endTile.getLocY();
					if (Math.abs(startY - endY) >= 5) {
						if (state.getTrack(Direction.TOP) == 1) {
							isBlackWin = true;
						} else {
							isWhiteWin = true;
						}
					}
				}
			}
		}

		for (SimpleTile tile : leftTiles) {
			State state = tile.getState();
			Direction dir = state.getLinkDirection(Direction.LEFT);
			SimpleTile endTile = this.getLineEndTile(tile, dir);
			if (endTile.getState().getTrack(Direction.RIGHT) == state.getTrack(Direction.LEFT)) {
				if (endTile.getLocX() == max.x) {
					int startX = tile.getLocX();
					int endX = endTile.getLocX();
					if (Math.abs(startX - endX) >= 5) {
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

	public SimpleTile getLineEndTile(SimpleTile startTile, Direction direction) {
		// System.out.println("start " + startTile.toSimpleString());
		// System.out.println("dir " + direction.toString());
		SimpleTile moveTile = startTile;
		Direction moveDirection = direction;

		SimpleTile endTile = startTile;
		while (true) {
			SimpleTile sideTile = this.getSideTile(moveTile, moveDirection);
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

	private void autoCompletionTiles(SimpleTile center, List<SimpleTile> updatedTiles) {
		// 주변 타일 자동완성
		for (Direction dir : Direction.values()) {
			SimpleTile tile = this.getSideTile(center, dir);
			if (tile.getState() != State.GRASS) {
				continue;
			}

			if (!tile.isFixed()) {
				// System.out.println(dir.toString());
				// System.out.println(tile.toSimpleString());
				List<State> states = this.getAvailableTileState(tile);
				if (states.size() == 0) {
					// System.out.println("자동완성: 가능한 타일 0개" + tile.toString());
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

	public void doTileFix(SimpleTile tile) {
		tile.setFixed(true);
		this.fixedTiles.add(tile);

		// 타일 자동완성
		List<SimpleTile> autoCompletedTiles = new ArrayList<>();
		autoCompletedTiles.add(tile);
		this.autoCompletionTiles(tile, autoCompletedTiles); // 클릭하여 추가된 타일요소(자동생성된 타일들도 포함)들을 추가, 이후 circle

		// System.out.println("자동완성 종료");

		boolean isBlackWin = false;
		boolean isWhiteWin = false;

		// 자동완성된 타일에 대해서 승리판별 작업
		// 원 승리조건
		for (SimpleTile t : autoCompletedTiles) {
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
	}
}

/*
 * @SuppressWarnings({ "unchecked", "unused" }) private List<State>
 * getAvailableTileState2(SimpleTile center) { List<State> states = new
 * ArrayList<>(); Pair<Direction, State>[] sides = new Pair[4]; int i = 0; for
 * (Direction dir : Direction.values()) { sides[i] = new Pair<Direction,
 * State>(dir, this.getSideTile(center, dir).getState()); i++; }
 * 
 * for (State state : State.values()) { if (state == State.GRASS) { continue; }
 * // grass일때는 넘어감
 * 
 * boolean available = true; for (Pair<Direction, State> pair : sides) {
 * Direction dir = pair.getKey(); if
 * (!this.isAvailableConnect(pair.getValue().getTrack(dir.opposite()),
 * state.getTrack(dir))) { available = false; } } if (available) {
 * states.add(state); } } return states; }
 */