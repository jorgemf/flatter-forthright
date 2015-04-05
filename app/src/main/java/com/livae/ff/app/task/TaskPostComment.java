package com.livae.ff.app.task;

import android.support.v4.util.Pair;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.common.Constants.CommentType;

public class TaskPostComment extends NetworkAsyncTask<Pair<TextId, CommentType>, Comment> {

	@Override
	protected Comment doInBackground(Pair<TextId, CommentType> params) throws Exception {
		TextId textId = params.first;
		CommentType commentType = params.second;
		Comment comment = API.endpoint().postComment(textId.getId(), commentType.name(),
													 textId.getText()).execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}