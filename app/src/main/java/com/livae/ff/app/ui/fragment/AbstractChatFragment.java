package com.livae.ff.app.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.R;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.listener.CommentClickListener;
import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.app.service.NotificationService;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.QueryId;
import com.livae.ff.app.task.TaskConversationJoin;
import com.livae.ff.app.task.TaskConversationLeave;
import com.livae.ff.app.task.TaskPostComment;
import com.livae.ff.app.task.TextId;
import com.livae.ff.app.ui.activity.AbstractActivity;
import com.livae.ff.app.ui.activity.AbstractChatActivity;
import com.livae.ff.app.ui.adapter.CommentsAdapter;
import com.livae.ff.app.ui.adapter.CursorAdapter;
import com.livae.ff.app.ui.dialog.NotificationColorDialogFragment;
import com.livae.ff.app.ui.dialog.NotificationMuteDialogFragment;
import com.livae.ff.app.ui.viewholders.CommentViewHolder;
import com.livae.ff.app.utils.AnimUtils;
import com.livae.ff.app.utils.UnitUtils;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

public abstract class AbstractChatFragment
  extends AbstractEndlessCursorLoaderFragment<CommentViewHolder>
  implements View.OnClickListener, NotificationDisabledReceiver.CloudMessagesDisabledListener,
			 CommentClickListener {

	protected static final int LOADER_CONVERSATION_ID = LOADER_ITEM;

	private static final int RESULT_SELECT_SOUND = 9999;

	protected Long conversationId;

	protected Long conversationPhone;

	protected String anonymousNick;

	protected ChatType chatType;

	protected CommentsAdapter commentsAdapter;

	protected String userImageUri;

	protected String userName;

	protected Long lastAccess;

	protected Long lastMessage;

	protected Integer unreadMessages;

	protected Long rawContactId;

	private FloatingActionButton buttonPostComment;

	private EditText commentText;

	private View commentPostContainer;

	private ContentObserver contentObserver;

	private NotificationDisabledReceiver notificationDisabledReceiver;

	private TextView dateText;

	private MenuItem menuNotifications;

	private Integer notificationColor;

	private String notificationSoundUri;

	private Long notificationMute;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras.containsKey(AbstractChatActivity.EXTRA_CONVERSATION_ID)) {
			conversationId = extras.getLong(AbstractChatActivity.EXTRA_CONVERSATION_ID);
		}
		chatType = (ChatType) extras.getSerializable(AbstractChatActivity.EXTRA_CHAT_TYPE);
		anonymousNick = extras.getString(AbstractChatActivity.EXTRA_ROOM_NAME, null);
		conversationPhone = extras.getLong(AbstractChatActivity.EXTRA_PHONE_NUMBER);
		userImageUri = extras.getString(AbstractChatActivity.EXTRA_IMAGE_URI);
		userName = extras.getString(AbstractChatActivity.EXTRA_DISPLAY_NAME);
		if (extras.containsKey(AbstractChatActivity.EXTRA_LAST_ACCESS_DATE)) {
			lastAccess = extras.getLong(AbstractChatActivity.EXTRA_LAST_ACCESS_DATE);
		}
		if (extras.containsKey(AbstractChatActivity.EXTRA_LAST_MESSAGE_DATE)) {
			lastMessage = extras.getLong(AbstractChatActivity.EXTRA_LAST_MESSAGE_DATE);
		}
		if (extras.containsKey(AbstractChatActivity.EXTRA_UNREAD_MESSAGES)) {
			unreadMessages = extras.getInt(AbstractChatActivity.EXTRA_UNREAD_MESSAGES);
		}
		if (extras.containsKey(AbstractChatActivity.EXTRA_USER_RAW_CONTACT_ID)) {
			rawContactId = extras.getLong(AbstractChatActivity.EXTRA_USER_RAW_CONTACT_ID);
		}
		super.onCreate(savedInstanceState);
		notificationDisabledReceiver = new NotificationDisabledReceiver();
		notificationDisabledReceiver.setListener(this);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_comments, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.center_progressbar).setVisibility(View.VISIBLE);
		buttonPostComment = (FloatingActionButton) view.findViewById(R.id.button_post_comment);
		commentText = (EditText) view.findViewById(R.id.comment_text);
		commentPostContainer = view.findViewById(R.id.comment_post);
		dateText = (TextView) view.findViewById(R.id.day_date);
		buttonPostComment.setOnClickListener(this);
		commentPostContainer.setVisibility(View.GONE);
		buttonPostComment.setVisibility(View.GONE);
		LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
		layoutManager.setReverseLayout(true);
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if (commentsAdapter.getItemCount() > 1) {
					LinearLayoutManager layoutManager;
					layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
					int lastComplete = layoutManager.findLastCompletelyVisibleItemPosition();
					if (lastComplete > 0 && lastComplete < commentsAdapter.getItemCount()) {
						long dateLastVisible = commentsAdapter.getDate(lastComplete);
						dateText.setVisibility(View.VISIBLE);
						dateText.setText(UnitUtils.getDate(getActivity(), dateLastVisible));
					} else {
						dateText.setVisibility(View.GONE);
					}
				}
			}
		});
	}

	@Override
	protected Uri getUriCursor() {
		return ConversationsProvider.getUriConversationComments(conversationId);
	}

	@Override
	protected CursorAdapter<CommentViewHolder> getAdapter() {
		Long myPhone = Application.appUser().getUserPhone();
		boolean isMyPublicChat = conversationPhone != null && conversationPhone.equals(myPhone) &&
								 (ChatType.FLATTER == chatType || ChatType.FORTHRIGHT == chatType);
		commentsAdapter =
		  new CommentsAdapter(this, this, chatType, userName, userImageUri, isMyPublicChat);
		return commentsAdapter;
	}

	@Override
	protected String[] getProjection() {
		return CommentsAdapter.PROJECTION;
	}

	@Override
	protected String getSelect() {
		return null;
	}

	@Override
	protected String[] getSelectArgs() {
		return new String[0];
	}

	@Override
	protected String getOrderString() {
		return "-" + Table.Comment.DATE;
	}

	@Override
	protected QueryId getBaseQueryParams() {
		return new QueryId(conversationId);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
			case LOADER_ITEMS:
				return new CursorLoader(getActivity(), getUriCursor(), getProjection(), null, null,
										Integer.toString(getTotalLoaded()));
			// break
			case LOADER_CONVERSATION_ID:
				return new CursorLoader(getActivity(),
										ConversationsProvider.getUriConversation(conversationId),
										null, null, null, null);
			// break
		}
		return super.onCreateLoader(id, bundle);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		switch (objectLoader.getId()) {
			case LOADER_CONVERSATION_ID:
				if (cursor.moveToFirst()) {
					final AbstractChatActivity activity = (AbstractChatActivity) getActivity();
					final int iRoomName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
					anonymousNick = cursor.getString(iRoomName);
					final int iDisplayName = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
					final String displayName = cursor.getString(iDisplayName);
					final int iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
					String imageUri = null;
					if (!cursor.isNull(iImageUri)) {
						imageUri = cursor.getString(iImageUri);
					}
					final int iAnonymousId = cursor.getColumnIndex(Table.Conversation.ALIAS_ID);
					Long anonymousId = null;
					if (!cursor.isNull(iAnonymousId)) {
						anonymousId = cursor.getLong(iAnonymousId);
					}
					final int iPhoneNumber = cursor.getColumnIndex(Table.Conversation.PHONE);
					if (!cursor.isNull(iPhoneNumber)) {
						conversationPhone = cursor.getLong(iPhoneNumber);
					}
					activity.bindToolbar(anonymousNick, displayName, imageUri, anonymousId,
										 conversationPhone);

					final ContentResolver contentResolver = activity.getContentResolver();
					final Uri uriConversation =
					  ConversationsProvider.getUriConversation(conversationId);
					final ContentValues contentValues = new ContentValues();
					contentValues.put(Table.Conversation.LAST_ACCESS, System.currentTimeMillis());
					contentResolver.update(uriConversation, contentValues, null, null);
					updateNotifications();
					// set menu notifications
					int iMute = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_MUTED);
					notificationMute = cursor.isNull(iMute) ? null : cursor.getLong(iMute);
					if (menuNotifications != null) {
						updateMenuNotifications();
					}
					int iNotfColor = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_COLOR);
					notificationColor =
					  cursor.isNull(iNotfColor) ? null : cursor.getInt(iNotfColor);
					int iNotfSound = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_SOUND);
					notificationSoundUri =
					  cursor.isNull(iNotfSound) ? null : cursor.getString(iNotfSound);
				}
				break;
			default:
				super.onLoadFinished(objectLoader, cursor);
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		notificationDisabledReceiver.register(getActivity());
		if (conversationId == null) {
			getConversation();
		} else {
			startConversation();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		notificationDisabledReceiver.unregister(getActivity());
		final ContentResolver cr = getActivity().getContentResolver();
		if (contentObserver != null) {
			cr.unregisterContentObserver(contentObserver);
		}
		if (conversationId != null) {
			leaveConversation();
		}
	}

	private void updateMenuNotifications() {
		this.menuNotifications.setEnabled(conversationId != null);
		Long mute = notificationMute;
		boolean isMute = mute != null && (mute < 0 || mute > System.currentTimeMillis());
		final SubMenu subMenu = menuNotifications.getSubMenu();
		subMenu.findItem(R.id.action_notification_mute).setVisible(!isMute);
		subMenu.findItem(R.id.action_notification_unmute).setVisible(isMute);
	}

	private void updateNotifications() {
		//noinspection PointlessBooleanExpression,ConstantConditions
		if (chatType != null && !BuildConfig.TEST) {
			final FragmentActivity activity = getActivity();
			switch (chatType) {
				case FLATTER:
					NotificationService.notifyChatsPublic(activity, ChatType.FLATTER, false);
					break;
				case FORTHRIGHT:
					NotificationService.notifyChatsPublic(activity, ChatType.FORTHRIGHT, false);
					break;
				case PRIVATE:
				case PRIVATE_ANONYMOUS:
				case SECRET:
					NotificationService.notifyChatsPrivate(activity, false);
					break;
			}
		}
	}

	abstract protected void getConversation();

	protected void startConversation() {
		reloadCursor();
		getLoaderManager().restartLoader(LOADER_CONVERSATION_ID, Bundle.EMPTY,
										 AbstractChatFragment.this);
		registerContentObserver();
		joinConversation();
	}

	private void registerContentObserver() {
		if (conversationId != null) {

			if (contentObserver == null) {
				contentObserver = new ContentObserver(new Handler()) {

					@Override
					public void onChange(boolean selfChange) {
						reloadCursor();
					}
				};
			}
			final ContentResolver cr = getActivity().getContentResolver();
			cr.registerContentObserver(ConversationsProvider.getUriConversation(conversationId),
									   true, contentObserver);
		}
	}

	private void joinConversation() {
		switch (chatType) {
			case PRIVATE_ANONYMOUS:
			case SECRET:
			case PRIVATE:
				startLoading();
				break;
		}
		TaskConversationJoin task = new TaskConversationJoin(this);
		task.execute(conversationId, new Callback<LifeCycle, Long, Void>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle, Long aLong, Void aVoid) {
				AbstractChatFragment f = (AbstractChatFragment) lifeCycle;
				switch (f.chatType) {
					case FLATTER:
					case FORTHRIGHT:
						f.startLoading();
						break;
				}
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle, Long aLong, @NonNull Exception e) {
				AbstractChatFragment f = (AbstractChatFragment) lifeCycle;
				switch (f.chatType) {
					case FLATTER:
					case FORTHRIGHT:
						AbstractActivity activity = (AbstractActivity) f.getActivity();
						activity.showSnackBarException(e);
						break;
				}
			}
		});
		ContentValues values = new ContentValues();
		values.put(Table.Conversation.UNREAD, 0);
		Uri uri = ConversationsProvider.getUriConversation(conversationId);
		getActivity().getContentResolver().update(uri, values, null, null);
	}

	private void leaveConversation() {
		new TaskConversationLeave(this).execute(conversationId, null);
	}

	private void postComment(String comment) {
		TextId textId = new TextId(comment, conversationId, anonymousNick);
		buttonPostComment.setEnabled(false);
		scrollToPosition(0, true);
		TaskPostComment task = new TaskPostComment(this);
		task.execute(textId, new Callback<LifeCycle, TextId, Comment>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle, TextId textId, Comment comment) {
				AbstractChatFragment f = (AbstractChatFragment) lifeCycle;
				switch (chatType) {
					case FLATTER:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_FLATTER);
						break;
					case FORTHRIGHT:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_FORTHRIGHT);
						break;
					case PRIVATE_ANONYMOUS:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_ANONYMOUS);
						break;
					case PRIVATE:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_PRIVATE);
						break;
					case SECRET:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_SECRET);
						break;
				}
				f.setTotalLoaded(f.getTotalLoaded() + 1);
				f.reloadCursor();
				f.commentText.setEnabled(true);
				f.buttonPostComment.setEnabled(true);
				f.commentText.setText("");
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle, TextId textId, @NonNull Exception
																			   e) {
				AbstractChatFragment f = (AbstractChatFragment) lifeCycle;
				f.commentText.setEnabled(true);
				f.buttonPostComment.setEnabled(true);
				AbstractActivity activity = (AbstractActivity) f.getActivity();
				if (e instanceof GoogleJsonResponseException) {
					GoogleJsonResponseException ge = (GoogleJsonResponseException) e;
					if (ge.getDetails() != null) {
						activity.showSnackBarException(e);
					} else {
						activity.showSnackBarException(getString(R.string.error_unknown,
																 e.getMessage()));
					}
				} else {
					activity.showSnackBarException(e);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_post_comment:
				String comment = commentText.getText().toString().trim();
				int length = comment.length();
				if (length > 0) {
					postComment(comment);
				} else {
					commentText.setText(comment);
				}
				break;
		}
	}

	@Override
	public boolean onNotificationReceived(Notification notification) {
		if (notification instanceof NotificationComment) {
			NotificationComment nc = (NotificationComment) notification;
			if (conversationId != null && conversationId.equals(nc.getConversationId())) {
				if (!nc.getIsMe()) {
					setTotalLoaded(getTotalLoaded() + 1);
				}
				reloadCursor();
				return true;
			}
		}
		return false;
	}

	protected void showSendMessagesPanel() {
		if (commentPostContainer.getVisibility() != View.VISIBLE) {
			Resources res = getResources();
			int height = commentPostContainer.getHeight();
			int margin = res.getDimensionPixelSize(R.dimen.space_normal) * 2;
			AnimUtils.build(commentPostContainer)
					 .alpha(0.2f, 1f)
					 .translateY(height + margin, 0)
					 .accelerateDecelerate()
					 .start();
			height = buttonPostComment.getHeight();
			AnimUtils.build(buttonPostComment)
					 .alpha(0.2f, 1f)
					 .translateY(height + margin, 0)
					 .accelerateDecelerate()
					 .start();
			commentPostContainer.setVisibility(View.VISIBLE);
			buttonPostComment.setVisibility(View.VISIBLE);
		}
	}

	protected void hideSendMessagesPanel() {
		if (commentPostContainer.getVisibility() == View.VISIBLE) {
			Resources res = getResources();
			int height = commentPostContainer.getHeight();
			int margin = res.getDimensionPixelSize(R.dimen.space_normal) * 2;
			AnimUtils.build(commentPostContainer)
					 .alpha(1f, 0.2f)
					 .translateY(0, height + margin)
					 .accelerateDecelerate()
					 .start();
			height = buttonPostComment.getHeight();
			AnimUtils.build(buttonPostComment)
					 .alpha(1f, 0.2f)
					 .translateY(0, height + margin)
					 .accelerateDecelerate()
					 .setListener(new AnimatorListenerAdapter() {
						 @Override
						 public void onAnimationEnd(Animator animation) {
							 commentPostContainer.setVisibility(View.GONE);
							 buttonPostComment.setVisibility(View.GONE);
						 }
					 })
					 .start();
		}
	}

	private void notificationsSelectColor() {
		new NotificationColorDialogFragment() {

			@Override
			public void onColorSelected(Integer color) {
				notificationColor = color;
				ContentValues values = new ContentValues();
				if (color == null) {
					values.putNull(Table.Conversation.NOTIFICATION_COLOR);
				} else {
					values.put(Table.Conversation.NOTIFICATION_COLOR, color);
				}
				final ContentResolver contentResolver = getActivity().getContentResolver();
				contentResolver.update(ConversationsProvider.getUriConversation(conversationId),
									   values, null, null);
				AbstractActivity activity = (AbstractActivity) getActivity();
				activity.showSnackBarMessage(R.string.notification_color_update);
			}
		}.show(getFragmentManager(), notificationColor);
	}

	private void notificationsSelectSound() {
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
						getString(R.string.select_notification_sound));
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
		Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri);
		Uri currentUri = defaultUri;
		if (notificationSoundUri != null) {
			currentUri = Uri.parse(notificationSoundUri);
		}
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri);
		startActivityForResult(intent, RESULT_SELECT_SOUND);
	}

	private void notificationsSelectMute() {
		new NotificationMuteDialogFragment() {

			@Override
			public void onMuteSelected(long mutedTime) {
				if (mutedTime > 0) {
					mutedTime = System.currentTimeMillis() + mutedTime;
				}
				ContentValues values = new ContentValues();
				values.put(Table.Conversation.NOTIFICATION_MUTED, mutedTime);
				final ContentResolver contentResolver = getActivity().getContentResolver();
				contentResolver.update(ConversationsProvider.getUriConversation(conversationId),
									   values, null, null);
				notificationMute = mutedTime;
				updateMenuNotifications();
				AbstractActivity activity = (AbstractActivity) getActivity();
				activity.showSnackBarMessage(R.string.notification_muted);

			}
		}.show(getFragmentManager(), null);
	}

	private void notificationsSelectUnmute() {
		ContentValues values = new ContentValues();
		values.putNull(Table.Conversation.NOTIFICATION_MUTED);
		final ContentResolver contentResolver = getActivity().getContentResolver();
		contentResolver.update(ConversationsProvider.getUriConversation(conversationId), values,
							   null, null);
		notificationMute = null;
		updateMenuNotifications();
		AbstractActivity activity = (AbstractActivity) getActivity();
		activity.showSnackBarMessage(R.string.notification_cancel_muted);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == RESULT_SELECT_SOUND) {
			Bundle extras = data.getExtras();
			//noinspection ConstantConditions
			String uriSelected = extras.get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI).toString();
			if (uriSelected == null) {
				return;
			}
			Uri defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			notificationSoundUri = uriSelected;
			if (notificationSoundUri.equals(defaultUri.toString())) {
				notificationSoundUri = null;
			}
			ContentValues values = new ContentValues();
			if (notificationSoundUri == null) {
				values.putNull(Table.Conversation.NOTIFICATION_SOUND);
			} else {
				values.put(Table.Conversation.NOTIFICATION_SOUND, notificationSoundUri);
			}
			final ContentResolver contentResolver = getActivity().getContentResolver();
			contentResolver.update(ConversationsProvider.getUriConversation(conversationId),
								   values,
								   null, null);
			AbstractActivity activity = (AbstractActivity) getActivity();
			activity.showSnackBarMessage(R.string.notification_sound_updated);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_notifications, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		this.menuNotifications = menu.findItem(R.id.action_notifications);
		updateMenuNotifications();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_notification_color:
				notificationsSelectColor();
				return true;
			case R.id.action_notification_sound:
				notificationsSelectSound();
				return true;
			case R.id.action_notification_mute:
				notificationsSelectMute();
				return true;
			case R.id.action_notification_unmute:
				notificationsSelectUnmute();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
