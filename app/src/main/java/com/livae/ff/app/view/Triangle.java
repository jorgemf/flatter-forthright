package com.livae.ff.app.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import javax.annotation.Nonnull;

public class Triangle extends View {

	private int color;

	private int gravity;

	private Paint paint;

	private Path path;

	public Triangle(Context context) {
		this(context, null);
	}

	public Triangle(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Triangle(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public Triangle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);

		int[] attrStyleable = new int[]{android.R.attr.color, android.R.attr.gravity};
		TypedArray a = context.obtainStyledAttributes(attrs, attrStyleable);
		color = a.getColor(android.R.attr.color, Color.BLACK);
		gravity = a.getInteger(android.R.attr.gravity, Gravity.LEFT);
		a.recycle();
		paint.setColor(color);
	}

	@Override
	public void draw(@Nonnull Canvas canvas) {
		super.draw(canvas);
		path.reset();
		final int width = getWidth();
		final int height = getHeight();
		switch (gravity) {
			case Gravity.RIGHT:
				path.moveTo(0, 0);
				path.lineTo(width, 0);
				path.lineTo(0, height);
				break;
			default:
			case Gravity.LEFT:
				path.moveTo(width, 0);
				path.lineTo(0, 0);
				path.lineTo(width, height);
				break;
		}
		path.close();
		canvas.drawPath(path, paint);
	}
}
