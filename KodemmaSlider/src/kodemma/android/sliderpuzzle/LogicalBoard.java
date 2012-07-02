package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.util.*;
enum Direction {UP, DOWN, LEFT, RIGHT, NON };

class LogicalTile {
	int serial;
	Point lp = new Point(); // logical position
	void setPoint(int x, int y) {
		this.lp.x = x;
		this.lp.y = y;
	}
}
public class LogicalBoard {
	private int row;
	private int column;
	private int totalDistance;
	LogicalTile[][] tiles;
	LogicalTile hole;
	private int oldMove;
	private LogicalTile tmp;
	private LogicalTile newMove;
	private ArrayList<Point> recode;		// ��
	private int holeNumber;//shima
//	int maxDistance;//shima
//	int shuffleLimit;//shima
	
    Direction direction;
	
	LogicalBoard(int r, int c){
		row = r;
		column = c;
		totalDistance = 0;
		recode = new ArrayList<Point>();
//		maxDistance = (int)(r * c * TWICE_VALUE);// shima
//		shuffleLimit = (int)(maxDistance * LIMIT_VALUE);//shima

		holeNumber = (int) (Math.random() * r * c + 1);	// �u�����N������
		
		// �z�u�̏���
		tiles = new LogicalTile[r][c];
		int serial = 0;
		for (int i = 0; i < c; i++) {
			for (int j = 0; j < r; j++) {
				tiles[j][i] = new LogicalTile();
				
				tiles[j][i].setPoint(j,i);
				tiles[j][i].serial = ++serial; // (0,0)�ɂ́u�P�v�̃^�C�������̂�++ 

				if (serial == holeNumber) {		// �u�����N��R�t��
					hole = tiles[j][i];
				}
			}
		}
	}
	
	protected int shuffle() {
		final float TWICE_VALUE = 2.0f;
		final float LIMIT_VALUE = 2.5f;
		
//		ArrayList<Integer> tile;
		ArrayList<LogicalTile> tile;
		oldMove = 0;
		 
		// �V���b�t���J�n
		for (int i = 0; i < (int)(row * column * TWICE_VALUE * LIMIT_VALUE); i++) {

			System.out.println("\n"+ (i) + "��]");

			// ������ǉ�
			recode.add(new Point(hole.lp.x, hole.lp.y));
			System.out.println("X "+ hole.lp.x + ",  Y" + hole.lp.y);

//			tile = canMoveTileSelect(holeData.point);
			tile = canMoveTileSelect();
			/*
			 * ���canMoveTileSelect���\�b�h����́A�O�񓮂������^�C���i���o�[������\��������̂�
			 * ����for���ŁA�O��^�C����tile�z�񂩂�폜�B
			 */
			for (int j = 0; j < tile.size(); j++) {
				if (oldMove == tile.get(j).serial) {
					tile.remove(j);
					break;
				}
			}
			System.out.println(tile.size());

			// �i��ꂽ���̒�����A�ǂ̃^�C���𓮂����������_���Ō���
			newMove = tile.get((int) (Math.random() * tile.size()));

			oldMove = newMove.serial;
			
			System.out.println("Move.serial = " + newMove.serial);
			System.out.println(getDirection(newMove));

				// �f�o�b�O�p
				debug(i,tile,oldMove);
			
			// �^�C��������
//			tileChange(newMove);
//			slide(newMove);
			System.out.println(slide(newMove));
			
			if (totalDistance >= (int)(row * column * TWICE_VALUE)) {
				break;
			}
		}
		return recode.size();
	}


	// �u�����N�Ɨאڃ^�C������ւ���āA���U���������Z
//	private void tileChange(LogicalTile newMove) {
//		for (int i = 0; i < column; i++) {
//			for (int j = 0; j < row; j++) {
//				if (logicalTiles[i][j].serial == newMove.serial) {
//					totalDistance -= getDistance(newMove, i, j);	// �O�񋗗�
//
//					logicalTiles[i][j].serial = holeData.serial;
//					logicalTiles[holeData.point.x][holeData.point.y].serial = newMove;
//
//					totalDistance += getDistance(newMove, holeData.point.x, holeData.point.y);	// ���񋗗�
//
////					logicalTile[holeData.point.x][holeData.point.y].setPoint(holeData.point.x, holeData.point.y);
//					holeData.setPoint(i, j);
//					return;
//				}
//			}
//		}
//	}

