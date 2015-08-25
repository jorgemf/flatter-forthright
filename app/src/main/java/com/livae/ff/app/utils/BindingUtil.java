package com.livae.ff.app.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class BindingUtil {

	@BindingAdapter({"bind:imageUrl"})
	public static void loadImage(ImageView view, String url) {
		Picasso.with(view.getContext()).load(url).into(view);
	}

	@BindingAdapter({"bind:imageUrl", "bind:imageNull"})
	public static void loadImage(ImageView view, String url, Drawable image) {
		if (TextUtils.isEmpty(url)) {
			view.setImageDrawable(image);
		} else {
			Picasso.with(view.getContext()).load(url).into(view);
		}
	}

	@BindingAdapter({"bind:imageUrlRounded"})
	public static void loadImageRounded(ImageView view, String url) {
		Picasso.with(view.getContext()).load(url).transform(new CircleTransform()).into(view);
	}

	@BindingAdapter({"bind:imageUrlRounded", "bind:imageNull"})
	public static void loadImageRounded(ImageView view, String url, Drawable image) {
		if (TextUtils.isEmpty(url)) {
			view.setImageDrawable(image);
		} else {
			Picasso.with(view.getContext()).load(url).transform(new CircleTransform()).into(view);
		}
	}

	@BindingAdapter({"bind:selected"})
	public static void setSelected(View view, Boolean selected) {
		if (selected != null) {
			view.setSelected(selected);
		} else {
			view.setSelected(false);
		}
	}

	@BindingAdapter({"bind:visible"})
	public static void setVisible(View view, Boolean visible) {
		if (visible != null) {
			view.setVisibility(visible ? View.VISIBLE : View.GONE);
		} else {
			view.setVisibility(View.VISIBLE);
		}
	}

	public static class CircleTransform implements Transformation {

		@Override
		public Bitmap transform(Bitmap source) {
			int size = Math.min(source.getWidth(), source.getHeight());

			int x = (source.getWidth() - size) / 2;
			int y = (source.getHeight() - size) / 2;

			Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
			if (squaredBitmap != source) {
				source.recycle();
			}

			Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

			Canvas canvas = new Canvas(bitmap);
			Paint paint = new Paint();
			BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP,
												   BitmapShader.TileMode.CLAMP);
			paint.setShader(shader);
			paint.setAntiAlias(true);

			float r = size / 2f;
			canvas.drawCircle(r, r, r, paint);

			squaredBitmap.recycle();
			return bitmap;
		}

		@Override
		public String key() {
			return "circle";
		}
	}
}
