package com.livae.ff.api.servlet.worker;

import com.livae.ff.api.Settings;
import com.livae.ff.api.model.FlagComment;
import com.livae.ff.api.model.PhoneUser;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.ff.api.OfyService.ofy;

public class ForgetFlaggedUsersWorkerServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Date minDate = new Date(System.currentTimeMillis() - Settings.FLAG_FORGET_TIME);
		List<FlagComment> flagCommentList;
		flagCommentList = ofy().load().type(FlagComment.class).filter("forgot", false)
							   .filter("date<", minDate).list();
		for (FlagComment flag : flagCommentList) {
			PhoneUser user = PhoneUser.get(flag.getUserId());
			user.unflag(flag.getReason());
			flag.setForgot(true);
			ofy().save().entity(flag);
			ofy().save().entity(user).now();
		}
	}

}
