package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.UserViewHolder;

import javax.annotation.Nonnull;

public class PublicChatsAdapter extends UsersAdapter {

	public static final String[] PROJECTION = {Table.LocalUser.T_ID,
											   Table.Conversation.T_ID + " AS CID",
											   Table.LocalUser.CONTACT_NAME, Table.LocalUser.PHONE,
											   Table.LocalUser.IMAGE_URI};

	private int iConversationId;

	public PublicChatsAdapter(@Nonnull Context context,
							  @Nonnull UserClickListener userClickListener) {
		super(context, userClickListener);
	}

	@Override
	protected void findIndexes(@Nonnull Cursor cursor) {
		super.findIndexes(cursor);
		iConversationId = cursor.getColumnIndex("CID");
	}

	@Override
	protected void bindCustomViewHolder(UserViewHolder holder, int position, Cursor cursor) {
		if (position > 0 || !TextUtils.isEmpty(search)) {
			position--;
			super.bindCustomViewHolder(holder, position, cursor);
			if (!cursor.isNull(iConversationId)) {
				long conversationId = cursor.getLong(iConversationId);
				holder.setConversationId(conversationId);
			}
		} else {
			holder.setUserPhone(Application.appUser().getUserPhone(), countryISO);
			String imageUri = Application.appUser().getUserImageUri();
			if (TextUtils.isEmpty(imageUri)) {
				holder.setUserImageRes(R.drawable.me_drawable);
			} else {
				holder.setUserImage(imageUri);
			}
			String displayName = Application.appUser().getUserDisplayName();
			if (TextUtils.isEmpty(displayName)) {
				holder.setUserNameRes(R.string.me);
			} else {
				holder.setUserName(displayName);
			}
		}
	}

	@Override
	public long getItemId(int position) {
		if (position > 0 || !TextUtils.isEmpty(search)) {
			position--;
			return super.getItemId(position);
		} else {
			return 0;
		}
	}

	@Override
	public int getItemCount() {
		if (!TextUtils.isEmpty(search)) {
			return super.getItemCount();
		} else {
			return super.getItemCount() + 1;
		}
	}
}
