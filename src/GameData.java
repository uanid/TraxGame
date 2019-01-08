import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class GameData {

	public static GameData data;

	// 버튼인 동시에 타일
	public Tile[][] tiles;
	public Tile outsideGrassTile;

	// 이미지 기능
	private ImageIcon grass;
	private ImageIcon[] fixedTileImages;
	private ImageIcon[] tempTileImages;
	private ImageIcon[] blueTileImages;

	public GameData() {
		data = this;

		grass = new ImageIcon("png/0.png");
		fixedTileImages = new ImageIcon[6];
		tempTileImages = new ImageIcon[6];
		blueTileImages = new ImageIcon[6];
		for (int i = 0; i < tempTileImages.length; i++) {
			fixedTileImages[i] = new ImageIcon("png/" + (i + 1) + ".png");
		}
		for (int i = 0; i < tempTileImages.length; i++) {
			tempTileImages[i] = new ImageIcon("png/" + (i + 1) + "_tmp.png");
		}
		for (int i = 0; i < blueTileImages.length; i++) {
			blueTileImages[i] = new ImageIcon("png/" + (i + 1) + "_blue.png");
		}

		this.outsideGrassTile = new Tile(-10, -10);
		this.tiles = new Tile[64][64];
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				tiles[j][i] = new Tile(i, j);
			}
		}
	}

	public ImageIcon getGrass() {
		return grass;
	}

	public ImageIcon[] getFixedTiles() {
		return fixedTileImages;
	}

	public ImageIcon[] getTempTiles() {
		return tempTileImages;
	}

	public ImageIcon[] getBlueTiles() {
		return blueTileImages;
	}
}