	// ���������Ƃ��\�ȗאڃ^�C���̔ԍ���z��ŕԂ����\�b�h
	protected ArrayList<LogicalTile> canMoveTileSelect() {

		ArrayList<LogicalTile> canMoveTiles = new ArrayList<LogicalTile>();

		if (0 <= hole.lp.y && hole.lp.y < column) {	// �㉺�ɂ͂ݏo���Ȃ�������
			if (hole.lp.x - 1 >= 0) {	// ������
				canMoveTiles.add(tiles[hole.lp.x - 1][hole.lp.y]);
			}
			if (hole.lp.x + 1 < row) {	// �E����
				canMoveTiles.add(tiles[hole.lp.x + 1][hole.lp.y]);
			}
		}
		if (0 <= hole.lp.x && hole.lp.x < row) {	// ���E�ɂ͂ݏo���Ȃ�������
			if (hole.lp.y - 1 >= 0) {	// �㌩��
				canMoveTiles.add(tiles[hole.lp.x][hole.lp.y - 1]);
			}
			if (hole.lp.y + 1 < column) {	// ������
				canMoveTiles.add(tiles[hole.lp.x][hole.lp.y + 1]);
			}
		}
		return canMoveTiles;
	}
//	protected ArrayList<LogicalTile> canMoveTileSelect() {
//
//		ArrayList<LogicalTile> canMoveTiles = new ArrayList<LogicalTile>();
//
//		if (0 <= holeData.point.y && holeData.point.y < column) {	// �㉺�ɂ͂ݏo���Ȃ�������
//			if (holeData.point.x - 1 >= 0) {	// ������
//				canMoveTiles.add(logicalTiles[holeData.point.x - 1][holeData.point.y]);
//			}
//			if (holeData.point.x + 1 < row) {	// �E����
//				canMoveTiles.add(logicalTiles[holeData.point.x + 1][holeData.point.y]);
//			}
//		}
//		if (0 <= holeData.point.x && holeData.point.x < row) {	// ���E�ɂ͂ݏo���Ȃ�������
//			if (holeData.point.y - 1 >= 0) {	// �㌩��
//				canMoveTiles.add(logicalTiles[holeData.point.x][holeData.point.y - 1]);
//			}
//			if (holeData.point.y + 1 < column) {	// ������
//				canMoveTiles.add(logicalTiles[holeData.point.x][holeData.point.y + 1]);
//			}
//		}
//		return canMoveTiles;
//	}

	// ���U���������߂郁�\�b�h
//	public int getDistance(int nm, int x, int y) {
//		int distnc = Math.abs(nm / column - (x)) + Math.abs(nm % column - (y+1));
//		return distnc;
//	}
	public int getDistance(LogicalTile nm) {
		int distnc = (Math.abs((nm.serial-1) / row - nm.lp.y) + (Math.abs((nm.serial-1) % row - nm.lp.x)));
		return distnc;
	}
	
	
	// �V���\�b�h�@���Q�b�g
	protected Direction getDirection(LogicalTile lt) {
		direction = Direction.NON;
		if (hole.lp.y == lt.lp.y) {	// �㉺�ɕ��񂾗�̒�����
			if (hole.lp.x > lt.lp.x) {	// ������
				direction = Direction.RIGHT;
			}
			if (hole.lp.x < lt.lp.x) {	// �E����
				direction = Direction.LEFT;
			}
		}
		if (hole.lp.x == lt.lp.x) {	// �㉺�ɕ��񂾗�̒�����
			if (hole.lp.y > lt.lp.y) {	// �㌩��
				direction = Direction.DOWN;
			}
			if (hole.lp.y < lt.lp.y) {	// ������
				direction = Direction.UP;
			}
		}
		return direction;
	}
	
