package com.livae.ff.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.R;
import com.livae.ff.app.settings.Settings;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.IOException;

import javax.annotation.Nonnull;

public class ImageUtils {

	public static Bitmap loadAppImage(@Nonnull Context context, String imageUrl) {
		if (imageUrl != null) {
			imageUrl += "?size=256";
		}
		imageUrl = parseUrl(imageUrl, context);
		Bitmap image = null;
		try {
			image = Picasso.with(context).load(imageUrl).config(Bitmap.Config.RGB_565).get();
		} catch (IOException ignore) {
		}
		return image;
	}

	public static Bitmap loadUserImage(@Nonnull Context context, String imageUrl) {
		if (imageUrl != null) {
			imageUrl = imageUrl.replace("?sz=512", "?sz=256");
		}
		imageUrl = parseUrl(imageUrl, context);
		Bitmap image = null;
		try {
			image = Picasso.with(context)
						   .load(imageUrl)
						   .transform(new CircleTransformPicasso())
						   .config(Bitmap.Config.RGB_565)
						   .get();
		} catch (IOException ignore) {
		}
		return image;
	}

	public static void loadUserImage(@Nonnull ImageView imageView, String imageUrl) {
		if (imageUrl != null) {
			imageUrl = imageUrl.replace("?sz=512", "?sz=256");
		}
		loadDefault(imageView, imageUrl, R.drawable.ic_account_circle_white_48dp, true, true, 256);
	}

	public static void loadDefault(@Nonnull ImageView imageView,
								   String imageUrl,
								   @DrawableRes int placeholderResId,
								   boolean round,
								   boolean fade,
								   Integer maxSize) {
		Context context = imageView.getContext();
		if (imageUrl != null) {
			imageUrl = parseUrl(imageUrl, context);
			RequestCreator requestCreator = Picasso.with(context).load(imageUrl);
			if (placeholderResId != 0) {
				requestCreator = requestCreator.placeholder(placeholderResId);
			}
			if (round) {
				requestCreator = requestCreator.transform(new CircleTransformPicasso());
			}
			if (!fade) {
				requestCreator = requestCreator.noFade();
			}
			if (maxSize != null && maxSize > 0) {
				requestCreator = requestCreator.resize(maxSize, maxSize);
			}

			requestCreator = requestCreator.config(Bitmap.Config.RGB_565);
			if (BuildConfig.DEBUG) {
				requestCreator.into(imageView, new TimerCallback(imageUrl));
			} else {
				requestCreator.into(imageView);
			}
		} else {
			if (placeholderResId != 0) {
				imageView.setImageResource(placeholderResId);
			} else if (round) {
				imageView.setImageResource(R.drawable.anom_user);
			} else {
				TypedValue a = new TypedValue();
				Resources.Theme theme = imageView.getContext().getTheme();
				theme.resolveAttribute(R.color.amber_light, a, true);
				imageView.setImageResource(a.resourceId);
			}
		}
	}

	private static String parseUrl(String imageUrl, Context context) {
		if (!imageUrl.contains("://")) {
			//noinspection PointlessBooleanExpression,ConstantConditions
			if (BuildConfig.DEV) {
				String newImageUrl = Settings.SERVER_URL + imageUrl;
				SharedPreferences sharedPreferences;
				sharedPreferences =
				  context.getSharedPreferences(Settings.PREFERENCES_DEBUG, Context.MODE_PRIVATE);
				if (sharedPreferences.contains(Settings.PREFERENCE_API_IP)) {
					String ip = sharedPreferences.getString(Settings.PREFERENCE_API_IP, null);
					newImageUrl = "http://" + ip + ":8080" + imageUrl;
				}
				imageUrl = newImageUrl;
			} else {
				imageUrl = Settings.SERVER_URL + imageUrl;
			}
		}
		return imageUrl;
	}

	public static Bitmap getCircularBitmapImage(Bitmap source) {
		int size = Math.min(source.getWidth(), source.getHeight());
		int x = (source.getWidth() - size) / 2;
		int y = (source.getHeight() - size) / 2;
		Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
		if (squaredBitmap != source) {
			source.recycle();
		}
		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		BitmapShader shader =
		  new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode
																		 .CLAMP);
		paint.setShader(shader);
		paint.setAntiAlias(true);
		float r = size / 2f;
		canvas.drawCircle(r, r, r, paint);
		squaredBitmap.recycle();
		return bitmap;
	}

	static final class TimerCallback implements Callback {

		private long time;

		private String imageUrl;

		TimerCallback(String imageUrl) {
			time = System.currentTimeMillis();
			this.imageUrl = imageUrl;
		}

		@Override
		public void onSuccess() {
			long elapsedTime = System.currentTimeMillis() - time;
			if (elapsedTime > 50) {
				Log.v("PROFILING_IMAGES", this.imageUrl + " " + elapsedTime);
			}
		}

		@Override
		public void onError() {

		}
	}

	static class CircleTransformPicasso implements Transformation {

		@Override
		public Bitmap transform(Bitmap source) {
			return getCircularBitmapImage(source);
		}

		@Override
		public String key() {
			return "Picasso_Circle_Transformation";
		}
	}
}
