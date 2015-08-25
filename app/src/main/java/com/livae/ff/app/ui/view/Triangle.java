package com.livae.ff.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.livae.ff.app.R;

import javax.annotation.Nonnull;

public class Triangle extends View {

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

		final Resources.Theme theme = context.getTheme();
		TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.Triangle, 0, 0);
		int color = Color.BLACK;
		try {
			color = a.getColor(R.styleable.Triangle_triangle_color, Color.BLACK);
		} finally {
			a.recycle();
		}
		paint.setColor(color);
	}

	@Override
	public void draw(@Nonnull Canvas canvas) {
		super.draw(canvas);
		path.reset();
		final int width = getWidth();
		final int height = getHeight();
		path.moveTo(0, 0);
		path.lineTo(width, 0);
		path.lineTo(width / 2, height);
		path.close();
		canvas.drawPath(path, paint);
	}
}
