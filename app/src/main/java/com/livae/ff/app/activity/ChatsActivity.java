package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.adapter.ChatsFragmentsAdapter;
import com.livae.ff.app.fragment.ChatsFragment;
import com.livae.ff.app.fragment.PublicChatsFragment;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.common.Constants;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

public class ChatsActivity extends AbstractActivity
  implements NotificationDisabledReceiver.CloudMessagesDisabledListener,
			 ViewPager.OnPageChangeListener {

	private NotificationDisabledReceiver notificationDisabledReceiver;

	private ChatsFragmentsAdapter chatsFragmentsAdapter;

	private FloatingActionButton floatingActionButton;

	private int floatingActionButtonTranslation;

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chats_menu, menu);
		getMenuInflater().inflate(R.menu.search_menu, menu);
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
			floatingActionButton.post(new Runnable() {
				@Override
				public void run() {
					ViewGroup.MarginLayoutParams mlp;
					mlp = (ViewGroup.MarginLayoutParams) floatingActionButton.getLayoutParams();
					floatingActionButtonTranslation =
					  floatingActionButton.getWidth() + mlp.rightMargin;
				}
			});

			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
//			tabLayout.addTab(tabLayout.newTab().setCustomView());
			ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
			viewPager.addOnPageChangeListener(this);
			chatsFragmentsAdapter = new ChatsFragmentsAdapter(getSupportFragmentManager(),
															  getResources());
			viewPager.setAdapter(chatsFragmentsAdapter);
			tabLayout.setupWithViewPager(viewPager);
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		if (position == ChatsFragmentsAdapter.CHAT_PRIVATE - 1) {
			floatingActionButton.setTranslationX(floatingActionButtonTranslation *
												 (1 - positionOffset));
		}
	}

	@Override
	public void onPageSelected(int position) {
		if (position != ChatsFragmentsAdapter.CHAT_PRIVATE) {
			floatingActionButton.setVisibility(View.GONE);
			floatingActionButton.setTranslationX(0);
		} else {
			floatingActionButton.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}
}
