package com.livae.ff.app.task;

import android.support.v4.util.Pair;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskCommentVoteDisagree extends NetworkAsyncTask<Pair<Long, Integer>, Comment> {

	@Override
	protected Comment doInBackground(Pair<Long, Integer> param)
	  throws Exception {
		Comment comment = API.endpoint().disagreeComment(param.first).execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}
