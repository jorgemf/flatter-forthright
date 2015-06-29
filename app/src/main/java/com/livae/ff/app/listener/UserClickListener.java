package com.livae.ff.app.listener;

import android.widget.ImageView;
import android.widget.TextView;

public interface UserClickListener {

	public void userClicked(Long userId, Long conversationId, TextView name, ImageView image);

}
