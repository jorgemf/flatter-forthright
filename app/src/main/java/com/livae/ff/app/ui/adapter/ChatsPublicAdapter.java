package com.livae.ff.app.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.listener.ChatPublicClickListener;
import com.livae.ff.app.settings.Chats;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.ui.viewholders.ChatPublicViewHolder;
import com.livae.ff.app.ui.viewholders.UserViewHolder;
import com.livae.ff.common.Constants.ChatType;

import javax.annotation.Nonnull;

public class ChatsPublicAdapter extends UsersAdapter {

	public static final String[] PROJECTION =
	  {Table.LocalUser.T_ID + " AS " + BaseColumns._ID, Table.LocalUser.CONTACT_NAME,
	   Table.LocalUser.PHONE, Table.LocalUser.IMAGE_URI, Table.LocalUser.BLOCKED,
	   Table.LocalUser.ANDROID_RAW_CONTACT_ID, Table.Conversation.T_ID + " AS CID",
	   Table.Conversation.ROOM_NAME, Table.Conversation.LAST_ACCESS, Table.Conversation.UNREAD};

	protected int iConversationId;

	protected int iRoomName;

	protected int iUnreadCount;

	private ChatPublicClickListener chatPublicClickListener;

	private ChatType chatType;

	public ChatsPublicAdapter(@Nonnull Context context,
							  @Nonnull ChatType chatType,
							  @Nonnull ChatPublicClickListener chatPublicClickListener) {
		this(context, chatType);
		this.chatPublicClickListener = chatPublicClickListener;
	}

	protected ChatsPublicAdapter(@Nonnull Context context, @Nonnull ChatType chatType) {
		super(context);
		if (chatType != ChatType.FLATTER && chatType != ChatType.FORTHRIGHT) {
			throw new RuntimeException("Only public chats allowed");
		}
		this.chatType = chatType;
	}

	@Override
	protected void bindCustomViewHolder(UserViewHolder userHolder, int position, Cursor cursor) {
		ChatPublicViewHolder holder = (ChatPublicViewHolder) userHolder;
		if (position > 0 || !TextUtils.isEmpty(search)) {
			if (TextUtils.isEmpty(search)) {
				position--;
			}
			cursor.moveToPosition(position);
			super.bindCustomViewHolder(holder, position, cursor);
			if (!cursor.isNull(iConversationId)) {
				holder.setConversationId(cursor.getLong(iConversationId));
			}
			if (!cursor.isNull(iRoomName)) {
				holder.setRoomName(cursor.getString(iRoomName));
			}
			// does not set a number, jut a flag
			holder.setUnread(!cursor.isNull(iUnreadCount) && cursor.getInt(iUnreadCount) > 0);
		} else {
			holder.clear();
			final AppUser appUser = Application.appUser();
			final Chats chats = appUser.getChats();
			switch (chatType) {
				case FLATTER:
					holder.setConversationId(chats.getChatFlatterId());
					holder.setUnreadCount(chats.getChatFlatterUnread());
					break;
				case FORTHRIGHT:
					holder.setConversationId(chats.getChatForthrightId());
					holder.setUnreadCount(chats.getChatForthrightUnread());
					break;
			}
			holder.setUserPhone(appUser.getUserPhone(), countryISO);
			String imageUri = chats.getUserImageUri();
			if (TextUtils.isEmpty(imageUri)) {
				holder.setUserImageRes(R.drawable.me_drawable);
			} else {
				holder.setUserImage(imageUri);
			}
			String displayName = chats.getUserDisplayName();
			if (TextUtils.isEmpty(displayName)) {
				holder.setUserNameRes(R.string.me);
			} else {
				holder.setUserName(displayName);
			}
		}
	}

	@Override
	protected void findIndexes(@Nonnull Cursor cursor) {
		super.findIndexes(cursor);
		iConversationId = cursor.getColumnIndexOrThrow("CID");
		iRoomName = cursor.getColumnIndexOrThrow(Table.Conversation.ROOM_NAME);
		iUnreadCount = cursor.getColumnIndexOrThrow(Table.Conversation.UNREAD);
	}

	@Override
	public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		View view = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
		return new ChatPublicViewHolder(view, chatPublicClickListener);
	}

	@Override
	public void onBindViewHolder(UserViewHolder viewHolder, int position) {
		int size = getItemCount();
		if (position >= 0 && position < size) {
			bindCustomViewHolder(viewHolder, position, getCursor());
		}
	}

	@Override
	public long getItemId(int position) {
		if (position > 0 || !TextUtils.isEmpty(search)) {
			position--;
			return super.getItemId(position);
		} else {
			return RecyclerView.NO_ID;
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
