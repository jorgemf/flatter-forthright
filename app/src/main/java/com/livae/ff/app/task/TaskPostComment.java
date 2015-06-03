package com.livae.ff.app.task;

import android.support.v4.util.Pair;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.common.Constants.ChatType;

public class TaskPostComment extends NetworkAsyncTask<Pair<TextId, ChatType>, Comment> {

	@Override
	protected Comment doInBackground(Pair<TextId, ChatType> params) throws Exception {
		TextId textId = params.first;
		ChatType chatType = params.second;
		Comment comment = API.endpoint().postComment(textId.getId(), chatType.name(),
													 textId.getText()).execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}