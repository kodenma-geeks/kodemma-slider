package kodemma.android.sliderpuzzle;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.ContentResolver;
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
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

enum GameStatus {
	WAITING, PLAYING, PAUSED, UNDOING, SOLVED, SP_ENDED;
	boolean shielded() { return this == WAITING || this == PAUSED; }
	boolean animating() { return this == UNDOING || this == SOLVED || this == SP_ENDED; }
	boolean inputIgnored() { return this.shielded() || this.animating();}
}
interface BoardViewListener extends EventListener {
	public void onTileSlided(int count);
	public void onGameSolved(int rows, int cols, int slides);
	public void onChronometerSwitched(boolean onoff);
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
	Board board;					// ゲーム盤クラス
	Bitmap bitmap; // 浜田　7/5
//	boolean showId;						// タイル番号の表示/非表示	// 浜田　7/12 Boardのほうで宣言
//	boolean isGrid;						// タイル枠の表示/非表示	// 浜田　7/12 Boardのほうで宣言
//	boolean isSound_mute; // 浜田　7/12
	Level level; // 浜田　7/5
	private List<Tile> movables = new ArrayList<Tile>();	// スライドするタイル群
	private Set<Tile> movablesSet = new HashSet<Tile>(); 
	static GameStatus gameStatus = GameStatus.WAITING;
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

		int lv = (SelectLevelActivity.getLevelSetting(context)==0)? 1: SelectLevelActivity.getLevelSetting(context);
		level = Level.levels().get(lv);

// 以下、削除 by shima
//		Uri u = SelectLevelActivity.getImgUriSetting(context);
//		bitmap = setImgUriSetting(u, context);
//以下、追加 by shima
//		bitmap = SelectLevelActivity.getBitmapSetting(context);
		SelectLevelActivity.setDefaultBitmapUriSetting(context);	// 7/18 追加
		bitmap = SelectLevelActivity.getBitmapSetting(context);
//以上。　by shima
		
		SoundEffect.mute(SelectLevelActivity.getSoundSetting(context));
	}
