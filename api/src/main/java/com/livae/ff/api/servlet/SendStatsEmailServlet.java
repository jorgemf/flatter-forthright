package com.livae.ff.api.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.livae.ff.api.model.PhoneUser;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.ff.api.OfyService.ofy;

public class SendStatsEmailServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query;
		Entity entityStat;

		query = new Query("__Stat_Kind__");
		query.setFilter(new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL,
												  PhoneUser.class.getName()));
		entityStat = datastore.prepare(query).asSingleEntity();
		Long totalUsers = (Long) entityStat.getProperty("count");

		// total users
		// total comments
		// total conversations
		// total comment votes
		// total flags
		// active users of the last 1 days
		// active users of the last 7 days
		// active users of the last 15 days
		// active users of the last 30 days

		// TODO send email
	}

}
