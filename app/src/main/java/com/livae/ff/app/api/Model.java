package com.livae.ff.app.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.livae.apphunt.api.apphunt.model.AppVote;
import com.livae.apphunt.api.apphunt.model.Application;
import com.livae.apphunt.api.apphunt.model.ApplicationEntry;
import com.livae.apphunt.api.apphunt.model.ApplicationRelated;
import com.livae.apphunt.api.apphunt.model.CollectionResponseAppVote;
import com.livae.apphunt.api.apphunt.model.CollectionResponseApplicationEntry;
import com.livae.apphunt.api.apphunt.model.CollectionResponseApplicationRelated;
import com.livae.apphunt.api.apphunt.model.CollectionResponseComment;
import com.livae.apphunt.api.apphunt.model.Comment;
import com.livae.apphunt.api.apphunt.model.HuntUser;
import com.livae.apphunt.app.BuildConfig;
import com.livae.apphunt.app.provider.DataProvider;
import com.livae.apphunt.app.sql.Table;

import java.util.ArrayList;
import java.util.List;

public class Model {

	private static final String LOG_TAG = "MODEL";

	private List<ContentValues> usersList;

	private List<ContentValues> appsList;

	private List<ContentValues> appsVotesList;

	private List<ContentValues> commentsList;

	private List<ContentValues> appRelatedList;

	private Context context;

	public Model(Context applicationContext) {
		this.context = applicationContext;
		usersList = new ArrayList<>();
		appsList = new ArrayList<>();
		appsVotesList = new ArrayList<>();
		commentsList = new ArrayList<>();
		appRelatedList = new ArrayList<>();
	}