//	// Uriから画像をセットするメソッド
//	protected Bitmap setImgUriSetting(Uri u, Context cn) {
//		InputStream inputStream = null;
//		try {
//			inputStream = cn.getContentResolver().openInputStream(u);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//		return BitmapFactory.decodeStream(inputStream);
//	}
	@Override protected void onSizeChanged(int w, int h, int pw, int ph) {
		super.onSizeChanged(w, h, pw, ph);
		shield = new Rect(0, 0, w, h);
		
//		board = new Board(bitmap, Utils.getColRow(bitmap, level).y,  Utils.getColRow(bitmap, level).x, w, h, showId,isGrid);
		boolean showId = SelectLevelActivity.getHintSetting(getContext());
		boolean isGrid = SelectLevelActivity.getTileSetting(getContext());
		board = new Board(bitmap, level, w, h, showId, isGrid);// 浜田　7/11　スリム化
		board.initializeTiles();
// 		board.shuffle();
		
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
			//Log.d(TAG, "sp=" + sp + ", ep=" + ep + ", vec=" + vec);
			if (vec == null) {
				movablesSet.clear();	// バックグラウンドでドロイドが動いていると、その間ずっとinvalidate()がシステムより発せられているため、
										// このリストがあると、リストに含まれているタイルが消えてしまうので、クリアするようにした。
				break;
			}
			if (Utils.isSlided(vec, limiter)) {
				board.slideCount++; // 浜田　まとめて動かしたときは１枚　7/13
				for (Tile t : movables) board.slide(t);
				boardViewListener.onTileSlided(board.slideCount);
				if (board.solved()) {	// パズルが解かれた場合
					boardViewListener.onChronometerSwitched(false);
					BoardActivity.buttonMap.get(R.id.board_button_start).setEnabled(false);
					BoardActivity.buttonMap.get(R.id.board_button_pause).setEnabled(false);
					BoardActivity.buttonMap.get(R.id.board_button_setting).setEnabled(false);
					BoardActivity.buttonMap.get(R.id.board_button_title).setEnabled(false);
//					splashListener.onSplashStarted();
					splashListener.onUndoStarted();
				}
			}
//			SoundEffect.getSound(SoundEffect.sound_Up);
			if(vec.x != 0 || vec.y != 0){	// 動かさなかった時は音を鳴らさない　7/10　浜田
				SoundEffect.getSound(R.raw.switch_ti);
			}

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
//		Log.d(TAG, "gameStatus=" + gameStatus + ", DRAW_ALL=" + DRAW_ALL);
//		if (gameStatus == GameStatus.SP_ENDED) {	// 浜田　7/12 追加
//			board.draw(canvas, movablesSet);
//		} else if (gameStatus == GameStatus.SOLVED) {
		if (gameStatus == GameStatus.SOLVED) {
			board.draw(canvas, movablesSet);	// 浜田　7/13 引数をひとつ除去
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
		Toast.makeText(getContext(),(R.string.congratulation),Toast.LENGTH_SHORT).show();	// クリア時に表示
		gameStatus = GameStatus.UNDOING;
		splushFrameCounter = 0;
		// アンドゥ時のアニメーションを執り行うハンドラを起動
		new UndoHandler().start();
		SoundEffect.getSound(R.raw.switch_ti);
	}
	public void onUndoEnded() {
//		gameStatus = GameStatus.PLAYING;
//		gameStatus = GameStatus.WAITING;
		gameStatus = GameStatus.SP_ENDED;
//		invalidate();
		boardViewListener.onGameSolved(board.rows, board.cols, board.slideCount);
	}
	public void onSplashStarted() {
		Log.d(TAG, "SOLVED!!!");
//		gameStatus = GameStatus.SOLVED;
		splushFrameCounter = 0;
		splashMatrix = new Matrix();
		// ゲーム完了時のスプラッシュアニメーションを執り行うハンドラを起動
		new SplashHandler().start();
		SoundEffect.getSound(R.raw.turururu);
	}
	public void onSplashEnded() {
		gameStatus = GameStatus.PLAYING;
		invalidate();
		splashMatrix = null;
		boardViewListener.onGameSolved(board.rows, board.cols, board.slideCount);
	}
	GameStatus startButtonPressed() {
		movables.clear();
		movablesSet.clear();
		vec = null;
		switch (gameStatus) {
		case PAUSED:	// PAUSED追加
			break;
		case SP_ENDED:	// SP_ENDED追加
		case WAITING:
			board.shuffle();
			boardViewListener.onTileSlided(board.slideCount);
			gameStatus = GameStatus.PLAYING;
			DRAW_ALL = false;
			invalidate();
			break;
		case PLAYING:
			// dialog
			board.shuffle();
			boardViewListener.onTileSlided(board.slideCount);
			DRAW_ALL = false;
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
			board.showId = false;
			gameStatus = GameStatus.PAUSED;	// PAUSED時はヒントを出さない
			DRAW_ALL = true;
			invalidate();
			break;
		case PAUSED:
			board.showId = SelectLevelActivity.getHintSetting(getContext());	// 値を戻す
			gameStatus = GameStatus.PLAYING;
			DRAW_ALL = true;
			invalidate();
			break;
		}
		return gameStatus;
	}
	GameStatus undoButtonPressed() {
//		switch (gameStatus) {
//		case PLAYING:
//			gameStatus = GameStatus.PAUSED;
			board.undo();
			DRAW_ALL = true;
			invalidate();
//			break;
//		}
		return gameStatus;
	}
	// アンドゥ時のアニメーションを執り行うハンドラ
	class UndoHandler extends Handler {
		private static final int INVALIDATE = 1;
		private static final int FRAMES = 10;
		private static final int INTERVAL = 1000 / FRAMES;
		private long nextTime;
		
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
			sendMessageAtTime(msg, nextTime);
			nextTime += INTERVAL;
		}
		@Override public void handleMessage(Message msg){
			if (msg.what == INVALIDATE) {
				gameStatus = GameStatus.SOLVED;
				if (board.undo()) {
					SoundEffect.getSound(R.raw.switch_ti);
					DRAW_ALL = true;
					invalidate();
					sendNextMessage();
				} else {
					splashListener.onUndoEnded();
				}
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