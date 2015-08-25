package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.api.ff.Ff;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskCommentPost extends NetworkAsyncTask<LifeCycle, TextId, Comment> {

	public TaskCommentPost(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Comment doInBackground(TextId params)
	  throws Exception {
		Ff.ApiEndpoint.PostComment postComment;
		postComment = API.endpoint().postComment(params.getId(), params.getText());
		if (params.getAlias() != null) {
			postComment.setAlias(params.getAlias());
		}
		Comment comment = postComment.execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}
