package com.livae.ff.app.fragment;

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

import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.ChatPublicActivity;
import com.livae.ff.app.adapter.ChatsPublicAdapter;
import com.livae.ff.app.listener.ChatPublicClickListener;
import com.livae.ff.app.listener.SearchListener;
import com.livae.ff.app.model.ChatPublicModel;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.app.sql.Table;
import com.livae.ff.common.Constants;
import com.livae.ff.common.model.Notification;

public class ChatsPublicFragment extends AbstractFragment
  implements ChatPublicClickListener, NotificationDisabledReceiver.CloudMessagesDisabledListener,
			 SearchListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final String SAVE_COMMENT_TYPE = "SAVE_COMMENT_TYPE";

	private static final int LOAD_CONTACTS = 1;

	protected ChatsPublicAdapter publicChatsAdapter;

	private Constants.ChatType chatType;

	private String searchText;

	private TextView emptyView;

	private ContentObserver contentObserver;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			chatType = (Constants.ChatType) savedInstanceState.getSerializable(SAVE_COMMENT_TYPE);
		}
		getLoaderManager().initLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
		contentObserver = new ContentObserver(null) {

			@Override
			public void onChange(boolean selfChange) {
				getLoaderManager().restartLoader(LOAD_CONTACTS, Bundle.EMPTY,
												 ChatsPublicFragment.this);
			}
		};
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_items, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		emptyView = (TextView) view.findViewById(R.id.empty_view);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		publicChatsAdapter = new ChatsPublicAdapter(getActivity(), chatType, this);
		recyclerView.setAdapter(publicChatsAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
		getActivity().getContentResolver().registerContentObserver(ContactsProvider
																	 .getUriContacts(), true,
																   contentObserver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_COMMENT_TYPE, chatType);
	}

	@Override
	public void onPause() {
		super.onPause();
		search(null);
		getActivity().getContentResolver().unregisterContentObserver(contentObserver);
	}

	public Constants.ChatType getChatType() {
		return chatType;
	}

	public void setChatType(Constants.ChatType chatType) {
		this.chatType = chatType;
	}

	@Override
	public void chatClicked(ChatPublicModel chatPublicModel) {
		switch (chatType) {
			case FLATTER:
				ChatPublicActivity.startChatFlatter(getActivity(), chatPublicModel.conversationId,
													chatPublicModel.userId,
													chatPublicModel.userDisplayName,
													chatPublicModel.roomName,
													chatPublicModel.userImageUri);
				break;
			case FORTHRIGHT:
				ChatPublicActivity.startChatForthright(getActivity(),
													   chatPublicModel.conversationId,
													   chatPublicModel.userId,
													   chatPublicModel.userDisplayName,
													   chatPublicModel.roomName,
													   chatPublicModel.userImageUri);
				break;
		}
	}

	@Override
	public boolean onNotificationReceived(Notification notification) {
		getLoaderManager().restartLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
		return true;
	}

	@Override
	public void search(String text) {
		if (!TextUtils.isEmpty(text)) {
			text = text.trim().toLowerCase();
			if (!text.equals(searchText)) {
				searchText = text;
				getLoaderManager().restartLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
			}
		} else if (!TextUtils.isEmpty(searchText)) {
			searchText = null;
			getLoaderManager().restartLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOAD_CONTACTS:
				String selection;
				String[] selectionArgs;
				String order;
				Long userPhone = Application.appUser().getUserPhone();
				if (TextUtils.isEmpty(searchText)) {
					selection = Table.LocalUser.IS_MOBILE_NUMBER + " AND ( " +
								Table.Conversation.TYPE + " IS NULL OR " + Table.Conversation.TYPE +
								"=? ) AND " + Table.LocalUser.PHONE + " != ?";
					selectionArgs = new String[]{chatType.name(), userPhone.toString()};
				} else {
					selection = Table.LocalUser.IS_MOBILE_NUMBER + " AND " +
								Table.LocalUser.CONTACT_NAME + " LIKE ? AND ( " +
								Table.Conversation.TYPE + " IS NULL OR " + Table.Conversation.TYPE +
								"=? ) AND " + Table.LocalUser.PHONE + " != ?";
					selectionArgs = new String[]{"%" + searchText + "%", chatType.name(),
												 userPhone.toString()};
				}
				order = "CASE WHEN " + Table.Conversation.LAST_ACCESS + " IS NULL THEN 0 ELSE " +
						Table.Conversation.LAST_ACCESS + " END DESC, " +
						Table.LocalUser.CONTACT_NAME + " COLLATE NOCASE";
				return new CursorLoader(getActivity(),
										ContactsProvider.getUriContactsConversations(chatType),
										ChatsPublicAdapter.PROJECTION, selection, selectionArgs,
										order);
			// break
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		publicChatsAdapter.setSearch(searchText);
		publicChatsAdapter.setCursor(data);
		publicChatsAdapter.notifyDataSetChanged();
		if (data.getCount() == 0) {
			emptyView.setVisibility(View.VISIBLE);
			if (searchText == null) {
				emptyView.setText(R.string.no_contacts);
			} else {
				emptyView.setText(R.string.nothing_found);
			}
		} else {
			emptyView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
