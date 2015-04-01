package com.livae.ff.api;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.livae.ff.api.model.AdminUser;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.CommentVote;
import com.livae.ff.api.model.PhoneUser;

public class OfyService {

	static {
		factory().register(Comment.class);
		factory().register(CommentVote.class);
		factory().register(PhoneUser.class);
		factory().register(AdminUser.class);
	}

	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}

	public static ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}
}
