package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.ChatPrivateClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.PhoneUtils;
import com.livae.ff.app.viewholders.ChatPrivateViewHolder;
import com.livae.ff.app.viewholders.UserViewHolder;
import com.livae.ff.common.Constants;

import javax.annotation.Nonnull;

public class ChatsPrivateAdapter extends UsersAdapter {

	public static final String[] PROJECTION = {Table.LocalUser.CONTACT_NAME, Table.LocalUser.PHONE,
											   Table.LocalUser.IMAGE_URI, Table.Conversation.T_ID,
											   Table.Conversation.ROOM_NAME,
											   Table.Conversation.TYPE,
											   Table.Conversation.LAST_MESSAGE,
											   Table.Conversation.LAST_MESSAGE_DATE,
											   Table.Conversation.LAST_ACCESS,
											   Table.Conversation.UNREAD, Table.Conversation.PHONE};

	protected int iConversationId;

	protected int iRoomName;

	protected int iType;

	protected int iLastMessage;

	protected int iLastMessageDate;

	protected int iLastAccess;

	protected int iUnreadCount;

	protected int iConversationPhone;

	private ChatPrivateClickListener chatPrivateClickListener;

	public ChatsPrivateAdapter(@Nonnull Context context,
							   @Nonnull ChatPrivateClickListener chatPrivateClickListener) {
		this(context);
		this.chatPrivateClickListener = chatPrivateClickListener;
	}

	protected ChatsPrivateAdapter(@Nonnull Context context) {
		super(context);
	}

	@Override
	protected void findIndexes(Cursor cursor) {
		super.findIndexes(cursor);
		iConversationId = cursor.getColumnIndex(Table.Conversation.ID);
		iRoomName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
		iType = cursor.getColumnIndex(Table.Conversation.TYPE);
		iLastMessage = cursor.getColumnIndex(Table.Conversation.LAST_MESSAGE);
		iLastMessageDate = cursor.getColumnIndex(Table.Conversation.LAST_MESSAGE_DATE);
		iLastAccess = cursor.getColumnIndex(Table.Conversation.LAST_ACCESS);
		iUnreadCount = cursor.getColumnIndex(Table.Conversation.UNREAD);
		iConversationPhone = cursor.getColumnIndex(Table.Conversation.PHONE);
	}

	@Override
	protected void bindCustomViewHolder(UserViewHolder userHolder, int position, Cursor cursor) {
		ChatPrivateViewHolder holder = (ChatPrivateViewHolder) userHolder;
		holder.clear();
		holder.setConversationId(cursor.getLong(iConversationId));
		final Constants.ChatType chatType = Constants.ChatType.valueOf(cursor.getString(iType));
		holder.setChatType(chatType);
		final String roomName = cursor.getString(iRoomName);
		if (roomName != null) {
			holder.setConversationTitle(roomName, search);
		}
		holder.setAnonymous(chatType == Constants.ChatType.PRIVATE_ANONYMOUS);
		holder.setSecret(chatType == Constants.ChatType.SECRET);
		if (chatType == Constants.ChatType.PRIVATE_ANONYMOUS) {
			holder.setImageAnonymous(roomName);
		}
		if (!cursor.isNull(iPhone)) {
			if (chatType == Constants.ChatType.PRIVATE_ANONYMOUS) {
				holder.setUserName(cursor.getString(iContact), search);
			} else {
				holder.setConversationTitle(cursor.getString(iContact), search);
			}
			holder.setUserImage(cursor.getString(iImageUri));
			holder.setUserPhone(cursor.getLong(iPhone));
		} else if (!cursor.isNull(iConversationPhone)) {
			Long conversationPhone = cursor.getLong(iConversationPhone);
			holder.setConversationTitle(PhoneUtils.getPrettyPrint(conversationPhone, countryISO),
										null);
		}
		if (!cursor.isNull(iLastMessage)) {
			holder.setLastMessage(cursor.getString(iLastMessage), cursor.getLong(iLastMessageDate));
		}
		if (!cursor.isNull(iLastAccess)) {
			holder.setLastAccessDate(cursor.getLong(iLastAccess));
		}
		holder.setUnreadCount(cursor.getInt(iUnreadCount));
	}

	@Override
	public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		View view = layoutInflater.inflate(R.layout.item_chat, viewGroup, false);
		return new ChatPrivateViewHolder(view, chatPrivateClickListener);
	}

}
