package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

// パズルのタイルを表すクラス
class Tile {
	Rect src;					// ビットマップ本来の矩形
	Rect dst;					// 描画先の矩形
	LogicalTile logicalTile;	// 論理タイル
	
	Tile(RectF srcF, RectF dstF, LogicalTile lt) {
		srcF.round(src = new Rect());
		dstF.round(dst = new Rect());
		logicalTile = lt;
	}
	@Override public String toString() {	// デバッグ用
		return "logicalTile={" + logicalTile.toString() + "}, src=" + src + ", dst=" + dst;
	}
}
// パズルのゲーム盤を表すクラス
public class Board {
	private static final int BORDER_WIDTH = 2;	// タイルの枠線の幅
	Bitmap bitmap;								// ユーザが選択したビットマップイメージ
	int rows;									// パズルの行数
	int cols;									// パズルの列数
	int width;									// BoardViewの幅
	int height;									// BoardViewの高さ
	List<Tile> tiles;							// タイルの集合
	private LogicalBoard logicalBoard;			// 論理ゲーム盤
	private Map<Point, Rect> dstRectsMap;		// タイルの論理位置と描画時の矩形のマッパー
	private Map<LogicalTile, Tile> tilesMap;	// 論理タイルとタイルのマッパー
	int slideCount = 0;							// ユーザのスライド回数
	boolean showId = true;						// タイル番号の表示/非表示
	Paint textPaint;							// タイル番号表示用(テキスト)
	Paint tagPaint;								// タイル番号表示用(タグ)
	Paint shadowPaint;							// タイル番号表示用(タグの影)
	
