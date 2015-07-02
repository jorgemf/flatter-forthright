package com.livae.ff.app.listener;

import android.widget.ImageView;
import android.widget.TextView;

public interface UserClickListener {

	public void userClicked(Long userId, Long conversationId, String userDisplayName,
							String conversationName, TextView name, ImageView image);

}
