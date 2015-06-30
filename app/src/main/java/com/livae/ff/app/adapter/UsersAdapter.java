package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.UserViewHolder;

import javax.annotation.Nonnull;

public class UsersAdapter extends CursorAdapter<UserViewHolder> {

	public static final String[] PROJECTION = {Table.LocalUser.T_ID, Table.LocalUser.CONTACT_NAME,
											   Table.LocalUser.PHONE, Table.LocalUser.IMAGE_URI};

	private String search;

	private int iContact;

	private int iPhone;

	private int iImageUri;

	private UserClickListener userClickListener;

	private String countryISO;

	public UsersAdapter(@Nonnull Context context, @Nonnull UserClickListener userClickListener) {
		super(context);
		this.userClickListener = userClickListener;
		TelephonyManager tm;
		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		countryISO = tm.getSimCountryIso().toUpperCase();
	}

	@Override
	protected void findIndexes(@Nonnull Cursor cursor) {
		iContact = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
		iPhone = cursor.getColumnIndex(Table.LocalUser.PHONE);
		iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
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
