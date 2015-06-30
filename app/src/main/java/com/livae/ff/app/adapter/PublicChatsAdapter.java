package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;

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
		super.bindCustomViewHolder(holder, position, cursor);
		if (!cursor.isNull(iConversationId)) {
			long conversationId = cursor.getLong(iConversationId);
			holder.setConversationId(conversationId);
		}
	}

}
