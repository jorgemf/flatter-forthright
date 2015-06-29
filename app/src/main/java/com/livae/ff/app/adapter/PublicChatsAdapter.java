package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;

import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.UserViewHolder;

import javax.annotation.Nonnull;

public class PublicChatsAdapter extends UsersAdapter {

	private int iConversationId;

	public static final String[] PROJECTION = {Table.Conversation.ID, Table.LocalUser.CONTACT_NAME,
											   Table.LocalUser.PHONE, Table.LocalUser.IMAGE_URI};

	public PublicChatsAdapter(@Nonnull Context context,
							  @Nonnull UserClickListener userClickListener) {
		super(context, userClickListener);
	}

	@Override
	protected void findIndexes(@Nonnull Cursor cursor) {
		super.findIndexes(cursor);
		iConversationId = cursor.getColumnIndex(Table.Conversation.ID);
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
