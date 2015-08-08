package com.livae.ff.app.utils;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.support.annotation.AttrRes;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class AnimUtils {

	public static void backgroundAnimation(View view,
										   @AttrRes int initialColorResId,
										   @AttrRes int finalColorResId,
										   long duration,
										   long delay) {
		TypedValue a = new TypedValue();
		Resources.Theme theme = view.getContext().getTheme();
		theme.resolveAttribute(initialColorResId, a, true);
		int startColor = a.data;
		theme.resolveAttribute(finalColorResId, a, true);
		int endColor = a.data;
		ObjectAnimator colorAnimation;
		colorAnimation =
		  ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), startColor,
								  endColor);
		colorAnimation.setDuration(duration);
		colorAnimation.setStartDelay(delay);
		colorAnimation.start();
	}

	public static Builder build(View view) {
		return new Builder(view);
	}

	public static class Builder {

		private static final AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();

		private static final DecelerateInterpolator DECELERATE = new DecelerateInterpolator();

		private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE =
		  new AccelerateDecelerateInterpolator();

		private View view;

		private Float alphaFrom;

		private Float alphaTo;

		private Float scaleXFrom;

		private Float scaleXTo;

		private Float scaleYFrom;

		private Float scaleYTo;

		private Integer translateXFrom;

		private Integer translateXTo;

		private Integer translateYFrom;

		private Integer translateYTo;

		private Animator.AnimatorListener listener;

		private ViewPropertyAnimator animator;

		public Builder(View view) {
			this.view = view;
			if (view != null) {
				view.clearAnimation();
				animator = view.animate();
			}
		}

		public Builder alpha(float from, float to) {
			if (view != null) {
				alphaFrom = from;
				alphaTo = to;
				view.setAlpha(from);
				animator.alpha(to);
			}
			return this;
		}

		public Builder scale(float from, float to) {
			if (view != null) {
				scaleXFrom = from;
				scaleXTo = to;
				view.setScaleX(from);
				animator.scaleX(to);
				scaleYFrom = from;
				scaleYTo = to;
				view.setScaleY(from);
				animator.scaleY(to);
			}
			return this;
		}

		public Builder scaleX(float from, float to) {
			if (view != null) {
				scaleXFrom = from;
				scaleXTo = to;
				view.setScaleX(from);
				animator.scaleX(to);
			}
			return this;
		}

		public Builder scaleY(float from, float to) {
			if (view != null) {
				scaleYFrom = from;
				scaleYTo = to;
				view.setScaleY(from);
				animator.scaleY(to);
			}
			return this;
		}

		public Builder translateX(int from, int to) {
			if (view != null) {
				translateXFrom = from;
				translateXTo = to;
				view.setTranslationX(from);
				animator.translationX(to);
			}
			return this;
		}

		public Builder translateY(int from, int to) {
			if (view != null) {
				translateYFrom = from;
				translateYTo = to;
				view.setTranslationY(from);
				animator.translationY(to);
			}
			return this;
		}

		public Builder setListener(Animator.AnimatorListener listener) {
			this.listener = listener;
			return this;
		}

		public Builder setDuration(long duration) {
			if (view != null) {
				animator.setDuration(duration);
			}
			return this;
		}

		public Builder setInterpolator(Interpolator interpolator) {
			if (view != null) {
				animator.setInterpolator(interpolator);
			}
			return this;
		}

		public Builder accelerate() {
			if (view != null) {
				animator.setInterpolator(ACCELERATE);
			}
			return this;
		}

		public Builder decelerate() {
			if (view != null) {
				animator.setInterpolator(DECELERATE);
			}
			return this;
		}

		public Builder accelerateDecelerate() {
			if (view != null) {
				animator.setInterpolator(ACCELERATE_DECELERATE);
			}
			return this;
		}

		public void start() {
			start(0);
		}

		public void start(long delay) {
			if (view != null) {
				animator.setListener(new AnimatorListenerUtil(this)).setStartDelay(delay);
				view.postDelayed(new Runnable() {
					@Override
					public void run() {
						animator.start();
					}
				}, 50);
			}
		}

	}

	static class AnimatorListenerUtil implements Animator.AnimatorListener {

		private Builder builder;

		AnimatorListenerUtil(Builder animationBuilder) {
			this.builder = animationBuilder;
		}

		@Override
		public void onAnimationStart(Animator animation) {
			if (builder.listener != null) {
				builder.listener.onAnimationStart(animation);
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			finishAnimation(animation);
			if (builder.listener != null) {
				builder.listener.onAnimationEnd(animation);
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			finishAnimation(animation);
			if (builder.listener != null) {
				builder.listener.onAnimationCancel(animation);
			}
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			if (builder.listener != null) {
				builder.listener.onAnimationRepeat(animation);
			}
		}

		public void finishAnimation(Animator animation) {
			final View view = builder.view;
			view.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (builder.alphaTo != null) {
						view.setAlpha(builder.alphaTo);
					}
					if (builder.scaleXTo != null) {
						view.setScaleX(builder.scaleXTo);
					}
					if (builder.scaleYTo != null) {
						view.setScaleY(builder.scaleYTo);
					}
					if (builder.translateXTo != null) {
						view.setTranslationX(builder.translateXTo);
					}
					if (builder.translateYTo != null) {
						view.setTranslationY(builder.translateYTo);
					}
				}
			}, 50);
		}
	}

}
