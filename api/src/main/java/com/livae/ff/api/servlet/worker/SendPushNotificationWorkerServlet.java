package com.livae.ff.api.servlet.worker;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livae.apphunt.api.model.ApplicationEntry;
import com.livae.apphunt.api.model.HuntUser;
import com.livae.apphunt.api.model.Notification;
import com.livae.apphunt.api.v1.AdminEndpoint;
import com.livae.apphunt.common.Constants.PushNotificationType;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.apphunt.api.OfyService.ofy;

public class SendPushNotificationWorkerServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String userIdString = request.getParameter("userId");
		String commentIdString = request.getParameter("commentId");
		if (idString != null) {
			Long id = Long.parseLong(idString);
			Notification notification = Notification.get(id);
			if (notification == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			try {
				sendNotification(notification);
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			return;
		}
		String userIdString = request.getParameter("userId");
		String message = request.getParameter("message");
		if (userIdString != null || message != null) {
			HuntUser user = HuntUser.get(Long.parseLong(userIdString));
			try {
				AdminEndpoint.sendPushNotification(user, message, PushNotificationType.MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	private void sendNotification(Notification notification)
	  throws BadRequestException, InternalServerErrorException, IOException {
		HuntUser user = HuntUser.get(notification.getUserId());
		notification.setApplicationEntry();
		notification.setLastUserCommented();
		AdminEndpoint.sendPushNotification(user, parse(notification), PushNotificationType.NORMAL);
		notification.setNotified(true);
		ofy().save().entity(notification);
	}

	private String parse(Notification notification) {
		ApplicationEntry applicationEntry = notification.getApplicationEntry();
		HuntUser huntUser = notification.getLastUserCommented();
		com.livae.apphunt.common.model.Notification n;
		n = new com.livae.apphunt.common.model.Notification();
		if (applicationEntry != null) {
			com.livae.apphunt.common.model.ApplicationEntry a;
			a = new com.livae.apphunt.common.model.ApplicationEntry();
			n.setApplicationEntry(a);
			a.setId(applicationEntry.getId());
			a.setImageUrl(applicationEntry.getImageUrl());
			a.setTitle(applicationEntry.getTitle());
		}
		if (huntUser != null) {
			com.livae.apphunt.common.model.HuntUser u;
			u = new com.livae.apphunt.common.model.HuntUser();
			n.setLastUserCommented(u);
			u.setId(huntUser.getId());
			u.setImageUrl(huntUser.getImageUrl());
			u.setName(huntUser.getName());
		}
		n.setId(notification.getId());
		n.setUserId(notification.getUserId());
		n.setNewComments(notification.getNewComments());
		n.setType(notification.getType().name());
		n.setUpdated(notification.getUpdated().getTime());
		Gson gson = new GsonBuilder().create();
		return gson.toJson(n);
	}

}
