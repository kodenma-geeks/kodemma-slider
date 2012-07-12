package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.util.*;
//enum Direction {UP, DOWN, LEFT, RIGHT, NON }; // Utilsへ移動 by shima

class LogicalTile {
	int serial;				// 実番号より１少ない連番（０スタート）
	Point lp = new Point(); // logical position
	void setPoint(int x, int y) {
		this.lp.x = x;
		this.lp.y = y;
	}
}
public class LogicalBoard {
	private int row;
	private int column;
	int totalDistance;
	LogicalTile[][] tiles;
	LogicalTile hole;
	private LogicalTile oldMove;			// 前回動かしたタイル
	private LogicalTile newMove;			// 今回動かすタイル
	private ArrayList<Point> recode;		// 棋譜
	private int holeNumber;//shima

	
    Direction direction;
	
	LogicalBoard(int r, int c){
		hole = new LogicalTile();
		oldMove = new LogicalTile();
		newMove = new LogicalTile();
		row = r;
		column = c;
		totalDistance = 0;
		recode = new ArrayList<Point>();

		holeNumber = (int) (Math.random() * r * c);	// ブランクを決定
		
		// 配置の初期化
		tiles = new LogicalTile[c][r];
		int serial = 0;
		for (int i = 0; i < r; i++) {
			for (int j = 0; j < c; j++, serial++) {	// 注：X座標が内側のネスト
				tiles[j][i] = new LogicalTile();
				
				tiles[j][i].setPoint(j,i);
				tiles[j][i].serial = serial; 	// (0,0)には「０」のタイルが入る

				if (serial == holeNumber) {
					hole = tiles[j][i];			// ブランクを紐付け
					recode.add(hole.lp);		// added by shima
					oldMove = hole;				// シャッフル１周目用に暫定保存
				}
			}
		}
	}
	
	protected int shuffle() {
		final float TWICE_VALUE = 2.0f;
		final float LIMIT_VALUE = 5.5f;
		
		ArrayList<LogicalTile> tile;
		
		// シャッフル開始
//		for (int i = 0; i < (int)(row * column * TWICE_VALUE * LIMIT_VALUE); i++) {
		for (int i = 0; i < 3; i++) {

			// 棋譜を一手追加
//			recode.add(new Point(hole.lp.x, hole.lp.y)); // removed by shima

			tile = canMoveTileSelect();
			/*
			 * 上のcanMoveTileSelectメソッドからは、前回動かしたタイルナンバーも来る可能性があるので
			 * 下のfor文で、前回タイルをtile配列から削除。
			 */
			for (int j = 0; j < tile.size(); j++) {
				if (oldMove.serial == tile.get(j).serial) {
					tile.remove(j);
					break;
				}
			}

			// 絞られた候補の中から、どのタイルを動かすかランダムで決定
			newMove = tile.get((int) (Math.random() * tile.size()));

				// デバッグ用
//				debug(i,tile);
			
			// タイルを交換
			slide(newMove);
		
			// シャッフル終了条件
			if (totalDistance >= (int)(row * column * TWICE_VALUE)) {
				break;
			}
		}
		// 棋譜に最後の一手を追加
//		recode.add(new Point(hole.lp.x, hole.lp.y)); // removed by shima

		return recode.size();
	}

	// 動かすことが可能な隣接タイルの番号を配列で返すメソッド
	protected ArrayList<LogicalTile> canMoveTileSelect() {

		ArrayList<LogicalTile> canMoveTiles = new ArrayList<LogicalTile>();

		if (hole.lp.x - 1 >= 0) {	// 左見て
			canMoveTiles.add(tiles[hole.lp.x - 1][hole.lp.y]);
		}
		if (hole.lp.x + 1 < column) {	// 右見て
			canMoveTiles.add(tiles[hole.lp.x + 1][hole.lp.y]);
		}
		if (hole.lp.y - 1 >= 0) {	// 上見て
			canMoveTiles.add(tiles[hole.lp.x][hole.lp.y - 1]);
		}
		if (hole.lp.y + 1 < row) {	// 下見て
			canMoveTiles.add(tiles[hole.lp.x][hole.lp.y + 1]);
		}
		return canMoveTiles;
	}

	// 離散距離を求めるメソッド
	public int getDistance(LogicalTile logTil) {
		int distnc = (Math.abs(logTil.serial / column - logTil.lp.y)		// 縦座標のズレ
				   + (Math.abs(logTil.serial % column - logTil.lp.x)));		// 横座標のズレ
		return distnc;
	}
	
	
	// 新メソッド　方向ゲット
	protected Direction getDirection(LogicalTile logTil) {
		return Direction.direction(logTil.lp, hole.lp);
		// 以下、を上記Directionのメソッドに委譲するようにした。
//		direction = Direction.NONE;
//		if (hole.lp.y == logTil.lp.y) {	// 上下に並んだ列の中から
//			if (hole.lp.x > logTil.lp.x) {	// 左見て
//				direction = Direction.RIGHT;
//			}
//			if (hole.lp.x < logTil.lp.x) {	// 右見て
//				direction = Direction.LEFT;
//			}
//		}
//		if (hole.lp.x == logTil.lp.x) {	// 左右に並んだ列の中から
//			if (hole.lp.y > logTil.lp.y) {	// 上見て
//				direction = Direction.DOWN;
//			}
//			if (hole.lp.y < logTil.lp.y) {	// 下見て
//				direction = Direction.UP;
//			}
//		}
//		return direction;	// デバッグ用に変数に入れてみたが、ifの中で直接returnしても良い
	}
	
