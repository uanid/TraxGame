
public enum Direction {
	TOP, BOTTOM, LEFT, RIGHT;

	public Direction opposite() {
		switch (this) {
		case BOTTOM:
			return TOP;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		case TOP:
			return BOTTOM;
		}
		return null;
	}
}
