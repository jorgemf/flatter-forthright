package com.livae.ff.app.task;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.api.ff.model.FlagText;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskCommentFlag extends NetworkAsyncTask<LifeCycle, Pair<FlagComment, Integer>, Void> {

	public TaskCommentFlag(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Void doInBackground(Pair<FlagComment, Integer> params)
	  throws Exception {
		FlagText flagText = new FlagText();
		FlagComment flagComment = params.first;
		flagText.setText(flagComment.getComment());
		flagText.setReason(flagComment.getReason().name());
		Comment comment =
		  API.endpoint().flagComment(flagComment.getCommentId(), flagText).execute();
		Model model = new Model(Application.getContext());
		model.parse(comment);
		model.save();
		return null;
	}

}
