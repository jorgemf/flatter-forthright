package com.livae.ff.app.fragment;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.activity.ConversationActivity;
import com.livae.ff.app.adapter.ConversationsAdapter;
import com.livae.ff.app.listener.ConversationClickListener;
import com.livae.ff.app.listener.SearchListener;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.app.sql.Table;
import com.livae.ff.common.Constants;
import com.livae.ff.common.model.Notification;

public class ChatsFragment extends AbstractFragment
  implements ConversationClickListener, NotificationDisabledReceiver.CloudMessagesDisabledListener,
			 SearchListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOAD_CHATS = 2;

	private ConversationsAdapter conversationsAdapter;

	private String searchText;

	private TextView emptyView;

	private ContentObserver contentObserver = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			getLoaderManager().restartLoader(LOAD_CHATS, Bundle.EMPTY, ChatsFragment.this);
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLoaderManager().initLoader(LOAD_CHATS, Bundle.EMPTY, this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_private_chats, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		emptyView = (TextView) view.findViewById(R.id.empty_view);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		conversationsAdapter = new ConversationsAdapter(getActivity(), this);
		recyclerView.setAdapter(conversationsAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(LOAD_CHATS, Bundle.EMPTY, this);
		final ContentResolver resolver = getActivity().getContentResolver();
		resolver.registerContentObserver(ContactsProvider.getUriContacts(), true, contentObserver);
	}

	@Override
	public void onPause() {
		super.onPause();
		search(null);
		getActivity().getContentResolver().unregisterContentObserver(contentObserver);
	}

	@Override
	public void conversationClicked(Long conversationId, String roomName, String userDisplayName,
									Long userId, Constants.ChatType chatType, Long lastMessageDate,
									TextView name, View image, String imageUri) {
		ConversationActivity.start(getActivity(), chatType, conversationId, userId, userDisplayName,
								   roomName, null);
	}

	@Override
	public boolean onNotificationReceived(Notification notification) {
		getLoaderManager().restartLoader(LOAD_CHATS, Bundle.EMPTY, this);
		// TODO
		return true;
	}

	public int getUnreadChats() {
		// TODO
		return 1;
	}

	@Override
	public void search(String text) {
		if (text != null) {
			text = text.trim();
		}
		searchText = text;
		if (TextUtils.isEmpty(searchText)) {
			searchText = null;
		}
		getLoaderManager().restartLoader(LOAD_CHATS, Bundle.EMPTY, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOAD_CHATS:
				String selection;
				String[] selectionArgs;
				String order;
				order = Table.Conversation.LAST_MESSAGE_DATE;
				if (TextUtils.isEmpty(searchText)) {
					selection = Table.Conversation.TYPE + "=? OR " +
								Table.Conversation.TYPE + "=? OR " +
								Table.Conversation.TYPE + "=?";
					selectionArgs = new String[]{Constants.ChatType.PRIVATE_ANONYMOUS.name(),
												 Constants.ChatType.PRIVATE.name(),
												 Constants.ChatType.SECRET.name()};
				} else {
					selection = "( " + Table.Conversation.ROOM_NAME + " LIKE ? OR " +
								Table.LocalUser.CONTACT_NAME + " LIKE ? " + " ) AND ( " +
								Table.Conversation.TYPE + "=? OR " +
								Table.Conversation.TYPE + "=? OR " +
								Table.Conversation.TYPE + "=? )";
					selectionArgs = new String[]{"%" + searchText + "%", "%" + searchText + "%",
												 Constants.ChatType.PRIVATE_ANONYMOUS.name(),
												 Constants.ChatType.PRIVATE.name(),
												 Constants.ChatType.SECRET.name()};
				}
				return new CursorLoader(getActivity(),
										ConversationsProvider.getUriConversationsContacts(),
										ConversationsAdapter.PROJECTION, selection, selectionArgs,
										order);
			// break
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		conversationsAdapter.setSearch(searchText);
		conversationsAdapter.setCursor(data);
		conversationsAdapter.notifyDataSetChanged();
		if (data.getCount() == 0) {
			emptyView.setVisibility(View.VISIBLE);
			if (searchText == null) {
				emptyView.setText(R.string.start_first_conversation);
			} else {
				emptyView.setText(R.string.no_conversations);
			}
		} else {
			emptyView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
