package com.livae.ff.app.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.adapter.ChatsFragmentsAdapter;
import com.livae.ff.app.fragment.ChatsPrivateFragment;
import com.livae.ff.app.fragment.ChatsPublicFragment;
import com.livae.ff.app.listener.SearchListener;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.app.settings.Chats;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.IntentUtils;
import com.livae.ff.app.utils.SyncUtils;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

public class ChatsActivity extends AbstractActivity
  implements NotificationDisabledReceiver.CloudMessagesDisabledListener,
			 ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener, View.OnClickListener,
			 LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOADER_UNREAD_PRIVATE = 6800;

	private static final int LOADER_UNREAD_FLATTER = 6801;

	private static final int LOADER_UNREAD_FORTHRIGHT = 6802;

	private static final int REQUEST_CONTACT_PRIVATE = 6901;

	private static final int REQUEST_CONTACT_SECRET = 6902;

	private static final int REQUEST_CONTACT_ANONYMOUS = 6903;

	private NotificationDisabledReceiver notificationDisabledReceiver;

	private ChatsFragmentsAdapter chatsFragmentsAdapter;

	private FloatingActionButton floatingActionButton;

	private View createChatNormalView;

	private View createChatSecretView;

	private View createChatAnonymousView;

	private ArgbEvaluator argbEvaluator;

	private int colorFlatterer;

	private int colorForthright;

	private int colorPrivate;

	private int colorFlattererDarker;

	private int colorForthrightDarker;

	private int colorPrivateDarker;

	private TabLayout tabLayout;

	private Toolbar toolbar;

	private MenuItem searchMenuItem;

	private ViewPager viewPager;

	private String searchText;

	private View addChatsContainer;

	private int buttonsTranslationStep;

	private View[] tabs;

	public static void start(Activity activity) {
		Intent intent = new Intent(activity, ChatsActivity.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity)
														  .toBundle());
		} else {
			activity.startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_chats, menu);
		menuInflater.inflate(R.menu.menu_search, menu);

		searchMenuItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchMenuItem.getActionView();
		searchView.setOnQueryTextListener(this);

		return true;
	}

	@Override
	public boolean onNotificationReceived(Notification notification) {
		if (notification.getType() == Constants.PushNotificationType.COMMENT) {
			NotificationComment nc = (NotificationComment) notification;
			// increase unread count of conversation
			Long conversationId = nc.getConversationId();
			if (!nc.getIsMe()) {
				Uri uriConversation = ConversationsProvider
										.getUriConversationIncreaseUnread(conversationId);
				getContentResolver().update(uriConversation, null, null, null);
			}
			// notify to listeners
			ChatType chatType = ChatType.valueOf(nc.getConversationType());
			Fragment fragment;
			Chats chats = Application.appUser().getChats();
			switch (chatType) {
				case PRIVATE_ANONYMOUS:
				case PRIVATE:
				case SECRET:
					getSupportLoaderManager().restartLoader(LOADER_UNREAD_PRIVATE, null, this);
					fragment = chatsFragmentsAdapter
								 .getRegisteredFragment(ChatsFragmentsAdapter.CHAT_PRIVATE);
					if (fragment != null) {
						ChatsPrivateFragment chatsFragment = (ChatsPrivateFragment) fragment;
						return chatsFragment.onNotificationReceived(notification);
					}
					break;
				case FLATTER:
					if (conversationId.equals(chats.getChatFlatterId())) {
						chats.increaseChatFlatterUnread();
					}
					getSupportLoaderManager().restartLoader(LOADER_UNREAD_FLATTER, null, this);
					fragment = chatsFragmentsAdapter
								 .getRegisteredFragment(ChatsFragmentsAdapter.CHAT_FLATTERED);
					if (fragment != null) {
						ChatsPublicFragment chatsPublicFragment = (ChatsPublicFragment) fragment;
						return chatsPublicFragment.onNotificationReceived(notification);
					}
					break;
				case FORTHRIGHT:
					if (conversationId.equals(chats.getChatForthrightId())) {
						chats.increaseChatForthrightUnread();
					}
					getSupportLoaderManager().restartLoader(LOADER_UNREAD_FORTHRIGHT, null, this);
					fragment = chatsFragmentsAdapter
								 .getRegisteredFragment(ChatsFragmentsAdapter.CHAT_FORTHRIGHT);
					if (fragment != null) {
						ChatsPublicFragment chatsPublicFragment = (ChatsPublicFragment) fragment;
						return chatsPublicFragment.onNotificationReceived(notification);
					}
					break;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
//			case R.id.menu_settings:
//				break;
			case R.id.menu_refresh_contacts:
				SyncUtils.syncContactsNow();
				break;
			case R.id.menu_share:
				IntentUtils.shareApp(this);
				break;
			case R.id.menu_rate:
				IntentUtils.rateApp(this);
				break;
			case R.id.menu_feedback:
				IntentUtils.sendFeedback(this);
				break;
			default:
				return super.onOptionsItemSelected(menuItem);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (addChatsContainer.getVisibility() == View.VISIBLE) {
			hideChatsButtonsContainer();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		NotificationManager manager;
		manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(Settings.Notifications.ID_CHAT_PRIVATE);
		manager.cancel(Settings.Notifications.ID_CHAT_PUBLIC_FLATTER);
		manager.cancel(Settings.Notifications.ID_CHAT_PUBLIC_FORTHRIGHT);
		Analytics.screen(Analytics.Screen.CHATS);
		notificationDisabledReceiver.register(this);
		LoaderManager loaderManager = getSupportLoaderManager();
		loaderManager.restartLoader(LOADER_UNREAD_FLATTER, null, this);
		loaderManager.restartLoader(LOADER_UNREAD_FORTHRIGHT, null, this);
		loaderManager.restartLoader(LOADER_UNREAD_PRIVATE, null, this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (android.text.TextUtils.isEmpty(Application.appUser().getAccessToken())) {
			OnBoardingActivity.start(this);
			finish();
		} else {
			notificationDisabledReceiver = new NotificationDisabledReceiver();
			notificationDisabledReceiver.setListener(this);

			setContentView(R.layout.activity_chats);
			floatingActionButton = (FloatingActionButton) findViewById(R.id.create_chat_button);
			argbEvaluator = new ArgbEvaluator();
			Resources res = getResources();
			colorFlatterer = res.getColor(R.color.pink);
			colorPrivate = res.getColor(R.color.purple);
			colorForthright = res.getColor(R.color.deep_purple);
			colorFlattererDarker = res.getColor(R.color.pink_dark);
			colorPrivateDarker = res.getColor(R.color.purple_dark);
			colorForthrightDarker = res.getColor(R.color.deep_purple_dark);

			toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			tabLayout = (TabLayout) findViewById(R.id.tab_layout);
			chatsFragmentsAdapter = new ChatsFragmentsAdapter(getSupportFragmentManager(),
															  getResources());
			tabs = new View[chatsFragmentsAdapter.getCount()];
			LayoutInflater inflater = getLayoutInflater();
			for (int i = 0; i < tabs.length; i++) {
				tabs[i] = inflater.inflate(R.layout.view_chats_tab, tabLayout, false);
				tabLayout.addTab(tabLayout.newTab().setCustomView(tabs[i]));
				TextView text = (TextView) tabs[i].findViewById(R.id.title);
				text.setText(chatsFragmentsAdapter.getPageTitle(i));
			}
			viewPager = (ViewPager) findViewById(R.id.view_pager);
			viewPager.addOnPageChangeListener(this);
			viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.space_normal));
			viewPager.setPageMarginDrawable(R.color.black_light);
			viewPager.setAdapter(chatsFragmentsAdapter);
//			tabLayout.setupWithViewPager(viewPager);
			viewPager
			  .addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
			viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset,
										   int positionOffsetPixels) {
					if (positionOffset == 0) {
						onPageSelected(position);
					} else {
						tabs[position].setAlpha(1 - 0.2f * positionOffset);
						tabs[position + 1].setAlpha(0.8f + 0.2f * positionOffset);
					}
				}

				@Override
				public void onPageSelected(int position) {
					for (View view : tabs) {
						view.setAlpha(0.8f);
					}
					tabs[position].setAlpha(1);
				}

				@Override
				public void onPageScrollStateChanged(int state) {

				}
			});
			tabLayout
			  .setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
			viewPager.setCurrentItem(ChatsFragmentsAdapter.CHAT_PRIVATE);

			addChatsContainer = findViewById(R.id.add_chat_container);
			createChatNormalView = findViewById(R.id.create_chat_normal);
			createChatSecretView = findViewById(R.id.create_chat_secret);
			createChatAnonymousView = findViewById(R.id.create_chat_anonymous);

			floatingActionButton.setOnClickListener(this);
			addChatsContainer.setOnClickListener(this);
			findViewById(R.id.create_chat_normal_button).setOnClickListener(this);
			findViewById(R.id.create_chat_secret_button).setOnClickListener(this);
			findViewById(R.id.create_chat_anonymous_button).setOnClickListener(this);
			findViewById(R.id.close_button).setOnClickListener(this);

			buttonsTranslationStep = getResources().getDimensionPixelSize(R.dimen.space_enormous);

			LoaderManager loaderManager = getSupportLoaderManager();
			loaderManager.initLoader(LOADER_UNREAD_FLATTER, null, this);
			loaderManager.initLoader(LOADER_UNREAD_FORTHRIGHT, null, this);
			loaderManager.initLoader(LOADER_UNREAD_PRIVATE, null, this);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		searchText = newText;
		onPageSelected(viewPager.getCurrentItem());
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.create_chat_button:
				showChatsButtonsContainer();
				break;
			case R.id.close_button:
			case R.id.add_chat_container:
				hideChatsButtonsContainer();
				break;
			case R.id.create_chat_normal_button:
				startActivityForResult(new Intent(this, ContactsActivity.class),
									   REQUEST_CONTACT_PRIVATE);
				break;
			case R.id.create_chat_secret_button:
				startActivityForResult(new Intent(this, ContactsActivity.class),
									   REQUEST_CONTACT_SECRET);
				break;
			case R.id.create_chat_anonymous_button:
				startActivityForResult(new Intent(this, ContactsActivity.class),
									   REQUEST_CONTACT_ANONYMOUS);
				break;
		}
	}

	private void hideChatsButtonsContainer() {
		floatingActionButton.animate().alpha(1).start();

		createChatNormalView.animate().alpha(0).translationY(buttonsTranslationStep * 3).start();
		createChatSecretView.animate().alpha(0).translationY(buttonsTranslationStep * 2).start();
		createChatAnonymousView.animate().alpha(0).translationY(buttonsTranslationStep).start();
		addChatsContainer.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				addChatsContainer.setVisibility(View.GONE);
			}
		}).start();
	}

	private void showChatsButtonsContainer() {
		floatingActionButton.animate().alpha(0).start();

		if (createChatNormalView.getTranslationY() == 0) {
			createChatNormalView.setAlpha(0f);
			createChatNormalView.setTranslationY(buttonsTranslationStep * 3);
		}
		if (createChatSecretView.getTranslationY() == 0) {
			createChatSecretView.setAlpha(0f);
			createChatSecretView.setTranslationY(buttonsTranslationStep * 2);
		}
		if (createChatAnonymousView.getTranslationY() == 0) {
			createChatAnonymousView.setAlpha(0f);
			createChatAnonymousView.setTranslationY(buttonsTranslationStep);
		}
		createChatNormalView.animate().alpha(1).translationY(0).start();
		createChatSecretView.animate().alpha(1).translationY(0).start();
		createChatAnonymousView.animate().alpha(1).translationY(0).start();
		if (addChatsContainer.getAlpha() == 1) {
			addChatsContainer.setAlpha(0f);
		}
		addChatsContainer.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				addChatsContainer.setVisibility(View.VISIBLE);
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			hideChatsButtonsContainer();
			final long phone = data.getLongExtra(ContactsActivity.SELECTED_PHONE, 0);
			final String name = data.getStringExtra(ContactsActivity.SELECTED_DISPLAY_NAME);
			switch (requestCode) {
				case REQUEST_CONTACT_PRIVATE:
					ChatPrivateActivity.startChatPrivate(this, phone, name);
					break;
				case REQUEST_CONTACT_SECRET:
					ChatPrivateActivity.startChatSecret(this, phone, name);
					break;
				case REQUEST_CONTACT_ANONYMOUS:
					ChatPrivateActivity.startChatAnonymous(this, phone, name);
					break;
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		notificationDisabledReceiver.unregister(this);
		if (searchMenuItem != null) {
			SearchView searchView = (SearchView) searchMenuItem.getActionView();
			searchView.setIconified(true);
		}
		searchText = null;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String[] projection = {Table.Conversation.T_ID};
		String selection = null;
		String[] selectionArgs = null;
		switch (id) {
			case LOADER_UNREAD_FLATTER:
				selection = Table.Conversation.UNREAD + ">0 AND " + Table.Conversation.TYPE + "=?";
				selectionArgs = new String[]{ChatType.FLATTER.name()};
				return new CursorLoader(this, ConversationsProvider.getUriConversations(),
										projection, selection, selectionArgs, null);
			case LOADER_UNREAD_FORTHRIGHT:
				selection = Table.Conversation.UNREAD + ">0 AND " + Table.Conversation.TYPE + "=?";
				selectionArgs = new String[]{ChatType.FORTHRIGHT.name()};
				return new CursorLoader(this, ConversationsProvider.getUriConversations(),
										projection, selection, selectionArgs, null);
			case LOADER_UNREAD_PRIVATE:
				selection = Table.Conversation.UNREAD + " > 0 AND " +
							Table.Conversation.TYPE + " IN (?,?,?)";
				selectionArgs = new String[]{ChatType.SECRET.name(), ChatType.PRIVATE.name(),
											 ChatType.PRIVATE_ANONYMOUS.name()};
				return new CursorLoader(this, ConversationsProvider.getUriConversations(),
										projection, selection, selectionArgs, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int count = data.getCount();
		int pos = -1;
		switch (loader.getId()) {
			case LOADER_UNREAD_FLATTER:
				pos = ChatsFragmentsAdapter.CHAT_FLATTERED;
				break;
			case LOADER_UNREAD_FORTHRIGHT:
				pos = ChatsFragmentsAdapter.CHAT_FORTHRIGHT;
				break;
			case LOADER_UNREAD_PRIVATE:
				pos = ChatsFragmentsAdapter.CHAT_PRIVATE;
				break;
		}
		if (pos != -1) {
			TextView text = (TextView) tabs[pos].findViewById(R.id.unread_count);
			if (count == 0) {
				text.setVisibility(View.GONE);
				text.setText(null);
			} else {
				text.setVisibility(View.VISIBLE);
				text.setText(Integer.toString(count));
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// nothing
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		int color = colorPrivate;
		int statusBarColor = colorPrivate;
		switch (position) {
			case ChatsFragmentsAdapter.CHAT_FLATTERED:
				color = (Integer) argbEvaluator.evaluate(positionOffset, colorFlatterer,
														 colorPrivate);
				statusBarColor = (Integer) argbEvaluator.evaluate(positionOffset,
																  colorFlattererDarker,
																  colorPrivateDarker);
				if (positionOffsetPixels > 0) {
					int totalPixels = (int) (positionOffsetPixels / positionOffset);
					floatingActionButton.setTranslationX(totalPixels - positionOffsetPixels);
				}
				break;
			case ChatsFragmentsAdapter.CHAT_PRIVATE:
				color = (Integer) argbEvaluator.evaluate(positionOffset, colorPrivate,
														 colorForthright);
				statusBarColor = (Integer) argbEvaluator.evaluate(positionOffset,
																  colorPrivateDarker,
																  colorForthrightDarker);
				floatingActionButton.setTranslationX(-positionOffsetPixels);
				break;
			case ChatsFragmentsAdapter.CHAT_FORTHRIGHT:
				color = colorForthright;
				statusBarColor = colorForthrightDarker;
				break;
		}
		toolbar.setBackgroundColor(color);
		tabLayout.setBackgroundColor(color);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(statusBarColor);
		}
	}

	@Override
	public void onPageSelected(int position) {
		Fragment fragment = chatsFragmentsAdapter.getRegisteredFragment(position);
		if (fragment != null && fragment instanceof SearchListener) {
			SearchListener searchListener = (SearchListener) fragment;
			searchListener.search(searchText);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

}
