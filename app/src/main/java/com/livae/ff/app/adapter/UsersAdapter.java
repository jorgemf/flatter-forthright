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

	public static final String[] PROJECTION = {Table.LocalUser.CONTACT_NAME, Table.LocalUser.PHONE,
											   Table.LocalUser.IMAGE_URI};

	private int iContact;

	private int iPhone;

	private int iImageUri;

	private UserClickListener userClickListener;

	public UsersAdapter(@Nonnull Context context, @Nonnull ViewCreator viewCreator,
						@Nonnull UserClickListener userClickListener) {
		super(context, viewCreator);
		this.userClickListener = userClickListener;
	}

	@Override
	protected void findIndexes(Cursor cursor) {
		iContact = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
		iPhone = cursor.getColumnIndex(Table.LocalUser.PHONE);
		iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
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
		String image = cursor.getString(iImageUri);
		holder.setUserName(name);
		holder.setUserPhone(phone);
//		holder.setUserImageView(image); // TODO fix this
	}

}
