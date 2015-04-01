package com.livae.ff.app.task;

import com.livae.apphunt.api.apphunt.Apphunt.CommentEndpoint.UpdateComment;
import com.livae.apphunt.api.apphunt.model.Comment;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.api.Model;
import com.livae.apphunt.app.async.NetworkAsyncTask;

public class TaskUpdateComment extends NetworkAsyncTask<TextId, Comment> {

	@Override
	protected Comment doInBackground(TextId params) throws Exception {
		UpdateComment request = API.comment().updateComment(params.getId(), params.getText());

		Comment comment = request.execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}