	// 新メソッド　動かせるタイルリスト
	protected List<LogicalTile> getMovables(LogicalTile logTil) {
		List<LogicalTile> ltList = null;

		if (hole.lp.y == logTil.lp.y) {	// 左右に並んだ列の中から
			int i = hole.lp.x < logTil.lp.x ? 1: -1; // 右か左かで、int iの値を決定
			for(int x = hole.lp.x; logTil.lp.x != x; ){ // どちらの方向でも１つずつ順番に処理
				if(ltList == null)ltList = new ArrayList<LogicalTile>();
				ltList.add(tiles[x += i][hole.lp.y]);
			}
		}
		if (hole.lp.x == logTil.lp.x) {	// 上下も同様に
			int i = hole.lp.y < logTil.lp.y ? 1: -1;
			for(int y = hole.lp.y; logTil.lp.y != y; ){
				if(ltList == null)ltList = new ArrayList<LogicalTile>();
				ltList.add(tiles[hole.lp.x][y += i]);
			}
		}
		return ltList;
	}
	// 新メソッド　隣接チェック　および　タイル入れ替え
	protected boolean slide(LogicalTile logTil) {
		if ((hole.lp.x == logTil.lp.x && (Math.abs(hole.lp.y - logTil.lp.y) == 1))	 // 横または
		  ||(hole.lp.y == logTil.lp.y && (Math.abs(hole.lp.x - logTil.lp.x) == 1))){ // 縦に隣接しているか？
			
			totalDistance -= getDistance(logTil);	// 現状の離散度を減算

			// オブジェクトの入れ替え
			oldMove = tiles[logTil.lp.x][logTil.lp.y];
			tiles[logTil.lp.x][logTil.lp.y] = tiles[hole.lp.x][hole.lp.y];	
			tiles[hole.lp.x][hole.lp.y] = oldMove;	// oldMoveは前回データとして次の周に参照するので保持
			
			LogicalTile pointTmp = new LogicalTile();
		
			// ポイントの入れ替え
			pointTmp.lp = logTil.lp;
			logTil.lp = hole.lp;
			hole.lp = pointTmp.lp;
			// 棋譜への追加
			recode.add(hole.lp); // added by shima

			totalDistance += getDistance(logTil);	// 新しい離散度を加算

			return true;
		}
		return false;
	}
	boolean undo() {
		LogicalTile undo = getUndoTile();
		if (undo == null) return false;
		totalDistance -= getDistance(undo);	// 現状の離散度を減算
		Point u = undo.lp;
		Point h = hole.lp;
		// タイルを移動する
		LogicalTile tmp = tiles[u.x][u.y];
		tiles[u.x][u.y] = tiles[h.x][h.y];
		tiles[h.x][h.y] = tmp;
		// 論理位置を付け替える
		undo.lp = hole.lp;
		hole.lp = u;
		totalDistance += getDistance(undo);	// 新しい離散度を加算
		recode.remove(recode.size() - 1);	// 棋譜から除去
		return true;
	}
	// 最後に移動したタイルを取得
	LogicalTile getUndoTile() {
		int size = recode.size();
		if (size < 2) return null;
		Point p = recode.get(size - 2);
		return tiles[p.x][p.y];
	}
	// 最後に移動したタイルをアンドゥする際の方向を取得する
	Direction getUndoDirection() {
		Point latest = getUndoTile().lp;
		if (latest == null) return Direction.NONE;
		return Direction.direction(latest, hole.lp);
	}

	// デバッグ用グリッド表示メソッド
	private void debug(int i, ArrayList<LogicalTile> tile) {
		System.out.println("\n"+ (i) + "回転");

		if(i==0){
			Log.i("holeNumber", Integer.toString(hole.serial +1));
		}
		for(int j = 0; j <tile.size();j++){
			System.out.println("canMoveTile "+ (tile.get(j).serial +1));
		}
		System.out.println("Move serialNomber is " + (newMove.serial +1) + " to " + getDirection(newMove));

		for (int k = 0; k < row; k++) {
			for (int j = 0; j < column; j++) {
				if(tiles[j][k].serial +1 < 10)System.out.print(" ");
				System.out.print(" "+(tiles[j][k].serial +1));
			}
			System.out.print("\n");	
		}
		Log.i("moved", Integer.toString(newMove.serial +1));
		Log.i("totalDistance", Integer.toString(totalDistance));
		System.out.println("棋譜　X "+ hole.lp.x + ",  Y" + hole.lp.y);
	}
}