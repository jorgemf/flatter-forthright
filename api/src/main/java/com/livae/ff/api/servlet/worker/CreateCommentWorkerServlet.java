package com.livae.ff.api.servlet.worker;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.Conversation;
import com.livae.ff.api.model.PhoneUser;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.ff.api.OfyService.ofy;

public class CreateCommentWorkerServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	  throws IOException {
		String idString = request.getParameter("id");
		if (idString == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Long id = Long.parseLong(idString);
		Comment comment = Comment.get(id);
		if (comment == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		Conversation conversation = Conversation.get(comment.getConversationId());
		switch (conversation.getType()) {
			case PRIVATE:
			case SECRET:
				createNotificationsPrivate(comment, conversation);
				break;
			case PRIVATE_ANONYMOUS:
				createNotificationsPrivateAnonymous(comment, conversation);
				break;
			case FLATTER:
			case FORTHRIGHT:
				createNotificationsPublic(comment, conversation);
				break;
		}
	}

	private void createNotificationsPrivate(Comment comment, Conversation conversation) {
		boolean updateComment = false;
		Long userPhone = comment.getUserId();
		final Long commentId = comment.getId();
		for (Long phone : conversation.getUsers()) {
			PhoneUser user = PhoneUser.get(phone);
			if (!user.isBlockedPhone(userPhone)) {
				notify(user.getPhone(), commentId);
				comment.increaseNotifyTo();
				updateComment = true;
			}
		}
		if (updateComment) {
			ofy().save().entity(comment).now();
		}
	}

	private void createNotificationsPrivateAnonymous(Comment comment, Conversation conversation) {
		boolean updateComment = false;
		Long userPhone = comment.getUserId();
		final Long commentId = comment.getId();
		for (Long phone : conversation.getUsers()) {
			PhoneUser user = PhoneUser.get(phone);
			if (!user.isBlockedAnonymousPhone(userPhone)) {
				notify(user.getPhone(), commentId);
				comment.increaseNotifyTo();
				updateComment = true;
			}
		}
		if (updateComment) {
			ofy().save().entity(comment).now();
		}
	}

	@SuppressWarnings("MethodWithMultipleLoops")
	private void createNotificationsPublic(Comment comment, Conversation conversation) {
		// notify the creator of the comment and the user of the conversation as individual push
		// notifications
		Long commentUserId = comment.getUserId();
		Long conversationUserId = conversation.getPhone();
		final Long commentId = comment.getId();
		notify(commentUserId, commentId);
		if (!commentUserId.equals(conversationUserId)) {
			notify(conversationUserId, commentId);
		}
		// notify other users
		notify(commentId);
	}

	private void notify(Long userId, Long commentId) {
		Queue queue = QueueFactory.getQueue("send-push-notifications-queue");
		queue.add(TaskOptions.Builder.withUrl("/sendpushcommentnotificationworker?userId=" +
											  userId + "&commentId=" + commentId)
									 .method(TaskOptions.Method.GET));
	}

	private void notify(Long commentId) {
		Queue queue = QueueFactory.getQueue("send-push-notifications-queue");
		queue.add(TaskOptions.Builder.withUrl("/sendpushcommentnotificationworker?commentId=" +
											  commentId).method(TaskOptions.Method.GET));
	}

}
