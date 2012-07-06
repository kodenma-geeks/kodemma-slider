package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

enum GameStatus {
	WAITING, PLAYING, PAUSED, UNDOING, SOLVED;
	boolean shielded() { return this == WAITING || this == PAUSED; }
	boolean animating() { return this == UNDOING || this == SOLVED; }
	boolean inputIgnored() { return this.shielded() || this.animating();}
}
interface BoardViewListener extends EventListener {
	public void onTileSlided(int count);
	public void onGameSolved(int rows, int cols, int slides);
}
interface AnimationListener extends EventListener {
	public void onUndoStarted();
	public void onUndoEnded();
	public void onSplashStarted();
	public void onSplashEnded();
}
public class BoardView extends View implements AnimationListener {
	private static final String TAG = BoardView.class.getSimpleName();
	private Point sp = new Point();			// 始点
	private Point ep = new Point();			// 終点
	private Point limiter = new Point();	// 移動ベクトルのリミッタ
	private Point vec;						// 移動ベクトル
	private Rect invalidated;				// 再描画する矩形領域
	private Board board;					// ゲーム盤クラス
	private List<Tile> movables = new ArrayList<Tile>();	// スライドするタイル群
	private Set<Tile> movablesSet = new HashSet<Tile>(); 
	private GameStatus gameStatus = GameStatus.WAITING;
	BoardViewListener boardViewListener;
	private AnimationListener splashListener = this;
	private Matrix splashMatrix;
	private int splushFrameCounter; // 仮利用
	// シールド関連
	private Rect shield;
	private Paint shieldPaint;
	private boolean DRAW_ALL = false; // 部分再描画：不具合対応-start
	
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
		
