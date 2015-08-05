package com.livae.ff.app.view;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class RoundedDrawable extends Drawable {

	private final BitmapShader bitmapShader;

	private final Paint paint;

	private final int x;

	private final int y;

	private final int radius;

	public RoundedDrawable(BitmapDrawable bitmapDrawable) {
		final Bitmap bitmap = bitmapDrawable.getBitmap();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		paint = new Paint();
		paint.setAntiAlias(true);
		x = width / 2;
		y = height / 2;
		radius = Math.min(width, height) / 2;
	}

	@Override
	public void draw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		paint.setShader(bitmapShader);
		float xScale = (float) (width) / (radius * 2);
		float yScale = (float) (height) / (radius * 2);
		canvas.translate((radius - x) * xScale, (radius - y) * yScale);
		canvas.scale(xScale, yScale);
		canvas.drawCircle(x, y, radius, paint);
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}
