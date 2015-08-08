package com.livae.ff.app.task;

import com.livae.ff.api.ff.Ff;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskCommentPost extends NetworkAsyncTask<TextId, Comment> {

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
