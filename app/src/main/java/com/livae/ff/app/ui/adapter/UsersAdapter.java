package com.livae.ff.app.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.ui.viewholders.UserViewHolder;
import com.livae.ff.app.utils.PhoneUtils;

import javax.annotation.Nonnull;

public class UsersAdapter extends CursorAdapter<UserViewHolder> {

	public static final String[] PROJECTION =
	  {Table.LocalUser.PHONE + " AS " + BaseColumns._ID, Table.LocalUser.CONTACT_NAME,
	   Table.LocalUser.PHONE, Table.LocalUser.IMAGE_URI, Table.LocalUser.BLOCKED,
	   Table.LocalUser.ANDROID_RAW_CONTACT_ID};

	protected String search;

	protected String countryISO;

	protected int iContact;

	protected int iPhone;

	protected int iImageUri;

	protected int iBlocked;

	protected int iRawContactId;

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
	protected void bindCustomViewHolder(UserViewHolder holder, int position, Cursor cursor) {
		holder.clear();
		String name = cursor.getString(iContact);
		long phone = cursor.getLong(iPhone);
		String imageUri = cursor.getString(iImageUri);
		holder.setUserName(name, search);
		holder.setUserPhone(phone, countryISO);
		holder.setUserImage(imageUri);
		holder.setUserBlocked(cursor.getInt(iBlocked) != 0);
		if (cursor.isNull(iRawContactId)) {
			holder.setRawContactId(null);
		} else {
			holder.setRawContactId(cursor.getLong(iRawContactId));
		}
	}

	@Override
	protected void findIndexes(@Nonnull Cursor cursor) {
		iContact = cursor.getColumnIndexOrThrow(Table.LocalUser.CONTACT_NAME);
		iPhone = cursor.getColumnIndexOrThrow(Table.LocalUser.PHONE);
		iImageUri = cursor.getColumnIndexOrThrow(Table.LocalUser.IMAGE_URI);
		iBlocked = cursor.getColumnIndexOrThrow(Table.LocalUser.BLOCKED);
		iRawContactId = cursor.getColumnIndexOrThrow(Table.LocalUser.ANDROID_RAW_CONTACT_ID);
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
