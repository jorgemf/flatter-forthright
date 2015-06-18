package com.livae.ff.api.servlet.worker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.util.NotificationsUtil;
import com.livae.ff.common.Constants.PushNotificationType;
import com.livae.ff.common.model.PushComment;

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
			NotificationsUtil.sendPushNotification(user, createNotification(comment),
												   PushNotificationType.COMMENT);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}

	private String createNotification(Comment comment) {
		PushComment pushComment = new PushComment();
		pushComment.setId(comment.getId());
		pushComment.setIsMe(comment.getIsMe());
		pushComment.setComment(comment.getComment());
		pushComment.setDate(comment.getDate());
		pushComment.setAlias(comment.getAlias());
		pushComment.setAliasId(comment.getAliasId());
		pushComment.setUserMark(comment.getUserMark());
		Gson gson = new GsonBuilder().create();
		return gson.toJson(pushComment);
	}
}
