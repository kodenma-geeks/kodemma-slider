package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

class LogicalTile {
	int serial;				// 実番号より１少ない連番（０スタート）
	Point lp = new Point(); // logical position
	void setPoint(int x, int y) {
		this.lp.x = x;
		this.lp.y = y;
	}
}

/**
 * シャッフルロジックなどのロジカルな処理
 * @author hamada
 * @version　1.01
 */
public class LogicalBoard {
	private int row;
	private int column;
	protected int totalDistance;
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

	/**
	 * シャッフルロジックメソッド
	 * @return　棋譜の配列の要素数（つまり手数）
	 */
	protected int shuffle() {
		final float TWICE_VALUE = 2.0f;	// 充分なシャッフルがなされるための値
		final float LIMIT_VALUE = 2.7f;	// シャッフル制限のための値
		
		ArrayList<LogicalTile> tile;
		
		// シャッフル開始
//		for (int i = 0; i < (int)(row * column * TWICE_VALUE * LIMIT_VALUE); i++) {
		for (int i = 0; i < 3; i++){ 	// デバッグ用ループ

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
			
			// タイルを交換
			slide(newMove);
		
			// シャッフル終了条件
			if (totalDistance >= (int)(row * column * TWICE_VALUE)) {
				break;
			}
		}
		return recode.size();
	}

	/**
	 * 動かすことが可能な隣接タイルを配列で返す
	 * @return 動かすことが可能な隣接タイルの配列
	 */
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

	/**
	 * 離散距離を求める
	 * @param logTil　LogicalTile型クラスのタイル
	 * @return 離散距離（縦座標のズレと横座標のズレの合計）
	 */
	protected int getDistance(LogicalTile logTil) {
		int distnc = (Math.abs(logTil.serial / column - logTil.lp.y)		// 縦座標のズレ
				   + (Math.abs(logTil.serial % column - logTil.lp.x)));		// 横座標のズレ
		return distnc;
	}
	/**
	 * タイル移動方向の取得
	 * @see   Utils.direction(Point sp, Point ep) 
	 * @param logTil
	 * @return　ENUM　{NONE, UP, DOWN, RIGHT, LEFT, OTHER}
	 */
	protected Direction getDirection(LogicalTile logTil) {
		return Direction.direction(logTil.lp, hole.lp);
		// メソッド内処理、を上記Directionのメソッドに委譲するようにした。
	}
	/**
	 * 穴の場所から引数のタイルまでの動かせるタイルの配列取得
	 * @param logTil
	 * @return　穴から近い順のタイルの配列
	 */
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
	/**
	 * 隣接チェック
	 * タイル入れ替え、及び離散度の更新
	 * @param logTil
	 * @return false:隣接しておらず　　true:入れ替え処理完了
	 */
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
	/**
	 * 最後に移動したタイルを戻す
	 * @return　false：これ以上戻せない　　true:一手戻す
	 */
	protected boolean undo() {
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
	/**
	 * 最後に移動したタイルを取得
	 * @return　タイル
	 */
	protected LogicalTile getUndoTile() {
		int size = recode.size();
		if (size < 2) return null;
		Point p = recode.get(size - 2);
		return tiles[p.x][p.y];
	}
	/**
	 * 最後に移動したタイルをアンドゥする際の方向を取得する
	 * @return ENUM
	 */
	protected Direction getUndoDirection() {
		Point latest = getUndoTile().lp;
		if (latest == null) return Direction.NONE;
		return Direction.direction(latest, hole.lp);
	}
}