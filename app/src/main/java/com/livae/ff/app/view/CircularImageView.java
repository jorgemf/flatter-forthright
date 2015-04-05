package com.livae.ff.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class CircularImageView extends ImageView {

	public CircularImageView(Context context) {
		this(context, null);
	}

	public CircularImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		Drawable roundedDrawable = getRoundedDrawable(drawable);
		if (roundedDrawable != null) {
			super.setImageDrawable(roundedDrawable);
		} else {
			super.setImageDrawable(drawable);
		}
	}

	private Drawable getRoundedDrawable(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		Drawable roundedDrawable = null;
		if (drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			if (bitmap != null) {
				roundedDrawable = new RoundedBitmapDrawable(bitmap);
			}
		} else if (drawable instanceof ColorDrawable) {
			int color = ((ColorDrawable) drawable).getColor();
			roundedDrawable = new RoundedColorDrawable(color);
		} else if (drawable instanceof TransitionDrawable) {
			TransitionDrawable transitionDrawable = ((TransitionDrawable) drawable);
			for (int i = 0; i < transitionDrawable.getNumberOfLayers(); i++) {
				if (transitionDrawable.getId(0) == View.NO_ID) {
					transitionDrawable.setId(0, 100 + i);
				}
			}
			for (int i = 0; i < transitionDrawable.getNumberOfLayers(); i++) {
				Drawable layerDrawable = transitionDrawable.getDrawable(i);
				Drawable newLayerDrawable = getRoundedDrawable(layerDrawable);
				transitionDrawable.setDrawableByLayerId(transitionDrawable.getId(i),
														newLayerDrawable);
			}
			roundedDrawable = transitionDrawable;
		} else if (drawable instanceof RoundedColorDrawable ||
				   drawable instanceof RoundedBitmapDrawable) {
			roundedDrawable = drawable;
		} else {
			roundedDrawable = drawable; // to avoid any crash
		}
		return roundedDrawable;
	}

	private class RoundedColorDrawable extends Drawable {

		private final int color;

		private final Paint paint;

		private RoundedColorDrawable(int color) {
			this.color = color;
			paint = new Paint();
			paint.setAntiAlias(true);
		}

		@Override
		public void draw(Canvas canvas) {
			int width = getWidth();
			int height = getHeight();
			paint.setColor(this.color);
			int radius = Math.min(width, height) / 2;
			canvas.drawCircle(width / 2, height / 2, radius, paint);
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

	private class RoundedBitmapDrawable extends Drawable {

		private final BitmapShader bitmapShader;

		private final Paint paint;

		private RoundedBitmapDrawable(Bitmap bitmap) {
			bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			paint = new Paint();
			paint.setAntiAlias(true);
		}

		@Override
		public void draw(Canvas canvas) {
			int width = getWidth();
			int height = getHeight();
			paint.setShader(bitmapShader);
			int radius = Math.min(width, height) / 2;
			canvas.drawCircle(width / 2, height / 2, radius, paint);
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
}
