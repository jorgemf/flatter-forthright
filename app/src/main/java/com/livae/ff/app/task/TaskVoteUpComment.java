package com.livae.ff.app.task;

import android.support.v4.util.Pair;

import com.livae.apphunt.api.apphunt.model.Comment;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.api.Model;
import com.livae.apphunt.app.async.NetworkAsyncTask;

public class TaskVoteUpComment extends NetworkAsyncTask<Pair<Long, Integer>, Comment> {

	@Override
	protected Comment doInBackground(Pair<Long, Integer> param) throws Exception {
		Comment comment = API.comment().upvoteComment(param.first).execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}