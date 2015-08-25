package com.livae.ff.app.task;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskCommentVoteAgree
  extends NetworkAsyncTask<LifeCycle, Pair<Long, Integer>, Comment> {

	public TaskCommentVoteAgree(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Comment doInBackground(Pair<Long, Integer> param)
	  throws Exception {
		Comment comment = API.endpoint().agreeComment(param.first).execute();
		Model model = Application.model();
		model.parse(comment);
		model.save();
		return comment;
	}
}
