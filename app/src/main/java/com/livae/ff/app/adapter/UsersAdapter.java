package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.PhoneUtils;
import com.livae.ff.app.viewholders.UserViewHolder;

import javax.annotation.Nonnull;

public class UsersAdapter extends CursorAdapter<UserViewHolder> {

	public static final String[] PROJECTION = {Table.LocalUser.CONTACT_NAME, Table.LocalUser.PHONE,
											   Table.LocalUser.IMAGE_URI, Table.LocalUser.BLOCKED};

	protected String search;

	protected String countryISO;

	protected int iContact;

	protected int iPhone;

	protected int iImageUri;

	protected int iBlocked;

	private UserClickListener userClickListener;

	public UsersAdapter(@Nonnull Context context, @Nonnull UserClickListener userClickListener) {
		this(context);
		this.userClickListener = userClickListener;
	}

	protected UsersAdapter(@Nonnull Context context) {
		super(context);
		countryISO = PhoneUtils.getCountryISO(context);
	}

	@Override
	protected void findIndexes(@Nonnull Cursor cursor) {
		iContact = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
		iPhone = cursor.getColumnIndex(Table.LocalUser.PHONE);
		iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
		iBlocked = cursor.getColumnIndex(Table.LocalUser.BLOCKED);
	}

	@Override
	public long getItemId(int position) {
		int size = getItemCount();
		if (position >= 0 && position < size) {
			Cursor cursor = getCursor();
			cursor.moveToPosition(position);
			return cursor.getLong(iPhone);
		} else {
			return RecyclerView.NO_ID;
		}
	}

	@Override
	protected void bindCustomViewHolder(UserViewHolder holder, int position, Cursor cursor) {
		holder.clear();
		String name = cursor.getString(iContact);
		long phone = cursor.getLong(iPhone);
		String imageUri = cursor.getString(iImageUri);
		holder.setUserName(name, search);
		holder.setUserPhone(phone, countryISO);
		holder.setUserImage(imageUri);
		holder.setUserBlocked(cursor.getInt(iBlocked) != 0);
	}

	@Override
	public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		View view = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
		return new UserViewHolder(view, userClickListener);
	}

	public void setSearch(String search) {
		this.search = search;
	}

}
