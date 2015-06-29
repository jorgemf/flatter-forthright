package com.livae.ff.app.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class AnonymousImage extends View {

	public static final int[] COLOR_PALETTE = {0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
											   0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
											   0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
											   0xFFFFEB3B, 0xFFFFC107, 0xFFFF9800, 0xFFFF5722,
											   0xFF795548, 0xFF9E9E9E, 0xFF607D8B, 0xFF000000};

	private long seed;

	private Random random = new Random();

	private Paint paint;

	private Path path;

	private Rect bounds;

	public AnonymousImage(Context context) {
		super(context);
		init();
	}

	public AnonymousImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AnonymousImage(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public AnonymousImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);
		bounds = new Rect();
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public void setSeed(String text) {
		setSeed(text.hashCode());
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (seed != 0) {
			random.setSeed(seed);
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			int minX = -width / 2;
			int maxX = width + width / 2;
			int minY = -height / 2;
			int maxY = height + height / 2;
			// draw background
			canvas.getClipBounds(bounds);
			paint.setColor(getColor());
			canvas.drawRect(bounds, paint);
			// draw random shapes
			int shapesCounter = 0;
			final SHAPE[] shapes = SHAPE.values();
			do {
				SHAPE shape = shapes[random.nextInt(shapes.length)];
				switch (shape) {
					case CIRCLE: {
						int minRadius = Math.max(width, height) / 2;
						int maxRadius = Math.max(width, height);
						int centerX = random.nextInt(width * 2) + minX;
						int centerY = random.nextInt(height * 2) + minY;
						int radius = random.nextInt(maxRadius - minRadius) + minRadius;
						paint.setColor(getColor());
						canvas.drawCircle(centerX, centerY, radius, paint);
					}
					break;
					case SQUARE: {
						int x1 = random.nextInt(width * 2) + minX;
						int x2 = random.nextInt(width * 2) + minX;
						int y1 = random.nextInt(height * 2) + minY;
						int y2 = random.nextInt(height * 2) + minY;
						paint.setColor(getColor());
						canvas.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2),
										Math.max(y1, y2), paint);
					}
					break;
					case TRIANGLE: {
						int x1 = random.nextInt(width * 2) + minX;
						int y1 = random.nextInt(height * 2) + minY;
						int x2 = random.nextInt(width * 2) + minX;
						int y2 = random.nextInt(height * 2) + minY;
						int x3 = random.nextInt(width * 2) + minX;
						int y3 = random.nextInt(height * 2) + minY;
						path.reset();
						path.moveTo(x1, y1);
						path.lineTo(x2, y2);
						path.lineTo(x3, y3);
						path.close();
						canvas.drawPath(path, paint);
					}
					break;
				}
				shapesCounter++;
			} while (shapesCounter < 3 && random.nextBoolean());
		}
	}

	private int getPoint(int max, double maxVariation) {
		return (int) (random.nextInt(max) * (1 + (random.nextDouble() - 0.5) * maxVariation));
	}

	private int getColor() {
		return COLOR_PALETTE[random.nextInt(COLOR_PALETTE.length)];
	}

	public static enum SHAPE {CIRCLE, TRIANGLE, SQUARE}
}
