package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.listener.ChatPublicClickListener;
import com.livae.ff.app.settings.Chats;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.ChatPublicViewHolder;
import com.livae.ff.app.viewholders.UserViewHolder;
import com.livae.ff.common.Constants.ChatType;

import javax.annotation.Nonnull;

public class ChatsPublicAdapter extends UsersAdapter {

	public static final String[] PROJECTION = {Table.LocalUser.T_ID, Table.LocalUser.CONTACT_NAME,
											   Table.LocalUser.PHONE, Table.LocalUser.IMAGE_URI,
											   Table.Conversation.T_ID + " AS CID",
											   Table.Conversation.ROOM_NAME};

	protected int iConversationId;

	protected int iRoomName;

	private ChatPublicClickListener chatPublicClickListener;

	private ChatType chatType;

	public ChatsPublicAdapter(@Nonnull Context context, @NonNull ChatType chatType,
							  @Nonnull ChatPublicClickListener chatPublicClickListener) {
		this(context, chatType);
		this.chatPublicClickListener = chatPublicClickListener;
	}

	protected ChatsPublicAdapter(@Nonnull Context context, @NonNull ChatType chatType) {
		super(context);
		if (chatType != ChatType.FLATTER && chatType != ChatType.FORTHRIGHT) {
			throw new RuntimeException("Only public chats allowed");
		}
		this.chatType = chatType;
	}

	@Override
	protected void findIndexes(@Nonnull Cursor cursor) {
		super.findIndexes(cursor);
		iConversationId = cursor.getColumnIndex("CID");
		iRoomName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
	}

	@Override
	protected void bindCustomViewHolder(UserViewHolder userHolder, int position, Cursor cursor) {
		super.bindCustomViewHolder(userHolder, position, cursor);
		ChatPublicViewHolder holder = (ChatPublicViewHolder) userHolder;
		if (position > 0 || !TextUtils.isEmpty(search)) {
			position--;
			super.bindCustomViewHolder(holder, position, cursor);
			if (!cursor.isNull(iConversationId)) {
				holder.setConversationId(cursor.getLong(iConversationId));
			}
			if (!cursor.isNull(iRoomName)) {
				holder.setRoomName(cursor.getString(iRoomName));
			}
		} else {
			final AppUser appUser = Application.appUser();
			final Chats chats = appUser.getChats();
			switch (chatType) {
				case FLATTER:
					holder.setConversationId(chats.getChatFlatterId());
					break;
				case FORTHRIGHT:
					holder.setConversationId(chats.getChatForthrightId());
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
	public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		View view = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
		return new ChatPublicViewHolder(view, chatPublicClickListener);
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
