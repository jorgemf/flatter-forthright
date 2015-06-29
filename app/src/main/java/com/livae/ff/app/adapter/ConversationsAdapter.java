package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.ConversationClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.ConversationViewHolder;
import com.livae.ff.common.Constants;

import javax.annotation.Nonnull;

public class ConversationsAdapter extends CursorAdapter<ConversationViewHolder> {

	public static final String[] PROJECTION = {Table.Conversation.ID, Table.Conversation.ROOM_NAME,
											   Table.Conversation.TYPE,
											   Table.Conversation.LAST_MESSAGE,
											   Table.Conversation.LAST_MESSAGE_DATE,
											   Table.LocalUser.CONTACT_NAME,
											   Table.LocalUser.IMAGE_URI};

	private int iId;

	private int iPhone;

	private int iRoomName;

	private int iType;

	private int iLastMessage;

	private int iLastMessageDate;

	private int iContactName;

	private int iContactImageUri;

	private ConversationClickListener conversationClickListener;

	public ConversationsAdapter(@Nonnull Context context,
								@Nonnull ConversationClickListener conversationClickListener) {
		super(context);
		this.conversationClickListener = conversationClickListener;
	}

	@Override
	protected void findIndexes(Cursor cursor) {
		iId = cursor.getColumnIndex(Table.Conversation.ID);
		iPhone = cursor.getColumnIndex(Table.Conversation.PHONE);
		iRoomName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
		iType = cursor.getColumnIndex(Table.Conversation.TYPE);
		iLastMessage = cursor.getColumnIndex(Table.Conversation.LAST_MESSAGE);
		iLastMessageDate = cursor.getColumnIndex(Table.Conversation.LAST_MESSAGE_DATE);
		iContactName = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
		iContactImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
	}

	@Override
	protected void bindCustomViewHolder(ConversationViewHolder viewHolder, int position,
										Cursor cursor) {
		viewHolder.clear();
		viewHolder.setConversationId(cursor.getLong(iId));
		final Constants.ChatType chatType = Constants.ChatType.valueOf(cursor.getString(iType));
		viewHolder.setConversationType(chatType);
		final String roomName = cursor.getString(iRoomName);
		viewHolder.setConversationTitle(roomName);
		viewHolder.setAnonymous(chatType == Constants.ChatType.PRIVATE_ANONYMOUS);
		viewHolder.setSecret(chatType == Constants.ChatType.SECRET);
		if (!cursor.isNull(iPhone)) {
			viewHolder.setContactName(cursor.getString(iContactName));
			viewHolder.setUserImage(cursor.getString(iContactImageUri));
		} else {
			viewHolder.setImageAnonymous(roomName);
		}
		if (!cursor.isNull(iLastMessage)) {
			viewHolder.setLastMessage(cursor.getString(iLastMessage),
									  cursor.getLong(iLastMessageDate));
		}
	}

	@Override
	public ConversationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		View view = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
		return new ConversationViewHolder(view, conversationClickListener);
	}
}