		boardViewListener.onTileSlided(board.slideCount);
	}
	@Override public boolean onTouchEvent(MotionEvent e) {
		if (gameStatus.inputIgnored()) { // シールド時、ゲーム完了時はイベントを受け付けない
			return true;
		}
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			sp.x = (int)e.getX(); sp.y = (int)e.getY();
			invalidated = board.getMovables(sp, movables, limiter, movablesSet);
			break;
		case MotionEvent.ACTION_MOVE:
			ep.x = (int)e.getX(); ep.y = (int)e.getY();
			if (invalidated == null) break;;
			vec = Utils.getAdjustedVector(sp, ep, limiter);
			if (vec == null) break;
			//invalidate(invalidated); 部分再描画：不具合対応-start
			invalidate();
			// 部分再描画：不具合対応-end
			break;
		case MotionEvent.ACTION_UP:
			if (vec == null) break;
			if (Utils.isSlided(vec, limiter)) {
				for (Tile t : movables) board.slide(t);
				boardViewListener.onTileSlided(board.slideCount);
				if (board.solved()) {	// パズルが解かれた場合
					splashListener.onSplashStarted();
				}
			}
			SoundEffect.getSound(SoundEffect.sound_Up);
			DRAW_ALL = true; // 部分再描画：不具合対応-start
			//invalidate(invalidated); 部分再描画：不具合対応-start
			invalidate();
			movablesSet.clear();
			// 部分再描画：不具合対応-end
			invalidated = null;
//			vec = null; // 部分再描画：不具合対応-start
			break;
		}
		return true;
	}
	@Override protected void onDraw(Canvas canvas) {
		if (gameStatus == GameStatus.SOLVED) {
			board.drawFrameInSplash(canvas, splashMatrix);
		} else {
			board.draw(canvas, gameStatus, movablesSet);
			if (DRAW_ALL) {
				Rect buffer = new Rect();
				for (Tile t : movables) board.drawTile(canvas, t, buffer);
				vec = null;
				DRAW_ALL = false;
			} else {
				for (Tile t : movables) board.drawTile(canvas, t, vec);
			}
			
			if (gameStatus.shielded()) {// シールドされている（ゲームが未開始、または中断されている）場合はシールドを描画する。
				canvas.drawRect(shield, shieldPaint);
			}
			// 以下、部分再描画：不具合対応-前
//			if (invalidated == null) {	// ゲームボード上の全タイルを描画する
//				//board.draw(canvas);
//				board.draw(canvas, gameStatus, movablesSet);	// 浜田　引き数を追加
//			} else {					// 移動したタイルだけを描画する
//				for (Tile t : movables) board.drawTile(canvas, t, vec);
//			}
//			if (gameStatus.shielded()) {// シールドされている（ゲームが未開始、または中断されている）場合はシールドを描画する。
//				canvas.drawRect(shield, shieldPaint);
//			}
		}
	}
	public void onUndoStarted() {
		gameStatus = GameStatus.UNDOING;
		splushFrameCounter = 0;
		// アンドゥ時のアニメーションを執り行うハンドラを起動
		new UndoHandler().start();
	}
	public void onUndoEnded() {
		gameStatus = GameStatus.PLAYING;
		invalidate();
	}
	public void onSplashStarted() {
		Log.d(TAG, "SOLVED!!!");
//		gameStatus = GameStatus.SOLVED;
		splushFrameCounter = 0;
		splashMatrix = new Matrix();
		// ゲーム完了時のスプラッシュアニメーションを執り行うハンドラを起動
		new SplashHandler().start();
		SoundEffect.getSound(SoundEffect.sound_solved);
	}
	public void onSplashEnded() {
		gameStatus = GameStatus.PLAYING;
		invalidate();
		splashMatrix = null;
		boardViewListener.onGameSolved(board.rows, board.cols, board.slideCount);
	}
	GameStatus startButtonPressed() {
		switch (gameStatus) {
		case WAITING:
			board.shuffle();
			boardViewListener.onTileSlided(board.slideCount);
			gameStatus = GameStatus.PLAYING;
			DRAW_ALL = true;
			invalidate();
			break;
		case PLAYING:
			// dialog
			board.shuffle();
			boardViewListener.onTileSlided(board.slideCount);
			DRAW_ALL = true;
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
			DRAW_ALL = true;
			invalidate();
			break;
		case PAUSED:
			gameStatus = GameStatus.PLAYING;
			DRAW_ALL = true;
			invalidate();
			break;
		}
		return gameStatus;
	}
	// アンドゥ時のアニメーションを執り行うハンドラ
	class UndoHandler extends Handler {
		private static final int INVALIDATE = 1;
		private static final int FRAMES = 10;
		private static final int INTERVAL = 1000 / FRAMES;
		private long nextTime;
		
		float deg = 3.3f/20f;
		void start() {
			Message msg = obtainMessage(INVALIDATE);
			nextTime = SystemClock.uptimeMillis();
			sendMessageAtTime(msg, nextTime);
		}
		private void sendNextMessage() {
			
			Message msg = obtainMessage(INVALIDATE);
			long current = SystemClock.uptimeMillis();
			if (nextTime < current) {
				nextTime = current + INTERVAL;
			}
			if (splushFrameCounter++ < FRAMES*1) {
				sendMessageAtTime(msg, nextTime);
				nextTime += INTERVAL;
			} else {
				splashListener.onSplashEnded();
			}
		}
		private void moveSplashMatrix(Matrix m) {
			//m.postScale(0.9f, 0.9f);
			m.setScale(0.9f, 0.9f);
			m.postRotate(deg);
			deg *= 2f;
		}
		@Override public void handleMessage(Message msg){
			if (msg.what == INVALIDATE) {
				gameStatus = GameStatus.SOLVED;
				moveSplashMatrix(splashMatrix);
				invalidate();
				sendNextMessage();
			}
		}
	}
	// ゲーム完了時のスプラッシュアニメーションを執り行うハンドラ
	class SplashHandler extends Handler {
		private static final int INVALIDATE = 1;
		private static final int FRAMES = 10;
		private static final int INTERVAL = 1000 / FRAMES;
		private long nextTime;
		
		float deg = 3.3f/20f;
		void start() {
			Message msg = obtainMessage(INVALIDATE);
			nextTime = SystemClock.uptimeMillis();
			sendMessageAtTime(msg, nextTime);
		}
		private void sendNextMessage() {
			
			Message msg = obtainMessage(INVALIDATE);
			long current = SystemClock.uptimeMillis();
			if (nextTime < current) {
				nextTime = current + INTERVAL;
			}
			if (splushFrameCounter++ < FRAMES*1) {
				sendMessageAtTime(msg, nextTime);
				nextTime += INTERVAL;
			} else {
				splashListener.onSplashEnded();
			}
		}
		private void moveSplashMatrix(Matrix m) {
			//m.postScale(0.9f, 0.9f);
			m.setScale(0.9f, 0.9f);
			m.postRotate(deg);
			deg *= 2f;
		}
		@Override public void handleMessage(Message msg){
			if (msg.what == INVALIDATE) {
				gameStatus = GameStatus.SOLVED;
				moveSplashMatrix(splashMatrix);
				invalidate();
				sendNextMessage();
			}
		}
	}
}