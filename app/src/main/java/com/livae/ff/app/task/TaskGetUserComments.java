package com.livae.ff.app.task;

import android.support.v4.util.Pair;

import com.livae.ff.api.ff.Ff.ApiEndpoint.GetComments;
import com.livae.ff.api.ff.model.CollectionResponseComment;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.common.Constants.CommentType;

public class TaskGetUserComments
  extends NetworkAsyncTask<Pair<QueryParamId, CommentType>, ListResult> {

	@Override
	protected ListResult doInBackground(Pair<QueryParamId, CommentType> params) throws Exception {
		QueryParamId queryParamId = params.first;
		CommentType commentType = params.second;
		GetComments request = API.endpoint().getComments(queryParamId.getId(), commentType.name());
		if (queryParamId.getLimit() != null) {
			request.setLimit(queryParamId.getLimit());
		}
		if (queryParamId.getCursor() != null) {
			request.setCursor(queryParamId.getCursor());
		}
		CollectionResponseComment comments = request.execute();
		Model model = Application.model();
		model.parse(comments);
		model.save();
		int size = comments.getItems() != null ? comments.getItems().size() : 0;
		return new ListResult(comments.getNextPageToken(), size);
	}
}