package com.livae.ff.app.utils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

public class TextUtils {

	private static final StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

	public static SpannableString setBoldText(CharSequence text, CharSequence boldText) {
		final SpannableString spannableString = new SpannableString(text);
		if (boldText != null && boldText.length() > 0) {
			int pos = text.toString().indexOf(boldText.toString());
			if (pos >= 0) {
				spannableString.setSpan(boldSpan, pos, pos + boldText.length(),
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannableString;
	}

}
