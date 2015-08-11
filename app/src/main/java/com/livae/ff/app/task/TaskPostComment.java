package com.livae.ff.app.task;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.utils.SyncUtils;

public class TaskPostComment extends CustomAsyncTask<TextId, Comment> {

	@Override
	protected Comment doInBackground(TextId params)
	  throws Exception {
		if(BuildConfig.TEST){
			return null;
		}else {
			Comment comment = null;
			Model model = Application.model();
// this code is too slow
//		if (DeviceUtils.isNetworkAvailable(Application.getContext())) {
//			// try to send the comment now
//			try {
//				Ff.ApiEndpoint.PostComment request;
//				request = API.endpoint().postComment(params.getId(), params.getText());
//				request.setAlias(params.getAlias());
//				comment = request.execute();
//				comment.setIsMe(true);
//				model.parse(comment);
//			} catch (Exception ignore) {
//
//			}
//		}
//		if (comment == null) {
//			// if comment wasn't sent, try now again
//			comment = saveCommentForSync(params);
//			model.parse(comment, true);
//			model.save();
//			SyncUtils.syncConversationsNow();
//		}
			comment = saveCommentForSync(params);
			model.parse(comment, true);
			model.save();
			SyncUtils.syncConversationsNow();
			return comment;
		}
	}

	private Comment saveCommentForSync(TextId params) {
		Comment comment = new Comment();
		comment.setAlias(params.getAlias());
		comment.setConversationId(params.getId());
		comment.setComment(params.getText().getText());
		return comment;
	}
}
