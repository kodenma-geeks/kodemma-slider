package kodemma.android.sliderpuzzle;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

public class GifView extends View {

	private GifDecoder decoder;
	private Bitmap bitmap;

	private int width;
	private int height;

	private long time;
	private int index;

	private boolean decoding = false;

	private int resId;
	private String filePath;

	private Handler handler = new Handler();

	public GifView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructor
	 */
	public GifView(Context context) {
		super(context);
	}

	private InputStream getInputStream() {
		if (filePath != null)
			try {
				return new FileInputStream(filePath);
			} catch (FileNotFoundException e) {
			}
		if (resId > 0)
			return getContext().getResources().openRawResource(resId);
		return null;
	}

	/**
	 * set gif file path
	 * 
	 * @param filePath
	 */
	public void setGif(String filePath) {
		this.filePath = filePath;
		decode();
	}

	/**
	 * set gif res id
	 * 
	 * @param resId
	 */
	public void setGif(int resId) {
		this.resId = resId;
		decode();
	}

	private void decode() {
		release();
		decoding = true;
		time = System.currentTimeMillis();
		index = 0;

		new Thread() {
			@Override
			public void run() {
				try {
					GifDecoder decoder = new GifDecoder();
					decoder.read(getInputStream());
					width = decoder.width;
					height = decoder.height;
					if (width == 0 || height == 0)
						throw new Exception();
					GifView.this.decoder = decoder;
				} catch (Throwable e) {
					try {
						Bitmap bitmap = BitmapFactory.decodeStream(getInputStream());
						width = bitmap.getWidth();
						height = bitmap.getHeight();
						GifView.this.bitmap = bitmap;
					} catch (Exception e1) {
					}
				} finally {
					handler.post(new Runnable() {
						public void run() {
							GifView.this.setLayoutParams(new LayoutParams(width, height));
						}
					});
					postInvalidate();
					decoding = false;
				}
			}
		}.start();
	}

	/*
	 * @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { setMeasuredDimension(width, height); }
	 */
	public void release() {
		decoder = null;
		bitmap = null;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (decoding) {
			Paint paint = new Paint();
			paint.setColor(Color.rgb(180, 150, 150));
			canvas.drawText("Loading ...", 20, 30, paint);
			invalidate();
			return;
		}

		if (bitmap != null) {
			canvas.drawBitmap(bitmap, 0, 0, null);
			return;
		}
		if (decoder == null)
			return;
		long now = System.currentTimeMillis();
		if (time + decoder.getDelay(index) < now) {
			time += decoder.getDelay(index);
			index++;
			if (index >= decoder.getFrameCount()) {
				index = 0;
			}
		}
		Bitmap bitmap = decoder.getFrame(index);
		if (bitmap != null)
			canvas.drawBitmap(bitmap, 0, 0, null);

		invalidate();
	}
}