	public synchronized void save() {
		if (BuildConfig.DEBUG) {
			if (usersList.size() > 0) {
				Log.v(LOG_TAG, "USERS");
				for (ContentValues value : usersList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
			if (appsList.size() > 0) {
				Log.v(LOG_TAG, "APPS");
				for (ContentValues value : appsList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
			if (appsVotesList.size() > 0) {
				Log.v(LOG_TAG, "VOTES");
				for (ContentValues value : appsVotesList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
			if (commentsList.size() > 0) {
				Log.v(LOG_TAG, "COMMENTS");
				for (ContentValues value : commentsList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
			if (appRelatedList.size() > 0) {
				Log.v(LOG_TAG, "RELATED");
				for (ContentValues value : appRelatedList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
		}
		ContentResolver contentResolver = context.getContentResolver();
		if (usersList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriUsers(),
									   usersList.toArray(new ContentValues[usersList.size()]));
			usersList.clear();
		}
		if (appsList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriApplicationEntries(),
									   appsList.toArray(new ContentValues[appsList.size()]));
			appsList.clear();
		}
		if (commentsList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriComments(),
									   commentsList.toArray(new ContentValues[commentsList
																				.size()]));
			commentsList.clear();
		}
		if (appsVotesList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriVotes(),
									   appsVotesList.toArray(new ContentValues[appsVotesList
																				 .size()]));
			appsVotesList.clear();
		}
		if (appRelatedList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriUsersApplicationsRelated(),
									   appRelatedList.toArray(new ContentValues[appRelatedList
																				  .size()]));
			appRelatedList.clear();
		}
	}

	public synchronized void parse(ApplicationEntry appEntry) {
		ContentValues val = new ContentValues();

		val.put(Table.AppEntry.ID, appEntry.getId());

		Application application = appEntry.getApplication();
		String appId = appEntry.getApplicationId();
		if (application != null) {
			val.put(Table.AppEntry.APPLICATION_ID, application.getId());
			val.put(Table.AppEntry.DEVELOPER, application.getDeveloper());
		} else if (appId != null) {
			val.put(Table.AppEntry.APPLICATION_ID, appId);
		}
		Integer comments = appEntry.getComments();
		if (comments != null) {
			val.put(Table.AppEntry.COMMENTS, comments);
		}
		String lang = appEntry.getLang();
		if (lang != null) {
			val.put(Table.AppEntry.LANG, lang);
		}
		DateTime date = appEntry.getDate();
		if (date != null) {
			val.put(Table.AppEntry.DATE, date.getValue());
		}
		String description = appEntry.getDescription();
		if (description != null) {
			val.put(Table.AppEntry.DESCRIPTION, description);
		}
		String title = appEntry.getTitle();
		if (title != null) {
			val.put(Table.AppEntry.TITLE, title);
		}
		String imageUrl = appEntry.getImageUrl();
		if (imageUrl != null) {
			val.put(Table.AppEntry.IMAGE_URL, imageUrl);
		}
		HuntUser userShared = appEntry.getUserShared();
		Long userSharedId = appEntry.getUserSharedId();
		if (userShared != null) {
			val.put(Table.AppEntry.USER_SHARED_ID, userShared.getId());
			parse(userShared);
		} else if (userSharedId != null) {
			val.put(Table.AppEntry.USER_SHARED_ID, userSharedId);
		}
		Boolean voted = appEntry.getVoted();
		if (voted != null) {
			val.put(Table.AppEntry.VOTED, voted);
		}
		Integer votes = appEntry.getVotes();
		if (votes != null) {
			val.put(Table.AppEntry.VOTES, votes);
		}

		appsList.add(val);
	}

	public synchronized void parse(ApplicationRelated appRelated) {
		ContentValues val = new ContentValues();
		val.put(Table.UserApplicationRelated.ID, appRelated.getId());
		val.put(Table.UserApplicationRelated.APPLICATION_ID, appRelated.getApplicationId());
		val.put(Table.UserApplicationRelated.USER_ID, appRelated.getUserId());
		val.put(Table.UserApplicationRelated.RELATIONSHIP, appRelated.getRelationship());
		appRelatedList.add(val);
	}

	public synchronized void parse(Comment comment) {
		ContentValues val = new ContentValues();

		val.put(Table.AppEntry.ID, comment.getId());

		ApplicationEntry application = comment.getApplicationEntry();
		Long appId = comment.getApplicationEntryId();
		if (application != null) {
			val.put(Table.Comment.APPLICATION_ENTRY_ID, application.getId());
			parse(application);
		} else if (appId != null) {
			val.put(Table.Comment.APPLICATION_ENTRY_ID, appId);
		}
		HuntUser userShared = comment.getUser();
		Long userSharedId = comment.getUserId();
		if (userShared != null) {
			val.put(Table.Comment.USER_ID, userShared.getId());
			parse(userShared);
		} else if (appId != null) {
			val.put(Table.Comment.USER_ID, userSharedId);
		}
		DateTime date = comment.getDate();
		if (date != null) {
			val.put(Table.Comment.DATE, date.getValue());
		}
		String text = comment.getComment();
		if (text != null) {
			val.put(Table.Comment.COMMENT, text);
		}
		Integer upVotes = comment.getUpVotes();
		if (upVotes != null) {
			val.put(Table.Comment.UP_VOTES, upVotes);
		}
		Integer downVotes = comment.getDownVotes();
		if (downVotes != null) {
			val.put(Table.Comment.DOWN_VOTES, downVotes);
		}
		val.put(Table.Comment.VOTE_TYPE, comment.getVoteType());
		val.put(Table.Comment.DATE, comment.getDate().getValue());
		commentsList.add(val);
	}

	public synchronized void parse(HuntUser user) {
		ContentValues val = new ContentValues();
		val.put(Table.User.ID, user.getId());
		Integer appsShared = user.getAppShared();
		if (appsShared != null) {
			val.put(Table.User.APPS_SHARED, appsShared);
		}
		Integer comments = user.getComments();
		if (comments != null) {
			val.put(Table.User.COMMENTS, comments);
		}
		Integer votes = user.getVotes();
		if (votes != null) {
			val.put(Table.User.VOTES, votes);
		}
		String imageUrl = user.getImageUrl();
		if (imageUrl != null) {
			val.put(Table.User.IMAGE_URL, imageUrl);
		}
		String name = user.getName();
		if (name != null) {
			val.put(Table.User.USER_NAME, name);
		}
		String tagline = user.getTagLine();
		if (tagline != null) {
			val.put(Table.User.TAGLINE, tagline);
		}
		Boolean anonymous = user.getAnonymous();
		if (anonymous != null) {
			val.put(Table.User.ANONYMOUS, anonymous);
		}

		usersList.add(val);
	}

	public synchronized void parse(AppVote vote, Long userId, Long appEntryId) {
		ContentValues val = new ContentValues();
		HuntUser user = vote.getUser();
		ApplicationEntry applicationEntry = vote.getApplicationEntry();
		val.put(Table.Vote.ID, vote.getId());
		if (user != null) {
			parse(user);
			val.put(Table.Vote.USER_ID, user.getId());
		} else {
			val.put(Table.Vote.USER_ID, userId);
		}
		if (applicationEntry != null) {
			parse(applicationEntry);
			val.put(Table.Vote.APPLICATION_ENTRY_ID, applicationEntry.getId());
		} else {
			val.put(Table.Vote.APPLICATION_ENTRY_ID, appEntryId);
		}
		appsVotesList.add(val);
	}

	public synchronized void parse(CollectionResponseApplicationEntry appEntries) {
		if (appEntries != null && appEntries.getItems() != null) {
			for (ApplicationEntry appEntry : appEntries.getItems()) {
				parse(appEntry);
			}
		}
	}

	public synchronized void parse(CollectionResponseApplicationRelated appRelated) {
		if (appRelated != null && appRelated.getItems() != null) {
			for (ApplicationRelated appRel : appRelated.getItems()) {
				parse(appRel);
			}
		}
	}

	public synchronized void parse(CollectionResponseComment comments) {
		if (comments != null && comments.getItems() != null) {
			for (Comment comment : comments.getItems()) {
				parse(comment);
			}
		}
	}

//	public synchronized void parse(CollectionResponseHuntUser users) {
//		if (users != null && users.getItems() != null) {
//			for (HuntUser user : users.getItems()) {
//				parse(user);
//			}
//		}
//	}

	public synchronized void parse(CollectionResponseAppVote votes, Long userId, Long appEntryId) {
		if (votes != null && votes.getItems() != null) {
			for (AppVote vote : votes.getItems()) {
				parse(vote, userId, appEntryId);
			}
		}
	}
}
