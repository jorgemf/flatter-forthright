package com.livae.ff.api.servlet;

import com.googlecode.objectify.cmd.Query;
import com.livae.ff.api.Settings;
import com.livae.ff.api.model.FlagComment;
import com.livae.ff.api.model.PhoneUser;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.ff.api.OfyService.ofy;

public class ForgetFlaggedUsersServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	  throws IOException {
		Date date = new Date(System.currentTimeMillis() - Settings.FLAG_FORGET_TIME);
		Query<FlagComment> query;
		query = ofy().load().type(FlagComment.class).filter("forgot", false).filter("date<", date);
		for (FlagComment flag : query) {
			flag.setForgot(true);
			PhoneUser user = PhoneUser.get(flag.getUserId());
			user.unflag(flag.getReason());
			ofy().save().entities(flag, user);
		}
	}

}
