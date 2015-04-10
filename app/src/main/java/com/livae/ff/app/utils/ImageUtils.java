package com.livae.ff.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.IntegerRes;
import android.widget.ImageView;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.Settings;

import javax.annotation.Nonnull;

public class ImageUtils {

	public static void loadDefault(@Nonnull ImageView imageView, String imageUrl) {
		loadDefault(imageView, imageUrl, 0, false, false);
	}

	public static void loadDefaultRound(@Nonnull ImageView imageView, String imageUrl) {
		loadDefault(imageView, imageUrl, 0, true, false);
	}

	public static void loadDefault(@Nonnull ImageView imageView, String imageUrl,
								   @IntegerRes int placeholderResId, boolean round, boolean fade) {
		if (imageUrl != null) {
			Context context = imageView.getContext();
			if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
				//noinspection PointlessBooleanExpression,ConstantConditions
				if (BuildConfig.DEV) {
					String newImageUrl = Settings.SERVER_URL + imageUrl;
					SharedPreferences sharedPreferences;
					sharedPreferences = context.getSharedPreferences(Settings.PREFERENCES_DEBUG,
																	 Context.MODE_PRIVATE);
					if (sharedPreferences.contains(Settings.PREFERENCE_API_IP)) {
						String ip = sharedPreferences.getString(Settings.PREFERENCE_API_IP, null);
						newImageUrl = "http://" + ip + ":8080" + imageUrl;
					}
					imageUrl = newImageUrl;
				} else {
					imageUrl = Settings.SERVER_URL + imageUrl;
				}
			}
			Debug.print(imageUrl);
			DrawableRequestBuilder builder = Glide.with(imageView.getContext()).load(imageUrl);
			builder = builder.diskCacheStrategy(DiskCacheStrategy.ALL);
			if (placeholderResId != 0) {
				builder = builder.placeholder(placeholderResId);
			}
			if (round) {
				builder = builder.transform(new CircleTransform(context));
			}
			if (fade) {
				builder = builder.crossFade();
			}
			builder.into(imageView);
		} else {
			imageView.setImageBitmap(null);
		}
	}

//	public static void loadDefault(@Nonnull SimpleDraweeView imageView, String imageUrl) {
////		imageView.setImageURI(null);
//		if (imageUrl != null) {
//			if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
//				//noinspection PointlessBooleanExpression,ConstantConditions
//				if (BuildConfig.DEV) {
//					String newImageUrl = Settings.SERVER_URL + imageUrl;
//					SharedPreferences sharedPreferences;
//					Context context = imageView.getContext();
//					sharedPreferences = context.getSharedPreferences(Settings.PREFERENCES_DEBUG,
//																	 Context.MODE_PRIVATE);
//					if (sharedPreferences.contains(Settings.PREFERENCE_API_IP)) {
//						String ip = sharedPreferences.getString(Settings.PREFERENCE_API_IP, null);
//						newImageUrl = "http://" + ip + ":8080" + imageUrl;
//					}
//					imageUrl = newImageUrl;
//				} else {
//					imageUrl = Settings.SERVER_URL + imageUrl;
//				}
//			}
//			Debug.print(imageUrl);
//			imageView.setImageURI(Uri.parse(imageUrl));
//		} else {
//			imageView.setImageBitmap(null);
//		}
//	}

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
		BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP,
											   BitmapShader.TileMode.CLAMP);
		paint.setShader(shader);
		paint.setAntiAlias(true);
		float r = size / 2f;
		canvas.drawCircle(r, r, r, paint);
		squaredBitmap.recycle();
		return bitmap;
	}

}
