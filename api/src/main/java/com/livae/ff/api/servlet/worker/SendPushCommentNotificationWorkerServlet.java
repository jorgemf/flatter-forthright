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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			comment.setIsMe(user.getPhone().equals(comment.getUserId()));
			Conversation conversation = Conversation.get(comment.getConversationId());
			NotificationsUtil.sendPushNotification(user, createNotification(comment, conversation),
												   PushNotificationType.COMMENT);
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
		notificationComment.setDate(comment.getDate());
		if (comment.getUserMark() != null) {
			notificationComment.setUserMark(comment.getUserMark().name());
		}
		final Constants.ChatType chatType = conversation.getType();
		notificationComment.setConversationType(chatType.name());
		notificationComment.setConversationId(conversation.getId());
		switch (chatType) {
			case FLATTER:
			case FORTHRIGHT:
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