	Board(Bitmap b, int r, int c, int w, int h) {
		bitmap = b;
		rows = r; cols = c;
		width = w; height = h;
//		logicalBoard = new LogicalBoard(rows=r, cols=c);
		textPaint	= new Paint(Paint.ANTI_ALIAS_FLAG);
		tagPaint	= new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint	= new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(14);
		textPaint.setColor(Color.WHITE);
		tagPaint.setColor(Color.GRAY);
		shadowPaint.setColor(Color.DKGRAY);
	}
	// タイルの生成と初期化を行う
	void initializeTiles() {
		// タイルのsrc矩形からdst矩形への変換をするためのマトリックスを作成する
		Matrix m = Utils.adjustingMatrix(bitmap.getWidth(), bitmap.getHeight(), width, height);
		// 論理タイルの初期化
		logicalBoard = new LogicalBoard(rows, cols);
		// タイルの生成と初期化を行う
		tiles = createTiles(rows, cols, bitmap.getWidth(), bitmap.getHeight(), m);
	}
	// タイルの生成と初期化を行う
	private List<Tile> createTiles(int rows, int cols, float w, float h, Matrix m) {
		dstRectsMap = new HashMap<Point, Rect>();
		tilesMap = new HashMap<LogicalTile, Tile>();
		List<Tile> list = new ArrayList<Tile>();
		float dx = w/cols, dy = h/rows;
		float top=0f, bottom=dy;		// 以下、誤差を少なくするために敢てfloatで計算している
		for (int i=0; i<rows; i++) {
			float left = 0f, right = dx;
			for (int j=0; j<cols; j++) {
				RectF src = new RectF(left, top, right, bottom);
				RectF dst = new RectF();
				m.mapRect(dst, src);
				
				LogicalTile logicalTile = logicalBoard.tiles[i][j];
				Tile tile = new Tile(src, dst, logicalTile);
				dstRectsMap.put(logicalTile.lp, new Rect(tile.dst));
				tilesMap.put(logicalTile, tile);
				list.add(tile);
				left = right; right += dx;
			}
			top = bottom; bottom += dy;
		}
		return list;
	}
	// タイルがスライドした際の再描画領域、スライドするタイル群、移動ベクトルのリミッタを取得する。 pはタップ位置。
	Rect getMovables(Point p, List<Tile> movables, Point limiter) {
		movables.clear();			// 前回スライド時のタイル群を除去
		LogicalTile selected = null;;
		for (Tile t : tiles) {		// タップ位置のタイルを取得
			if (t.dst.contains(p.x, p.y)) { selected = t.logicalTile; break; }
		}
		if (selected == null) return null;
		// 論理ゲーム盤オブジェクトに、スライドするタイル群を問い合わせる
		List<LogicalTile> lgTiles = logicalBoard.getMovables(selected);
		if (lgTiles == null) return null;
		Rect rect = new Rect();
		for (LogicalTile lg : lgTiles) {	// 再描画の矩形領域を計算する
			Tile tile = tilesMap.get(lg);
			rect.union(tile.dst);
			movables.add(tile);
		}
		Tile hole = tilesMap.get(logicalBoard.hole);
		rect.union(hole.dst);				// 穴タイルも再描画矩形領域に加える
		// タイルの移動方向から、移動ベクトルのリミッタを計算する
		limiter.x = limiter.y = 0;
		switch (logicalBoard.getDirection(selected)) {
		case UP:	limiter.y = -hole.dst.height();	break;
		case DOWN:	limiter.y =  hole.dst.height();	break;
		case LEFT:	limiter.x = -hole.dst.width();	break;
		case RIGHT:	limiter.x =  hole.dst.width();	break;
		}
		return rect;
	}
	// タイルをランダムにシャッフルする
	int shuffle() {
		slideCount = 0;
		// 論理タイルの初期化 - 本当は無条件にnewするのは無駄が多いが、とりあえず。
		initializeTiles();
		//
		int counter = logicalBoard.shuffle();	// 論理タイルをシャッフルする
		for (Tile tile : tiles) {	// 論理タイルのシャッフル結果に合わせて、dst矩形も再設定する。
			tile.dst = dstRectsMap.get(tile.logicalTile.lp);
		}
		return counter;
	}
	// タイルをスライドする
	void slide(Tile tile) {
		logicalBoard.slide(tile.logicalTile);
		Tile hole = tilesMap.get(logicalBoard.hole);
		// スライドしたタイルと穴タイルのdst矩形を交換する。
		Rect tmp = tile.dst;
		tile.dst = hole.dst;
		hole.dst = tmp;
		slideCount++;
	}
	// ゲーム盤全体を描画する
	void draw(Canvas canvas) {
		Rect buffer = new Rect();								// 作業用の矩形
		for (Tile t : tiles) {
			if (logicalBoard.hole == t.logicalTile) continue;	// 穴タイルは描画しない
			drawTile(canvas, t, buffer);
		}
	}
	// 移動ベクトルを加算したタイルを描画する
	void drawTile(Canvas canvas, Tile tile, Point vector) {
		Rect buffer = new Rect();								// 作業用の矩形
		Utils.translateByVector(vector, tile.dst, buffer);		// 移動ベクトルを加算する
		Utils.scaleByBorder(-BORDER_WIDTH, buffer, buffer);		// 枠線の分だけタイルを縮小する
		drawTile(canvas, tile.src, buffer, getTileId(tile));
	}
	// タイルを描画する - draw()メソッドからコールされる
	private void drawTile(Canvas canvas, Tile tile, Rect buffer) {
		Utils.scaleByBorder(-BORDER_WIDTH, tile.dst, buffer);	// 枠線の分だけタイルを縮小する
		drawTile(canvas, tile.src, buffer, getTileId(tile));
	}
	// タイルを描画する - 最終的にこのメソッドで描画
	private void drawTile(Canvas canvas, Rect s, Rect d, String id) {
		canvas.drawBitmap(bitmap, s, d, null);
		if (showId) {	// タイルの番号タグを描画する
			Utils.drawTag(canvas, id, (d.left+d.right)/2, (d.top+d.bottom)/2, textPaint, tagPaint, shadowPaint);
		}
	}
	// タイルの番号を取得する
	private String getTileId(Tile tile) {
		return (showId)? String.valueOf(tile.logicalTile.serial + 1) : null;
	}
}