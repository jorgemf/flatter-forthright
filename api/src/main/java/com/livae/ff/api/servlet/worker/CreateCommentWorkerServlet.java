package com.livae.ff.api.servlet.worker;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.Conversation;
import com.livae.ff.api.model.PhoneUser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
		Set<Long> usersToNotify = new HashSet<>();
		for (Long phone : conversation.getUsersNotification()) {
			PhoneUser user = PhoneUser.get(phone);
			usersToNotify.add(user.getPhone());
		}
		PhoneUser conversationUser = PhoneUser.get(conversation.getPhone());
		if (conversationUser != null) {
			usersToNotify.add(conversationUser.getPhone());
		}
// do not notify to last commenters
//		List<Comment> latest;
//		latest = ofy().load().type(Comment.class).filter("conversationId", conversation.getId())
//					  .order("-date").limit(Settings.NOTIFY_PUBLIC_COMMENTS_LAST_COMMENTERS)
// .list();
//		for (Comment latestComment : latest) {
//			usersToNotify.add(latestComment.getUserId());
//		}
		final Long commentId = comment.getId();
		for (Long phone : usersToNotify) {
			notify(phone, commentId);
		}
	}

	private void notify(Long userId, Long commentId) {
		Queue queue = QueueFactory.getQueue("send-push-notifications-queue");
		queue.add(TaskOptions.Builder.withUrl("/sendpushcommentnotificationworker?userId=" +
											  userId + "&commentId=" + commentId)
									 .method(TaskOptions.Method.GET));
	}

}
