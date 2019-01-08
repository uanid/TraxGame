public enum State {
	GRASS(), IMAGE1(1, 2, 1, 2), IMAGE2(1, 2, 2, 1),

	IMAGE3(2, 1, 2, 1), IMAGE4(2, 1, 1, 2),

	IMAGE5(2, 2, 1, 1), IMAGE6(1, 1, 2, 2);

	// 풀: 0, 검정: 1, 하양: 2
	private int top;
	private int bottom;
	private int left;
	private int right;

	private State() {
		this(0, 0, 0, 0);
	}

	private State(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public Direction getLinkDirection(Direction dir) {
		for (Direction d : Direction.values()) {
			if (d == dir) {
				continue;
			}
			if (this.getTrack(d) == this.getTrack(dir)) {
				return d;
			}
		}
		return null;
	}

	public int getTrack(Direction dir) {
		switch (dir) {
		case TOP:
			return top;
		case BOTTOM:
			return bottom;
		case LEFT:
			return left;
		case RIGHT:
			return right;
		}
		return -1;
	}
};