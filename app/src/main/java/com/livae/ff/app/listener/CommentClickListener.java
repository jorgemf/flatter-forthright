package com.livae.ff.app.listener;

import com.livae.ff.app.viewholders.CommentViewHolder;

public interface CommentClickListener {

	public boolean onLongClick(CommentViewHolder holder);

	public void onClick(CommentViewHolder holder);

}
