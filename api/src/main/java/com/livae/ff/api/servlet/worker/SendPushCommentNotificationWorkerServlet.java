package com.livae.ff.api.servlet.worker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.Conversation;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.util.NotificationsUtil;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.PushNotificationType;
import com.livae.ff.common.model.NotificationComment;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.ff.api.OfyService.ofy;

public class SendPushCommentNotificationWorkerServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String userIdString = request.getParameter("userId");
		String commentIdString = request.getParameter("commentId");
		if (userIdString == null || commentIdString == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		PhoneUser user = PhoneUser.get(Long.parseLong(userIdString));
		Comment comment = Comment.get(Long.parseLong(commentIdString));
		if (user == null || comment == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		try {
			boolean isMe = user.getPhone().equals(comment.getUserId());
			comment.setIsMe(isMe);
			Conversation conversation = Conversation.get(comment.getConversationId());
			NotificationsUtil.sendPushNotification(user, createNotification(comment, conversation),
												   PushNotificationType.COMMENT);
			switch (conversation.getType()) {
				case PRIVATE:
				case PRIVATE_ANONYMOUS:
				case SECRET:
					comment.decreaseNotifyTo();
					if (comment.getNotifyTo() == null) {
						ofy().delete().entity(comment);
					} else {
						ofy().save().entity(comment);
					}
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	private String createNotification(Comment comment, Conversation conversation) {
		NotificationComment notificationComment = new NotificationComment();
		notificationComment.setId(comment.getId());
		notificationComment.setIsMe(comment.getIsMe());
		notificationComment.setComment(comment.getComment());
		notificationComment.setDate(comment.getDate().getTime());
		if (comment.getUserMark() != null) {
			notificationComment.setUserMark(comment.getUserMark().name());
		}
		final Constants.ChatType chatType = conversation.getType();
		notificationComment.setConversationType(chatType.name());
		notificationComment.setConversationId(conversation.getId());
		switch (chatType) {
			case FORTHRIGHT:
				final boolean isMe = comment.getIsMe();
				PhoneUser phoneUser = PhoneUser.get(conversation.getPhone());
				final Date date = phoneUser.getForthrightChatsDateBlocked();
				final Long dateBlocked = date == null ? null : date.getTime();
				if (!isMe && dateBlocked != null && comment.getDate().getTime() > dateBlocked) {
					// hide comments since the user blocked the list
					notificationComment.setComment(null);
				}
				// no break
			case FLATTER:
				notificationComment.setConversationUserId(conversation.getPhone());
				notificationComment.setAlias(comment.getAlias());
				notificationComment.setAliasId(comment.getAliasId());
				break;
			case PRIVATE_ANONYMOUS:
				notificationComment.setAlias(conversation.getAlias());
				notificationComment.setAliasId(conversation.getAliasId());
				break;
			case PRIVATE:
			case SECRET:
				notificationComment.setUserId(comment.getUserId());
				break;
		}

		Gson gson = new GsonBuilder().create();
		return gson.toJson(notificationComment);
	}
}