	// �V���\�b�h�@��������^�C�����X�g
	protected List<LogicalTile> getMovables(LogicalTile lt) {
		List<LogicalTile> ltList = null;

		if (hole.lp.y == lt.lp.y) {	// ���E�ɕ��񂾗�̒�����
			int i = hole.lp.x < lt.lp.x ? 1: -1; // �E�������ŁAint i�̒l������
			for(int x = hole.lp.x; lt.lp.x != x; ){ // �ǂ���̕��ł��P�����Ԃɏ���
				if(ltList == null)ltList = new ArrayList<LogicalTile>();
				ltList.add(tiles[x += i][hole.lp.y]);
			}
		}
		if (hole.lp.x == lt.lp.x) {	// �㉺�ɕ��񂾗�̒�����
			int i = hole.lp.y < lt.lp.y ? 1: -1;
			for(int y = hole.lp.y; lt.lp.y != y; ){
				if(ltList == null)ltList = new ArrayList<LogicalTile>();
				ltList.add(tiles[hole.lp.x][y += i]);
			}
		}
		return ltList;
	}
	
	// �V���\�b�h�@�אڃ`�F�b�N
	protected boolean slide(LogicalTile lt) {
		if ((hole.lp.x == lt.lp.x && (Math.abs(hole.lp.y - lt.lp.y) == 1))
		  ||(hole.lp.y == lt.lp.y && (Math.abs(hole.lp.x - lt.lp.x) == 1))){	// �אڂ��Ă��邩�H
			
//			LogicalTile tmp;
			
			totalDistance -= getDistance(lt);	// �O�񋗗�
			System.out.println("�O��@" + getDistance(lt));

//			oldMove = lt;			
//			lt = holeData;
//			holeData = oldMove;
//
//			oldMove.point = lt.point;
//			lt.point = holeData.point;
//			holeData.point = oldMove.point;

			tmp = tiles[lt.lp.x][lt.lp.y];
			tiles[lt.lp.x][lt.lp.y] = tiles[hole.lp.x][hole.lp.y];	
			tiles[hole.lp.x][hole.lp.y] = tmp;
			
			tmp = new LogicalTile();
			
			tmp.lp = hole.lp;
			tiles[hole.lp.x][hole.lp.y].lp = tiles[lt.lp.x][lt.lp.y].lp;
			tiles[lt.lp.x][lt.lp.y].lp = tmp.lp;
//			logicalTiles[lt.point.x][lt.point.y].point = logicalTiles[holeData.point.x][holeData.point.y].point;
//			logicalTiles[holeData.point.x][holeData.point.y].point = tmp.point;
//			holeData.serial = logicalTiles[holeData.point.x][holeData.point.y].serial;
	
			totalDistance += getDistance(lt);	// ���񋗗�
			System.out.println("����@" + getDistance(lt));

			return true;
		}
		return false;
	}
//	protected boolean slide(LogicalTile lt) {
//		if (holeData.point.x == lt.point.x) {	// ���E�ɕ��񂾗�̒�����
//			if (Math.abs(holeData.point.y - lt.point.y) == 1) {	// �אڂ��Ă��邩�H
//				return true;
//			}
//		}
//		if (holeData.point.y == lt.point.y) {	// �㉺�ɕ��񂾗�̒�����
//			if (Math.abs(holeData.point.x - lt.point.x) == 1) {	// �אڂ��Ă��邩�H
//				return true;
//			}
//		}
//		return false;
//	}
	
//	class ShuffleResalts {
//		ArrayList<Point> recode = new ArrayList<Point>();
//		int[][] logicalTiles;
//		ShuffleResalts(int r, int c) {
//			logicalTiles = new int[r][c];
//		}
//	}

	// �f�o�b�O�p�O���b�h�\�����\�b�h
	private void debug(int i, ArrayList<LogicalTile> tile, int nm) {
		if(i==0){
			Log.i("holeNumber", Integer.toString(hole.serial));
		}
		for(int j = 0; j <tile.size();j++){
			System.out.println("canMoveTile "+ tile.get(j).serial);
		}
		for (int k = 0; k < column; k++) {
			for (int j = 0; j < row; j++) {
				if(tiles[j][k].serial < 10)System.out.print(" ");
				System.out.print(" "+tiles[j][k].serial);
			}
			System.out.print("\n");	
		}
		Log.i("moved", Integer.toString(nm));
		Log.i("totalDistance", Integer.toString(totalDistance));		
	}
}