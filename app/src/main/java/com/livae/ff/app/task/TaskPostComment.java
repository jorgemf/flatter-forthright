package com.livae.ff.app.task;

import com.livae.ff.api.ff.Ff.ApiEndpoint.PostComment;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.utils.DeviceUtils;

public class TaskPostComment extends CustomAsyncTask<TextId, Comment> {

	@Override
	protected Comment doInBackground(TextId params) throws Exception {
		Comment comment;
		Model model = Application.model();
		if (DeviceUtils.isNetworkAvailable(Application.getContext())) {
			PostComment request;
			request = API.endpoint().postComment(params.getId(), params.getText());
			request.setAlias(params.getAlias());
			comment = request.execute();
			model.parse(comment);
		} else {
			comment = saveCommentForSync(params);
			model.parse(comment, true);
		}
		model.save();
		return comment;
	}

	private Comment saveCommentForSync(TextId params) {
		Comment comment = new Comment();
		comment.setAlias(params.getAlias());
		comment.setConversationId(params.getId());
		comment.setComment(params.getText().getText());
		return comment;
	}
}