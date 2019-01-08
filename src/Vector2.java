public class Vector2 {
	public int x;
	public int y;

	public Vector2() {
		this(0, 0);
	}

	public Vector2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Vector2) {
			Vector2 v = (Vector2) obj;
			return x == v.x && y == v.y;
		}
		return false;
	}

	public String toString() {
		return "Vector2 (" + x + ", " + y + ")";
	}
}