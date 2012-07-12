package kodemma.android.sliderpuzzle;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;

// 方向を表す列挙型
enum Direction {
	NONE, UP, DOWN, RIGHT, LEFT, OTHER;
	public boolean virtical() { return this == UP || this == DOWN; }
	public boolean horizontal() { return this == RIGHT || this == LEFT; }
	public boolean invalid() { return this == NONE || this == OTHER; }
	public boolean valid() { return !invalid(); }
	public static Direction direction(Point sp, Point ep) {	// 始点から終点への方向を取得する。 
		int x = ep.x - sp.x; int y = ep.y - sp.y;
		if (x == 0 && y == 0) return NONE;
		if (x == 0) return (y > 0)? DOWN : UP;
		if (y == 0) return (x > 0)? RIGHT : LEFT;
		return OTHER;
	}
}
// ユーティリティメソッドを集めたクラス 
public class Utils {
	private static final String PACKAGE_NAME = Utils.class.getPackage().getName();
	private static final String URI_SCHEME = "android.resource://";
	
	// 指定された確率（odds:0.0～1.0)のくじを引いて、当たればtrueが返る。
	public static boolean lot(float odds) { return Math.random() < odds; }
	// 指定されたIDのリソースを示すURIを取得する
	public static String getResourceUri(int resId) { return URI_SCHEME + PACKAGE_NAME + "/" + resId; }
	// src矩形の全体を、縦横比を変えずに、dst矩形にフィットさせるためのマトリックスを計算する。
	public static Matrix adjustingMatrix(float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
		float scale;
		float dx = 0f, dy = 0f;
		if (verticallyFit(srcWidth, srcHeight, dstWidth, dstHeight)) {
			scale = dstHeight/srcHeight;
			dx = (dstWidth - scale*srcWidth) / 2f;
		} else {
			scale = dstWidth/srcWidth;
			dy = (dstHeight - scale*srcHeight) / 2f;
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		matrix.postTranslate(dx, dy);
		return matrix;
	}
	// 縦方向にフィットするかどうか
	public static boolean verticallyFit(float srcWidth, float srcHeight, float dstWidth, float dstHeight) {
		return (dstWidth/dstHeight > srcWidth/srcHeight);
	}
	public static Point getColRow(Bitmap b, Level lv) {
		int col;
		int row;
		if(b.getWidth() < b.getHeight()){
			col = lv.small();
			row = lv.large();
		}else{
			row = lv.small();
			col = lv.large();			
		}
		return new Point(col, row);
	}
	// フリップの始点(sp)、終点(ep)から、その水平または垂直ベクトルをPointとして取得する。
	// 但し、limiterの値が最大値、または最小値となるように制限される。
	public static Point getAdjustedVector(Point sp, Point ep, Point limiter) {
		Point vec = new Point(ep.x - sp.x, ep.y - sp.y);
		if (limiter.x == 0) {
			vec.x = 0;
			if (limiter.y < 0) {
				if (vec.y < limiter.y) vec.y = limiter.y;
				if (vec.y > 0) vec.y = 0;
			} else {
				if (vec.y > limiter.y) vec.y = limiter.y;
				if (vec.y < 0) vec.y = 0;
			}
		} else {
			vec.y = 0;
			if (limiter.x < 0) {
				if (vec.x < limiter.x) vec.x = limiter.x;
				if (vec.x > 0) vec.x = 0;
			} else {
				if (vec.x > limiter.x) vec.x = limiter.x;
				if (vec.x < 0) vec.x = 0;
			}
		}
		return vec;
	}
	public static Rect scaleByBorder(int borderWidth, Rect src, Rect dst) {
		dst.left	= -borderWidth + src.left;
		dst.top		= -borderWidth + src.top;
		dst.right	=  borderWidth + src.right;
		dst.bottom	=  borderWidth + src.bottom;
		return dst;
	}
	public static Rect translateByVector(Point vec, Rect src, Rect dst) {
		if (vec == null) return dst;
		dst.left	= vec.x + src.left;
		dst.top		= vec.y + src.top;
		dst.right	= vec.x + src.right;
		dst.bottom	= vec.y + src.bottom;
		return dst;
	}
	public static Rect translateByVector(Point vec, Rect rect) {
		if (vec == null) return rect;
		rect.left	+= vec.x;
		rect.top	+= vec.y;
		rect.right	+= vec.x;
		rect.bottom	+= vec.y;
		return rect;
	}
	public static RectF translate(float x, float y, RectF rect) {
		rect.left	+= x;
		rect.top	+= y;
		rect.right	+= x;
		rect.bottom	+= y;
		return rect;
	}
	public static boolean isSlided(Point vec, Point limiter) {
		return Math.abs(vec.x*2) > Math.abs(limiter.x) || Math.abs(vec.y*2) > Math.abs(limiter.y);
	}
	public static void drawTag(Canvas canvas, String text,  float x, float y, Paint textPaint, Paint tagPaint, Paint shadowPaint) {
		final float MARGIN = 5f;
		final float SHADOW = 2f;
		FontMetrics fm = textPaint.getFontMetrics();
		float textWidth = textPaint.measureText(text);		// 文字列の幅を取得
		float textX = x - textWidth / 2f;					// 文字列の幅からX座標を計算
		float textY = y - (fm.ascent + fm.descent) / 2f;	// 文字列の高さからY座標を計算
		// タグの影の矩形を計算
		float left		= textX - MARGIN + SHADOW ;
		float right		= textX + MARGIN + SHADOW + textWidth;
		float top		= textY - MARGIN + SHADOW + fm.ascent;
		float bottom	= textY + MARGIN + SHADOW + fm.descent; 
		RectF rect = new RectF(left, top, right, bottom);
		canvas.drawRoundRect(rect, MARGIN, MARGIN, shadowPaint);	// タグの影を描画
		rect = translate(-SHADOW, -SHADOW, rect);					// タグの影の部分をスライドする
		canvas.drawRoundRect(rect, MARGIN, MARGIN, tagPaint);		// タグの描画
		canvas.drawText(text, textX, textY, textPaint);				// 文字列の描画
	}
}
