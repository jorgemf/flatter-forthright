package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

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

	public UsersAdapter(@Nonnull Context context, @Nonnull ViewCreator viewCreator,
						@Nonnull UserClickListener userClickListener) {
		super(context, viewCreator);
		this.userClickListener = userClickListener;
	}

	@Override
	protected void findIndexes(Cursor cursor) {
		iId = cursor.getColumnIndex(Table.Comment.ID);
	}

	@Override
	protected CommentsViewHolder createCustomViewHolder(final ViewGroup viewGroup, final int type) {
		View view = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
		return new UserViewHolder(view, userClickListener);
	}

	@Override
	protected void bindCustomViewHolder(CommentsViewHolder holder, int position, Cursor cursor) {
		holder.clear();
	}

}
