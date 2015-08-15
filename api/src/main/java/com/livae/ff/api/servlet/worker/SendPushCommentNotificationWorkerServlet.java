package com.livae.ff.api.servlet.worker;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livae.ff.api.Settings;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.Conversation;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.util.NotificationsUtil;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.PushNotificationType;
import com.livae.ff.common.model.NotificationComment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.ff.api.OfyService.ofy;

public class SendPushCommentNotificationWorkerServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	  throws IOException {
		String userIdString = request.getParameter("userId");
		String commentIdString = request.getParameter("commentId");
		if (commentIdString == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		Comment comment = Comment.get(Long.parseLong(commentIdString));
		if (comment == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		try {
			if (userIdString != null) {
				PhoneUser user = PhoneUser.get(Long.parseLong(userIdString));
				if (user == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
					return;
				}
				sendPushToOneUser(comment, user);
			} else {
				sendPushToPublicUsers(comment);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void sendPushToOneUser(Comment comment, PhoneUser user)
	  throws IOException, InternalServerErrorException {
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
	}

	private void sendPushToPublicUsers(Comment comment)
	  throws IOException, InternalServerErrorException {
		Conversation conversation = Conversation.get(comment.getConversationId());
		// users already notified
		Long commentUserId = comment.getUserId();
		Long conversationUserId = conversation.getPhone();
		// other users
		// users in the conversation
		Set<Long> usersToNotify = new HashSet<>();
		for (Long phone : conversation.getUsersNotification()) {
			if (phone != null && !phone.equals(commentUserId) &&
				!phone.equals(conversationUserId)) {
				usersToNotify.add(phone);
			}
		}
		// last commenters
		List<Comment> latest = ofy().load()
									.type(Comment.class)
									.filter("conversationId", conversation.getId())
									.order("-date")
									.limit(Settings.NOTIFY_PUBLIC_LAST_COMMENTERS)
									.list();
		for (Comment latestComment : latest) {
			Long phone = latestComment.getUserId();
			if (phone != null && !phone.equals(commentUserId) &&
				!phone.equals(conversationUserId)) {
				usersToNotify.add(phone);
			}
		}
		// get devicesIds
		List<String> devicesIds = new ArrayList<>();
		for (Long phone : usersToNotify) {
			PhoneUser user = PhoneUser.get(phone);
			if (user != null) {
				String deviceId = user.getDeviceId();
				if (deviceId != null) {
					devicesIds.add(deviceId);
				}
			}
		}
		NotificationsUtil.sendMulticastPushNotification(devicesIds,
														createNotification(comment, conversation),
														PushNotificationType.COMMENT);
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
