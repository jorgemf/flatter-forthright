package com.livae.ff.app.task;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.apphunt.api.apphunt.Apphunt.CommentEndpoint.AddComment;
import com.livae.apphunt.api.apphunt.model.Comment;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.api.Model;
import com.livae.apphunt.app.async.NetworkAsyncTask;

import org.apache.http.HttpStatus;

public class TaskPostComment extends NetworkAsyncTask<TextId, Comment> {

	@Override
	protected Comment doInBackground(TextId params) throws Exception {
		AddComment request;
		try {
			request = API.comment().addComment(params.getId(), params.getText());
		} catch (GoogleJsonResponseException exception) {
			if (exception.getDetails().getCode() == HttpStatus.SC_FORBIDDEN) {
				Application.appUser().setCommentsLeft(0);
			}
			throw exception;
		}
		Comment comment = request.execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}