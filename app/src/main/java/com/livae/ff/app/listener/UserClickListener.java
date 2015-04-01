package com.livae.ff.app.listener;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public interface UserClickListener {

	public void userClicked(Long userId, ImageView imageView, TextView name, TextView tagline,
							View cardView);

	public void userRelationshipClicked();

}
