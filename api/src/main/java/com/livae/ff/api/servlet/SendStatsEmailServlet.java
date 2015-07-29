package com.livae.ff.api.servlet;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.CommentVote;
import com.livae.ff.api.model.Conversation;
import com.livae.ff.api.model.CounterStats;
import com.livae.ff.api.model.FlagComment;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.model.Stats;
import com.livae.ff.api.v1.ApiEndpoint;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.mail.Session;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.livae.ff.api.OfyService.ofy;

public class SendStatsEmailServlet extends HttpServlet {

	private static final Logger logger = Logger.getLogger(SendStatsEmailServlet.class.getName());

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query query;
		Entity entityStat;
		Date date = new Date();
		Stats stats = new Stats();
		int count;
		long now = System.currentTimeMillis();

		// total users
		query = new Query("__Stat_Kind__");
		query.setFilter(new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL,
												  PhoneUser.class.getSimpleName()));
		entityStat = datastore.prepare(query).asSingleEntity();
		if (entityStat != null && entityStat.hasProperty("count")) {
			stats.setTotalUsers((Long) entityStat.getProperty("count"));
		}

		// total comments
		query = new Query("__Stat_Kind__");
		query.setFilter(new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL,
												  Comment.class.getSimpleName()));
		entityStat = datastore.prepare(query).asSingleEntity();
		if (entityStat != null && entityStat.hasProperty("count")) {
			stats.setTotalComments((Long) entityStat.getProperty("count"));
		}

		// total conversations
		query = new Query("__Stat_Kind__");
		query.setFilter(new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL,
												  Conversation.class.getSimpleName()));
		entityStat = datastore.prepare(query).asSingleEntity();
		if (entityStat != null && entityStat.hasProperty("count")) {
			stats.setTotalConversations((Long) entityStat.getProperty("count"));
		}

		// total comment votes
		query = new Query("__Stat_Kind__");
		query.setFilter(new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL,
												  CommentVote.class.getSimpleName()));
		entityStat = datastore.prepare(query).asSingleEntity();
		if (entityStat != null && entityStat.hasProperty("count")) {
			stats.setTotalCommentVotes((Long) entityStat.getProperty("count"));
		}

		// total flags
		query = new Query("__Stat_Kind__");
		query.setFilter(new Query.FilterPredicate("kind_name", Query.FilterOperator.EQUAL,
												  FlagComment.class.getSimpleName()));
		entityStat = datastore.prepare(query).asSingleEntity();
		if (entityStat != null && entityStat.hasProperty("count")) {
			stats.setTotalFlags((Long) entityStat.getProperty("count"));
		}

		// active users of the last 1 days
		date.setTime(now - TimeUnit.DAYS.toMillis(1));
		count = ofy().load().type(PhoneUser.class).filter("lastAccess >=", date).count();
		stats.setActiveUsersLast1Day((long) count);
		// created users of the last 1 days
		count = ofy().load().type(PhoneUser.class).filter("created >=", date).count();
		stats.setCreatedUsersLast1Day((long) count);

		// active users of the last 7 days
		date.setTime(now - TimeUnit.DAYS.toMillis(7));
		count = ofy().load().type(PhoneUser.class).filter("lastAccess >=", date).count();
		stats.setActiveUsersLast7Days((long) count);
		// created users of the last 7 days
		count = ofy().load().type(PhoneUser.class).filter("created >=", date).count();
		stats.setCreatedUsersLast7Days((long) count);

		// active users of the last 15 days
		date.setTime(now - TimeUnit.DAYS.toMillis(15));
		count = ofy().load().type(PhoneUser.class).filter("lastAccess >=", date).count();
		stats.setActiveUsersLast15Days((long) count);
		// created users of the last 15 days
		count = ofy().load().type(PhoneUser.class).filter("created >=", date).count();
		stats.setCreatedUsersLast15Days((long) count);

		// active users of the last 30 days
		date.setTime(now - TimeUnit.DAYS.toMillis(30));
		count = ofy().load().type(PhoneUser.class).filter("lastAccess >=", date).count();
		stats.setActiveUsersLast30Days((long) count);
		// created users of the last 30 days
		count = ofy().load().type(PhoneUser.class).filter("created >=", date).count();
		stats.setCreatedUsersLast30Days((long) count);

		CounterStats counterStats = ofy().load().type(CounterStats.class).first().now();
		if (counterStats == null) {
			counterStats = new CounterStats();
		}

		stats.setAnonymousMessages(counterStats.getAnonymousMessages());
		stats.setPrivateMessages(counterStats.getPrivateMessages());
		stats.setSecretMessages(counterStats.getSecretMessages());
		stats.setFlatteredMessages(counterStats.getFlatteredMessages());
		stats.setForthrightMessages(counterStats.getForthrightMessages());

		ofy().save().entity(stats);

		Session session = Session.getDefaultInstance(new Properties(), null);
		String subject = "Stats of pensamientos.livae";
		String msgBody = "";
		final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
																	 DateFormat.LONG);
		msgBody += "Date:  " + dateFormat.format(stats.getDate()) + "   -  " +
				   stats.getDate().getTime() + "\n";
		msgBody += "Total users:  " + stats.getTotalUsers() + "\n";
		msgBody += "Total comments:  " + stats.getTotalComments() + "\n";
		msgBody += "Total conversations:  " + stats.getTotalConversations() + "\n";
		msgBody += "Total flags:  " + stats.getTotalFlags() + "\n";
		msgBody += "Active users last 1 day:  " + stats.getActiveUsersLast1Day() + "\n";
		msgBody += "Active users last 7 days:  " + stats.getActiveUsersLast7Days() + "\n";
		msgBody += "Active users last 15 days:  " + stats.getActiveUsersLast15Days() + "\n";
		msgBody += "Active users last 30 days:  " + stats.getActiveUsersLast30Days() + "\n";
		msgBody += "Created users last 1 day:  " + stats.getCreatedUsersLast1Day() + "\n";
		msgBody += "Created users last 7 days:  " + stats.getCreatedUsersLast7Days() + "\n";
		msgBody += "Created users last 15 days:  " + stats.getCreatedUsersLast15Days() + "\n";
		msgBody += "Created users last 30 days:  " + stats.getCreatedUsersLast30Days() + "\n";
		msgBody += "Total comments real:  " + counterStats.getTotal() + "\n";
		msgBody += "Total comments private:  " + stats.getPrivateMessages() + "\n";
		msgBody += "Total comments secret:  " + stats.getSecretMessages() + "\n";
		msgBody += "Total comments anonymous:  " + stats.getAnonymousMessages() + "\n";
		msgBody += "Total comments flattered:  " + stats.getFlatteredMessages() + "\n";
		msgBody += "Total comments forthright:  " + stats.getForthrightMessages() + "\n";

		msgBody += dateFormat.format(stats.getDate()) + ";";
		msgBody += stats.getTotalUsers() + ";";
		msgBody += stats.getTotalComments() + ";";
		msgBody += stats.getTotalConversations() + ";";
		msgBody += stats.getTotalFlags() + ";";
		msgBody += stats.getActiveUsersLast1Day() + ";";
		msgBody += stats.getActiveUsersLast7Days() + ";";
		msgBody += stats.getActiveUsersLast15Days() + ";";
		msgBody += stats.getActiveUsersLast30Days() + ";";
		msgBody += stats.getCreatedUsersLast1Day() + ";";
		msgBody += stats.getCreatedUsersLast7Days() + ";";
		msgBody += stats.getCreatedUsersLast15Days() + ";";
		msgBody += stats.getCreatedUsersLast30Days() + ";";
		msgBody += counterStats.getTotal() + ";";
		msgBody += stats.getPrivateMessages() + ";";
		msgBody += stats.getSecretMessages() + ";";
		msgBody += stats.getAnonymousMessages() + ";";
		msgBody += stats.getFlatteredMessages() + ";";
		msgBody += stats.getForthrightMessages() + ";";

		try {
			ApiEndpoint.sendEmail(session, "pensamientos@livae.com", subject, msgBody);
		} catch (Exception e) {
			logger.severe(e.getMessage());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
