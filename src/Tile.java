import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class Tile extends JButton {

	private int x;
	private int y;
	private State state;
	private int stateIndex;
	private boolean isFixed;
	private boolean isBlue;

	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
		this.isFixed = false;
		this.isBlue = false;
		this.setStateIndex(0);
		this.state = State.GRASS;

		this.setBorder(null);
		this.reDraw();
	}

	public ImageIcon getTileImage() {
		if (this.state == State.GRASS) {
			return GameData.data.getGrass();
		} else {
			int ordinal = this.state.ordinal();
			if (isBlue) {
				return GameData.data.getBlueTiles()[ordinal - 1];
			} else {
				if (isFixed) {
					return GameData.data.getFixedTiles()[ordinal - 1];
				} else {
					return GameData.data.getTempTiles()[ordinal - 1];
				}
			}
		}
	}

	public boolean isFixed() {
		return this.isFixed;
	}

	public void setState(State state) {
		this.state = state;
		this.reDraw();
	}

	public void reDraw() {
		ImageIcon image = this.getTileImage();
		this.setIcon(image);
	}

	public String toSimpleString() {
		return String.format("Tile (%d, %d) %s", x, y, state.name());
	}

	public String toString() {
		return String.format("Tile: x=%d, y=%d, status=%s", x, y, state.name());
	}

	public Vector2 getLoc() {
		return new Vector2(x, y);
	}

	public int getLocX() {
		return x;
	}

	public int getLocY() {
		return y;
	}

	public State getState() {
		return state;
	}
	
	public boolean isBlue() {
		return this.isBlue;
	}
	
	public void setBlue(boolean isBlue) {
		this.isBlue = isBlue;
		this.reDraw();
	}

	public void setFixed(boolean isFixed) {
		this.isFixed = isFixed;
		this.reDraw();
	}

	public int getStateIndex() {
		return stateIndex;
	}

	public void setStateIndex(int stateIndex) {
		this.stateIndex = stateIndex;
	}
}
