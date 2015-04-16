package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.UserViewHolder;

import javax.annotation.Nonnull;

public class UsersAdapter extends EndlessCursorAdapter<UserViewHolder> {

	public static final String[] PROJECTION = {Table.LocalUser.CONTACT, Table.LocalUser.PHONE,
											   Table.LocalUser.IMAGE, Table.LocalUser.FAVORITE};

	private int iId;

	private int iContact;

	private int iPhone;

	private int iImage;

	private int iFavorite;

	private UserClickListener userClickListener;

	public UsersAdapter(@Nonnull Context context, @Nonnull ViewCreator viewCreator,
						@Nonnull UserClickListener userClickListener) {
		super(context, viewCreator);
		this.userClickListener = userClickListener;
	}

	@Override
	protected void findIndexes(Cursor cursor) {
		iId = cursor.getColumnIndex(Table.LocalUser.ID);
		iContact = cursor.getColumnIndex(Table.LocalUser.CONTACT);
		iPhone = cursor.getColumnIndex(Table.LocalUser.PHONE);
		iImage;=cursor.getColumnIndex(Table.LocalUser.IMAGE);
		iFavorite = cursor.getColumnIndex(Table.LocalUser.FAVORITE);
	}

	@Override
	protected UserViewHolder createCustomViewHolder(final ViewGroup viewGroup, final int type) {
		View view = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
		return new UserViewHolder(view, userClickListener);
	}

	@Override
	protected void bindCustomViewHolder(UserViewHolder holder, int position, Cursor cursor) {
		holder.clear();
		String name = cursor.getString(iContact);
		long phone = cursor.getLong(iPhone);
		boolean favorite = cursor.getInt(iFavorite) != 0;
		String image = cursor.getString(iImage);
		holder.setUserName(name);
		holder.setUserPhone(phone);
		holder.setUserImageView(image); // TODO fix this
		holder.setFavorite(favorite);
	}

}
