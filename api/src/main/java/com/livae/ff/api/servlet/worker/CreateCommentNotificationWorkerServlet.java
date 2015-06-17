package com.livae.ff.api.servlet.worker;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.livae.ff.api.model.Comment;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CreateCommentNotificationWorkerServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		createNotifications(comment);
	}

	private void createNotifications(Comment comment) {
		// create notifications
		long currentUserId = comment.getUser().getId();
		long now = comment.getDate().getTime();
		ApplicationEntry applicationEntry = comment.getApplicationEntry();
		long appEntryId = comment.getApplicationEntryId();
		Set<Long> usersIds = new HashSet<>();
		for (Comment c : ofy().load().type(Comment.class).filter("applicationEntryId", appEntryId)
							  .order("-date").orderKey(true)
							  .limit(Settings.NOTIFICATIONS_COMMENTS_MAX_USERS)) {
			long commentUserId = c.getUserId();
			if (commentUserId != currentUserId) {
				usersIds.add(commentUserId);
			}
		}
		long appSharedUserId = applicationEntry.getUserSharedId();
		long maxDate = applicationEntry.getDate().getTime() + Settings.NOTIFICATIONS_NOTIFY_POSTER;
		if (appSharedUserId != currentUserId && now < maxDate) {
			usersIds.add(appSharedUserId);
		}
		long maxTimeNotification = now - Settings.NOTIFICATIONS_CREATE_NEW_NOTIFICATION;
		Date dateNow = new Date(now);
		for (long userId : usersIds) {
			// get last notification for the app entry
			Notification notification;
			notification = ofy().load().type(Notification.class).filter("type",
																		Constants.NotificationType.APP_COMMENTED)
								.filter("userId", userId).filter("applicationEntryId", appEntryId)
								.order("-updated").first().now();
			if (notification == null || notification.getUpdated().getTime() < maxTimeNotification) {
				notification = new Notification(userId, appEntryId, currentUserId);
				notification.setUpdated(dateNow);
			} else {
				notification.setUpdated(dateNow);
				notification.setLastUserCommentedId(currentUserId);
				notification.setNewComments(notification.getNewComments() + 1);
				notification.setNotified(false);
			}
			ofy().save().entity(notification).now();
			String idString = Long.toString(notification.getId());
			Queue queue = QueueFactory.getQueue("send-push-notifications-queue");
			queue.add(TaskOptions.Builder.withUrl("/sendpushnotificationworker?id=" + idString)
										 .method(TaskOptions.Method.GET));
		}
	}

}
