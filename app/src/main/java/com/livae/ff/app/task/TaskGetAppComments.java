package com.livae.ff.app.task;

import com.livae.apphunt.api.apphunt.Apphunt.CommentEndpoint.GetComments;
import com.livae.apphunt.api.apphunt.model.CollectionResponseComment;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.api.Model;
import com.livae.apphunt.app.async.NetworkAsyncTask;

public class TaskGetAppComments extends NetworkAsyncTask<QueryParamId, ListResult> {

	@Override
	protected ListResult doInBackground(QueryParamId params) throws Exception {
		GetComments request = API.comment().getComments(params.getId());
		if (params.getOrder() != null) {
			request.setOrder(params.getOrder().name());
		}
		if (params.getLimit() != null) {
			request.setLimit(params.getLimit());
		}
		if (params.getCursor() != null) {
			request.setCursor(params.getCursor());
		}
		CollectionResponseComment comments = request.execute();
		Model model = Application.model();
		model.parse(comments);
		model.save();
		int size = comments.getItems() != null ? comments.getItems().size() : 0;
		return new ListResult(comments.getNextPageToken(), size);
	}
}