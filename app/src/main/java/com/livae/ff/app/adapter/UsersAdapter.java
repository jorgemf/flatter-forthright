package com.livae.ff.app.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.android.loading.CursorRecyclerAdapter;
import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.CommentsViewHolder;
import com.livae.ff.app.viewholders.UserViewHolder;

import javax.annotation.Nonnull;

public class UsersAdapter extends EndlessCursorAdapter<CommentsViewHolder> {

	public static final String[] PROJECTION = {};

	private int iId;

	private UserClickListener userClickListener;

	private LayoutInflater layoutInflater;

	public UsersAdapter(@Nonnull Activity activity, @Nonnull UserClickListener userClickListener) {
		layoutInflater = activity.getLayoutInflater();
		this.userClickListener = userClickListener;
	}

	@Override
	public CommentsViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int type) {
		View view = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
		return new UserViewHolder(view, userClickListener);
	}

	@Override
	public void findIndexes(Cursor cursor) {
		iId = cursor.getColumnIndex(Table.Comment.ID);
	}

	@Override
	public void onBindViewHolder(final CommentsViewHolder holder, final Cursor cursor,
								 final int position) {
		holder.clear();
	}
}
