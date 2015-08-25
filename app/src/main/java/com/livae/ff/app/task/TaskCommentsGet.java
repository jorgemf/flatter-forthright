package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.api.ff.Ff.ApiEndpoint.GetComments;
import com.livae.ff.api.ff.model.CollectionResponseComment;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.ui.fragment.AbstractFragment;

public class TaskCommentsGet extends NetworkAsyncTask<AbstractFragment, QueryParam, ListResult> {

	public TaskCommentsGet(@NonNull AbstractFragment lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected ListResult doInBackground(QueryParam queryParams)
	  throws Exception {
		if (BuildConfig.TEST) {
			return new ListResult(null, 20);
		} else {
			Long id = ((QueryId) queryParams).getId();
			GetComments request = API.endpoint().getComments(id);
			if (queryParams.getLimit() != null) {
				request.setLimit(queryParams.getLimit());
			}
			if (queryParams.getCursor() != null) {
				request.setCursor(queryParams.getCursor());
			}
			CollectionResponseComment comments = request.execute();
			Model model = Application.model();
			model.parse(comments);
			model.save();
			int size = comments.getItems() != null ? comments.getItems().size() : 0;
			return new ListResult(comments.getNextPageToken(), size);
		}
	}
}
