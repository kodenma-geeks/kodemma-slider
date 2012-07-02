package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

enum GameStatus {
	WAITING, PLAYING, PAUSED;
	boolean shielded() { return this == WAITING || this == PAUSED; }
}
interface TileListener extends EventListener {
	public void onTileSlided(int count);
}

public class BoardView extends View {
	private static final String TAG = BoardView.class.getSimpleName();
	private Point sp = new Point();			// 始点
	private Point ep = new Point();			// 終点
	private Point limiter = new Point();	// 移動ベクトルのリミッタ
	private Point vec;						// 移動ベクトル
	private Rect invalidated;				// 再描画する矩形領域
	private Board board;					// ゲーム盤クラス
	private List<Tile> movables = new ArrayList<Tile>();	// スライドするタイル群
	
	GameStatus gameStatus = GameStatus.WAITING;
	TileListener tileListener;
	// シールド関連
	private Rect shield;
	private Paint shieldPaint;
	
	public BoardView(Context context) { this(context, null); }
	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(Color.alpha(0));
		shieldPaint = new Paint();
		shieldPaint.setColor(Color.BLUE);
		shieldPaint.setAlpha(128);
	}
	@Override protected void onSizeChanged(int w, int h, int pw, int ph) {
		super.onSizeChanged(w, h, pw, ph);
		shield = new Rect(0, 0, w, h);
		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sky);
		board = new Board(bitmap, 4, 3, w, h);
		board.initializeTiles();
//		board.shuffle();
		
		tileListener.onTileSlided(board.slideCount);
	}
	@Override public boolean onTouchEvent(MotionEvent e) {
		if (gameStatus.shielded()) { // シールド時はイベントを受け付けない
			return true;
		}
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			sp.x = (int)e.getX(); sp.y = (int)e.getY();
			invalidated = board.getMovables(sp, movables, limiter);
			break;
		case MotionEvent.ACTION_MOVE:
			ep.x = (int)e.getX(); ep.y = (int)e.getY();
			if (invalidated == null) break;;
			vec = Utils.getAdjustedVector(sp, ep, limiter);
			if (vec == null) break;
			invalidate(invalidated);
			break;
		case MotionEvent.ACTION_UP:
			if (vec == null) break;
			if (Utils.isSlided(vec, limiter)) {
				for (Tile t : movables) board.slide(t);
			}
			if (tileListener != null) {
				tileListener.onTileSlided(board.slideCount);
			}
			invalidate(invalidated);
			invalidated = null;
			vec = null;
			break;
		}
		return true;
	}
	@Override protected void onDraw(Canvas canvas) {
		if (invalidated == null) {	// ゲームボード上の全タイルを描画する
			board.draw(canvas);
		} else {					// 移動したタイルだけを描画する
			for (Tile t : movables) board.drawTile(canvas, t, vec);
		}
		if (gameStatus.shielded()) {// シールドされている（ゲームが未開始、または中断されている）場合はシールドを描画する。
			canvas.drawRect(shield, shieldPaint);
		}
	}
	GameStatus startButtonPressed() {
		switch (gameStatus) {
		case WAITING:
			board.shuffle();
			if (tileListener != null) tileListener.onTileSlided(board.slideCount);
			gameStatus = GameStatus.PLAYING;
			invalidate();
			break;
		case PLAYING:
			// dialog
			board.shuffle();
			if (tileListener != null) tileListener.onTileSlided(board.slideCount);
			invalidate();
			break;
		}
		return gameStatus;
	}
	GameStatus pauseButtonPressed() {
		switch (gameStatus) {
		case WAITING:
			break;
		case PLAYING:
			gameStatus = GameStatus.PAUSED;
			invalidate();
			break;
		case PAUSED:
			gameStatus = GameStatus.PLAYING;
			invalidate();
			break;
		}
		return gameStatus;
	}
}