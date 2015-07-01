package com.livae.ff.app.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.adapter.ChatsFragmentsAdapter;
import com.livae.ff.app.fragment.ChatsFragment;
import com.livae.ff.app.fragment.PublicChatsFragment;
import com.livae.ff.app.listener.SearchListener;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.common.Constants;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

public class ChatsActivity extends AbstractActivity
  implements NotificationDisabledReceiver.CloudMessagesDisabledListener,
			 ViewPager.OnPageChangeListener, SearchView.OnQueryTextListener, View.OnClickListener {

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
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.chats_menu, menu);
		menuInflater.inflate(R.menu.search_menu, menu);

		searchMenuItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchMenuItem.getActionView();
		searchView.setOnQueryTextListener(this);

		return true;
	}

	@Override
	public boolean onNotificationReceived(Notification notification) {
		if (notification.getType() == Constants.PushNotificationType.COMMENT) {
			NotificationComment nc = (NotificationComment) notification;
			Constants.ChatType chatType = Constants.ChatType.valueOf(nc.getConversationType());
			Fragment fragment;
			switch (chatType) {
				case PRIVATE_ANONYMOUS:
				case PRIVATE:
				case SECRET:
					fragment = chatsFragmentsAdapter
								 .getRegisteredFragment(ChatsFragmentsAdapter.CHAT_PRIVATE);
					if (fragment != null) {
						ChatsFragment chatsFragment = (ChatsFragment) fragment;
						return chatsFragment.onNotificationReceived(notification);
					}
					break;
				case FLATTER:
					fragment = chatsFragmentsAdapter
								 .getRegisteredFragment(ChatsFragmentsAdapter.CHAT_FLATTERED);
					if (fragment != null) {
						PublicChatsFragment publicChatsFragment = (PublicChatsFragment) fragment;
						return publicChatsFragment.onNotificationReceived(notification);
					}
					break;
				case FORTHRIGHT:
					fragment = chatsFragmentsAdapter
								 .getRegisteredFragment(ChatsFragmentsAdapter.CHAT_FORTHRIGHT);
					if (fragment != null) {
						PublicChatsFragment publicChatsFragment = (PublicChatsFragment) fragment;
						return publicChatsFragment.onNotificationReceived(notification);
					}
					break;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_feedback:
				// TODO
				break;
			case R.id.menu_settings:
				// TODO
				break;
			case R.id.menu_share:
				// TODO
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
		Analytics.screen(Analytics.Screen.CHATS);
		notificationDisabledReceiver.register(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Application.appUser().getUserPhone() == null) {
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
//			tabLayout.addTab(tabLayout.newTab().setCustomView());
			viewPager = (ViewPager) findViewById(R.id.view_pager);
			viewPager.addOnPageChangeListener(this);
			viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.space_normal));
			viewPager.setPageMarginDrawable(R.color.black_light);
			chatsFragmentsAdapter = new ChatsFragmentsAdapter(getSupportFragmentManager(),
															  getResources());
			viewPager.setAdapter(chatsFragmentsAdapter);
			tabLayout.setupWithViewPager(viewPager);
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

			buttonsTranslationStep = getResources().getDimensionPixelSize(R.dimen.space_huge);
		}
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
				// TODO
				break;
			case R.id.create_chat_secret_button:
				// TODO
				break;
			case R.id.create_chat_anonymous_button:
				// TODO
				break;
		}
	}

	private void hideChatsButtonsContainer() {
		floatingActionButton.animate().alpha(1).start();

		createChatNormalView.animate().alpha(0).translationY(buttonsTranslationStep * 3).start();
		createChatSecretView.animate().alpha(0).translationY(buttonsTranslationStep * 2).start();
		createChatAnonymousView.animate().alpha(0).translationY(buttonsTranslationStep * 1).start();
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
			createChatAnonymousView.setTranslationY(buttonsTranslationStep * 1);
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
}
