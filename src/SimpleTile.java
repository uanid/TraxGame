
public class SimpleTile {
	private int x;
	private int y;
	private State state;
	private int stateIndex;
	private boolean isFixed;

	public SimpleTile(int x, int y) {
		this(x, y, false, State.GRASS);
	}

	public SimpleTile(int x, int y, boolean isFixed, State state) {
		this.x = x;
		this.y = y;
		this.isFixed = isFixed;
		this.setStateIndex(0);
		this.state = state;
	}

	public SimpleTile clone() {
		SimpleTile t = new SimpleTile(this.getLocX(), this.getLocY());
		t.setState(this.getState());
		t.setFixed(this.isFixed());
		t.setStateIndex(this.getStateIndex());
		return t;
	}
	
	public int hashCode() {
		return x * 64 * 64 + y * 64 + state.ordinal();
	}

	public boolean equals(Object t) {

		if (t instanceof SimpleTile) {
			SimpleTile t1 = this;
			SimpleTile t2 = (SimpleTile) t;
			if (t1.getLocX() == t2.getLocX()) {
				if (t1.getLocY() == t2.getLocY()) {
					if (t1.getState() == t2.getState()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getLocX() {
		return x;
	}

	public int getLocY() {
		return y;
	}

	public boolean isFixed() {
		return this.isFixed;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Vector2 getLoc() {
		return new Vector2(x, y);
	}

	public State getState() {
		return state;
	}

	public String toSimpleString() {
		return String.format("Tile (%d, %d) %s", x, y, state.name());
	}

	public String toString() {
		return String.format("Tile: x=%d, y=%d, status=%s", x, y, state.name());
	}

	public void setFixed(boolean isFixed) {
		this.isFixed = isFixed;
	}

	public int getStateIndex() {
		return stateIndex;
	}

	public void setStateIndex(int stateIndex) {
		this.stateIndex = stateIndex;
	}
}
