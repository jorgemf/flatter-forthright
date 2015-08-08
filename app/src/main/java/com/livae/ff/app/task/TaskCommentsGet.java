package com.livae.ff.app.task;

import com.livae.ff.api.ff.Ff.ApiEndpoint.GetComments;
import com.livae.ff.api.ff.model.CollectionResponseComment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskCommentsGet extends NetworkAsyncTask<QueryId, ListResult> {

	@Override
	protected ListResult doInBackground(QueryId queryParams)
	  throws Exception {
		GetComments request = API.endpoint().getComments(queryParams.getId());
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
