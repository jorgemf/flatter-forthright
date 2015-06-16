package com.livae.ff.api;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.livae.ff.api.model.AdminUser;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.CommentVote;
import com.livae.ff.api.model.Conversation;
import com.livae.ff.api.model.Numbers;
import com.livae.ff.api.model.NumbersDate;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.model.Version;

public class OfyService {

	static {
		factory().register(AdminUser.class);
		factory().register(Comment.class);
		factory().register(CommentVote.class);
		factory().register(Conversation.class);
		factory().register(Numbers.class);
		factory().register(NumbersDate.class);
		factory().register(PhoneUser.class);
		factory().register(Version.class);
	}

	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}

	public static ObjectifyFactory factory() {
		return ObjectifyService.factory();
	}
}
