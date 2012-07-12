package kodemma.android.sliderpuzzle;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

// Droid の位置、または移動ベクトルを表すクラス
class Motion {
	private static final float MIN_DEGREE = 2f;
	private static final float MAX_DEGREE = 10f;
	private static final float MIN_VELOCITY = 5f;
	private static final float MAX_VELOCITY = 12f;
	static PointF viewSize;			// BackgroundViewのサイズ
	private static boolean vertical = true;	// Droidが降る方向
	float x, y;	// 位置、または移動ベクトル
	float d;	// 回転角度
	Motion(float xx, float yy, float dd) { x = xx; y = yy; d = dd; }
	static void initialize(int w, int h, boolean v) {
		if (viewSize == null) {
			viewSize = new PointF(w, h);
		} else {
			viewSize.x = w;
			viewSize.y = h;
		}
		vertical = v;
	}
	void add(Motion m) { x += m.x; y += m.y; d += m.d; }	// 現在位置に移動ベクトルを加算
	Motion negateX() { x = -x; return this; }	// 移動ベクトルの反転
	Motion negateY() { y = -y; return this; }	// 移動ベクトルの反転
	static Motion randomPosition() {			// 初期位置をランダムに取得
		float length = (vertical)? viewSize.x : viewSize.y;
		length = (float)(length * Math.random());
		float w, h;
		if (vertical) {
			w = length; h = 0f;
		} else {
			h = length; w = 0f;
		}
		return new Motion(w, h, randomScalar(MIN_DEGREE, MAX_DEGREE, true));
	}
	static Motion randomVector(Motion pos) {	// 移動ベクトルをランダムに取得
		double min, max;
		if (vertical) {
			min = Math.atan2(-viewSize.y, -pos.x);
			max = Math.atan2(-viewSize.y, viewSize.x - pos.x);
		} else {
			min = Math.atan2(pos.y - viewSize.y, viewSize.x);
			max = Math.atan2(pos.y, viewSize.x);
		}
		double theta = min + (max - min) * Math.random();
		double x = Math.cos(theta);
		double y = -Math.sin(theta);
		double v = randomScalar(MIN_VELOCITY, MAX_VELOCITY, false);
		return new Motion((int)(x*v), (int)(y*v), randomScalar(MIN_DEGREE, MAX_DEGREE, true));
	}
	// 範囲内のスカラー量をランダムに取得。 includeMinusRange=trueの場合は、マイナス側の範囲も含める。
	static float randomScalar(double min, double max, boolean includeMinusRange) {
		double range = max - min;
		if (includeMinusRange) {
			double rand = range*2.0 * Math.random(); 
			return (float)((rand < min)? min + rand : range - rand - min);
		} else {
			return (float)(range * Math.random() + min); 
		}
	}
	@Override public String toString() {	// デバッグ用
		return "{x=" + x + ", y=" + y + ", d=" + d + "}";
	}
}
// Droid 本体
class Droid {
	static Bitmap bitmap;
	static Matrix initial = new Matrix();	// 回転の中心をビットマップの中心にするためのマトリックス
	Motion pos;	// 現在位置
	Motion vec;	// ベクトル（一回移動分）
	Matrix mat = new Matrix();
	Droid(Motion p, Motion v) { pos = p; vec = v; }
	static void initialize(Bitmap b) {
		bitmap = b;
		// 回転の中心をビットマップの中心にする
		initial.setTranslate(bitmap.getWidth()/-2, bitmap.getHeight()/-2);
	}
	void move() {	// １フレーム分の移動を行う
		pos.add(vec);	// 移動
		// マトリックス更新
		mat.set(initial);
		mat.postRotate(pos.d);
		mat.postTranslate(pos.x, pos.y);
		// 領域判定
		if (pos.x < 0 || pos.x > Motion.viewSize.x) { vec.negateX(); }
		if (pos.y < 0 || pos.y > Motion.viewSize.y) { vec.negateY(); }
	}
	@Override public String toString() {	// デバッグ用
		return "{pos=" + pos + ", vec=" + vec + ", mat=" + mat + "}";
	}
}
// Droid 牧場
class DroidFarm {
	private static final int MAX_DROIDS = 10;
	List<Droid> droids = new ArrayList<Droid>();
	Paint paint	= new Paint(Paint.ANTI_ALIAS_FLAG);
	void moveDroids() { for (Droid d : droids) { d.move(); } }
	void drawDroids(Canvas canvas) {
		for (Droid d : droids) {
//			Log.d("AA", "droid=" + d);
			canvas.drawBitmap(Droid.bitmap, d.mat, paint);
		}
	}
	void createDroid() {
		Motion pos = Motion.randomPosition();
		Motion vec = Motion.randomVector(pos);
		Droid droid = new Droid(pos, vec);
		droid.move();
		droids.add(droid);
		if (droids.size() > MAX_DROIDS) {
			Droid removed = droids.remove(0);
			removed = null;
		}
	}
	void clearDroids() { droids.clear(); }
}
public class BackgroundView extends View {
	private static final float APPEARANCE_ODDS = 0.05f;	// 	Droid の出現確率
	DroidFarm droidFarm = new DroidFarm();
	DroidHandler droidHandler = new DroidHandler();
	
	public BackgroundView(Context context) { this(context, null); }
	public BackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);	
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		Droid.initialize(bitmap);
	}
	@Override protected void onSizeChanged(int w, int h, int pw, int ph) {
		super.onSizeChanged(w, h, pw, ph);
	}
	@Override protected void onDraw(Canvas canvas) { droidFarm.drawDroids(canvas); }
	void shuffled(Bitmap b) {
		boolean vertically = Utils.verticallyFit(b.getWidth(), b.getHeight(), getWidth(), getHeight());
		Motion.initialize(getWidth(), getHeight(), !vertically);
		droidFarm.clearDroids();
		droidHandler.stop();
		if (Utils.lot(APPEARANCE_ODDS)) { droidHandler.start(); }
	}
	void slided() { if (droidHandler.active()) { droidFarm.createDroid(); } }
	
	class DroidHandler extends Handler {
		private static final int INVALIDATE = 1;
		private static final int INTERVAL = 100;
		private long nextTime;
		private boolean stop = true;
		void start() {
			stop = false;
			Message msg = obtainMessage(INVALIDATE);
			nextTime = SystemClock.uptimeMillis();
			sendMessageAtTime(msg, nextTime);
		}
		void stop() { stop = true; }
		boolean active() { return !stop; }
		private void sendNextMessage() {
			Message msg = obtainMessage(INVALIDATE);
			long current = SystemClock.uptimeMillis();
			if (nextTime < current) { nextTime = current + INTERVAL; }
			sendMessageAtTime(msg, nextTime);
			nextTime += INTERVAL;
		}
		@Override public void handleMessage(Message msg){
			if (msg.what == INVALIDATE) {
				if (stop) return;
				droidFarm.moveDroids();
				invalidate();
				sendNextMessage();
			}
		}
	}
}
