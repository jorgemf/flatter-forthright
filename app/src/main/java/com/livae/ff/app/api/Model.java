package com.livae.ff.app.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.livae.ff.api.ff.model.CollectionResponseComment;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.provider.DataProvider;
import com.livae.ff.app.sql.Table;

import java.util.ArrayList;
import java.util.List;

public class Model {

	private static final String LOG_TAG = "MODEL";

	private List<ContentValues> commentsList;

	private Context context;

	public Model(Context applicationContext) {
		this.context = applicationContext;
		commentsList = new ArrayList<>();
	}

	public synchronized void save() {
		if (BuildConfig.DEBUG) {
			if (commentsList.size() > 0) {
				Log.v(LOG_TAG, "COMMENTS");
				for (ContentValues value : commentsList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
		}
		ContentResolver contentResolver = context.getContentResolver();
		if (commentsList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriComments(),
									   commentsList.toArray(new ContentValues[commentsList
																				.size()]));
			commentsList.clear();
		}
	}

	public synchronized void parse(Comment comment) {
		ContentValues val = new ContentValues();

		val.put(Table.Comment.ID, comment.getId());
		val.put(Table.Comment.AGREE_VOTES, comment.getAgreeVotes());
		val.put(Table.Comment.DISAGREE_VOTES, comment.getDisagreeVotes());
		val.put(Table.Comment.DATE, comment.getDate().getValue());
		val.put(Table.Comment.PHONE, comment.getPhone());
		val.put(Table.Comment.USER_ID, comment.getUser());
		val.put(Table.Comment.VOTE_TYPE, comment.getVoteType());
		val.put(Table.Comment.USER_VOTE_TYPE, comment.getUserVoteType());
		val.put(Table.Comment.COMMENT, comment.getComment());

		commentsList.add(val);
	}

	public synchronized void parse(CollectionResponseComment comments) {
		if (comments != null && comments.getItems() != null) {
			for (Comment comment : comments.getItems()) {
				parse(comment);
			}
		}
	}

}
