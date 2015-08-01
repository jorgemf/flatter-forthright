package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.PhoneUtils;
import com.livae.ff.app.view.AnonymousImage;
import com.livae.ff.common.Constants.ChatType;

import javax.annotation.Nonnull;

public class AbstractChatActivity extends AbstractActivity {

	public static final String EXTRA_CONVERSATION_ID = "EXTRA_CONVERSATION_ID";

	public static final String EXTRA_CHAT_TYPE = "EXTRA_CHAT_TYPE";

	public static final String EXTRA_DISPLAY_NAME = "EXTRA_DISPLAY_NAME";

	public static final String EXTRA_ROOM_NAME = "EXTRA_ROOM_NAME";

	public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";

	public static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";

	public static final String EXTRA_IMAGE_SEED = "EXTRA_IMAGE_SEED";

	public static final String EXTRA_LAST_ACCESS_DATE = "EXTRA_LAST_ACCESS_DATE";

	public static final String EXTRA_LAST_MESSAGE_DATE = "EXTRA_LAST_MESSAGE_DATE";

	public static final String EXTRA_UNREAD_MESSAGES = "EXTRA_UNREAD_MESSAGES";

	public static final String EXTRA_USER_BLOCKED = "EXTRA_USER_BLOCKED";

	private ChatType chatType;

	private TextView title;

	private TextView subtitle;

	private AnonymousImage imageAnonymous;

	private ImageView imageUser;

	public static Intent createIntent(@Nonnull Context context, @Nonnull ChatType chatType,
									  @Nonnull Long conversationId, Long phoneNumber,
									  String displayName, String roomName, String imageUri,
									  Long imageSeed, Long lastAccess, Long lastMessage,
									  Integer unreadMessages, Boolean userBlocked) {
		Intent intent = null;
		switch (chatType) {
			case SECRET:
			case PRIVATE:
			case PRIVATE_ANONYMOUS:
				intent = new Intent(context, ChatPrivateActivity.class);
				break;
			case FORTHRIGHT:
			case FLATTER:
				intent = new Intent(context, ChatPublicActivity.class);
				break;
		}
		fillIntent(intent, chatType, conversationId, phoneNumber, displayName, roomName, imageUri,
				   imageSeed, lastAccess, lastMessage, unreadMessages, userBlocked);
		return intent;
	}

	protected static void startIntent(@Nonnull Intent intent, @Nonnull Activity activity,
									  @Nonnull ChatType chatType, Long conversationId,
									  Long phoneNumber, String displayName, String roomName,
									  String imageUri, Long imageSeed, Long lastAccess,
									  Long lastMessage, Integer unreadMessages,
									  Boolean userBlocked) {
		if (conversationId == null && phoneNumber == null) {
			throw new RuntimeException("Not enough data to start the conversation");
		}
		fillIntent(intent, chatType, conversationId, phoneNumber, displayName, roomName, imageUri,
				   imageSeed, lastAccess, lastMessage, unreadMessages, userBlocked);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity)
														  .toBundle());
		} else {
			activity.startActivity(intent);
		}
	}

	private static void fillIntent(@Nonnull Intent intent, @Nonnull ChatType chatType,
								   Long conversationId, Long phoneNumber, String displayName,
								   String roomName, String imageUri, Long imageSeed,
								   Long lastAccess, Long lastMessage, Integer unreadMessages,
								   Boolean userBlocked) {
		if (conversationId != null) {
			intent.putExtra(EXTRA_CONVERSATION_ID, conversationId);
		}
		intent.putExtra(EXTRA_CHAT_TYPE, chatType);
		if (displayName != null) {
			intent.putExtra(EXTRA_DISPLAY_NAME, displayName);
		}
		if (roomName != null) {
			intent.putExtra(EXTRA_ROOM_NAME, roomName);
		}
		if (phoneNumber != null) {
			intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
		}
		if (imageUri != null) {
			intent.putExtra(EXTRA_IMAGE_URI, imageUri);
		}
		if (imageSeed != null) {
			intent.putExtra(EXTRA_IMAGE_SEED, imageSeed);
		}
		if (lastAccess != null) {
			intent.putExtra(EXTRA_LAST_ACCESS_DATE, lastAccess);
		}
		if (lastMessage != null) {
			intent.putExtra(EXTRA_LAST_MESSAGE_DATE, lastMessage);
		}
		if (unreadMessages != null) {
			intent.putExtra(EXTRA_UNREAD_MESSAGES, unreadMessages);
		}
		if (userBlocked != null) {
			intent.putExtra(EXTRA_USER_BLOCKED, userBlocked);
		}
	}

	protected void onCreated() {
		// Set up the action bar.
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		assert actionBar != null;
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setTitle(null);

		title = (TextView) findViewById(R.id.toolbar_title);
		subtitle = (TextView) findViewById(R.id.toolbar_subtitle);
		imageAnonymous = (AnonymousImage) findViewById(R.id.image_anonymous);
		imageUser = (ImageView) findViewById(R.id.image_user);

		Bundle extras = getIntent().getExtras();
		chatType = (ChatType) extras.getSerializable(EXTRA_CHAT_TYPE);
		setChatType(chatType);

		String roomName = extras.getString(EXTRA_ROOM_NAME);
		String displayName = extras.getString(EXTRA_DISPLAY_NAME);
		String imageUri = extras.getString(EXTRA_IMAGE_URI);
		Long imageSeed = extras.getLong(EXTRA_IMAGE_SEED, 0);
		if (imageSeed == 0) {
			imageSeed = null;
		}
		Long phoneNumber = extras.getLong(EXTRA_PHONE_NUMBER, 0);
		if (phoneNumber == 0) {
			phoneNumber = null;
		}
		bindToolbar(roomName, displayName, imageUri, imageSeed, phoneNumber);
	}

	public void setTitle(CharSequence title) {
		this.title.setText(title);
	}

	public void bindToolbar(String roomName, String displayName, String imageUri, Long imageSeed,
							Long phoneNumber) {
		final Resources res = getResources();
		switch (chatType) {
			case FORTHRIGHT:
				if (displayName != null) {
					setTitle(displayName);
				}
				if (imageUri != null) {
					setImageUri(imageUri);
				}
				if (roomName != null) {
					setSubtitle(res.getString(R.string.chat_forthright_subtitle, roomName));
				}
				break;
			case FLATTER:
				if (displayName != null) {
					setTitle(displayName);
				}
				if (imageUri != null) {
					setImageUri(imageUri);
				}
				if (roomName != null) {
					setSubtitle(res.getString(R.string.chat_flatter_subtitle, roomName));
				}
				break;
			case PRIVATE:
				if (displayName != null) {
					setTitle(displayName);
				}
				if (imageUri != null) {
					setImageUri(imageUri);
				}
				if (phoneNumber != null) {
					setSubtitle(PhoneUtils.getPrettyPrint(phoneNumber,
														  PhoneUtils.getCountryISO(this)));
				}
				break;
			case SECRET:
				if (displayName != null) {
					setTitle(displayName);
				}
				if (imageUri != null) {
					setImageUri(imageUri);
				}
				if (phoneNumber != null) {
					setSubtitle(PhoneUtils.getPrettyPrint(phoneNumber,
														  PhoneUtils.getCountryISO(this)));
				}
				break;
			case PRIVATE_ANONYMOUS:
				if (roomName != null) {
					setTitle(roomName);
				}
				if (phoneNumber != null) {
					// I started the chat
					if (displayName != null) {
						setSubtitle(res.getString(R.string.chat_anonymous_me_subtitle,
												  displayName));
					}
					if (roomName != null) {
						setRandomImageSeed(roomName);
					}
				} else {
					// someone started the chat
					if (displayName == null) {
						setSubtitle(R.string.chat_anonymous_other_subtitle);
					}
					if (roomName != null) {
						setRandomImageSeed(roomName);
					}
				}
				break;

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		switch (chatType) {
			case PRIVATE:
				Analytics.screen(Analytics.Screen.CONVERSATION_PRIVATE);
				break;
			case PRIVATE_ANONYMOUS:
				Analytics.screen(Analytics.Screen.CONVERSATION_ANONYMOUS);
				break;
			case SECRET:
				Analytics.screen(Analytics.Screen.CONVERSATION_SECRET);
				break;
			case FLATTER:
				Analytics.screen(Analytics.Screen.CONVERSATION_FLATTER);
				break;
			case FORTHRIGHT:
				Analytics.screen(Analytics.Screen.CONVERSATION_FORTHRIGHT);
				break;
		}
	}

	private void setChatType(ChatType chatType) {
		Resources res = getResources();
		AppBarLayout barLayout = (AppBarLayout) findViewById(R.id.bar_layout);
		switch (chatType) {
			case FORTHRIGHT:
				barLayout.setBackgroundColor(res.getColor(R.color.deep_purple));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(res.getColor(R.color.deep_purple_dark));
				}
				break;
			case FLATTER:
				barLayout.setBackgroundColor(res.getColor(R.color.pink));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(res.getColor(R.color.pink_dark));
				}
				break;
			case PRIVATE_ANONYMOUS:
				barLayout.setBackgroundColor(res.getColor(R.color.black_toolbar));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(res.getColor(R.color.black));
				}
				setSubtitleImage(R.drawable.ic_question_mark_white_14dp);
				break;
			case SECRET:
				barLayout.setBackgroundColor(res.getColor(R.color.purple));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(res.getColor(R.color.purple_dark));
				}
				setSubtitleImage(R.drawable.ic_timer_white_14dp);
				break;
			case PRIVATE:
				barLayout.setBackgroundColor(res.getColor(R.color.purple));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					getWindow().setStatusBarColor(res.getColor(R.color.purple_dark));
				}
				setSubtitleImage(R.drawable.ic_chat_bubble_outline_white_14dp);
				break;
		}
	}

	public void setSubtitle(CharSequence subtitle) {
		if (subtitle != null) {
			this.subtitle.setText(subtitle);
			this.subtitle.setVisibility(View.VISIBLE);
		} else {
			this.subtitle.setVisibility(View.GONE);
		}
	}

	public void setSubtitle(@StringRes int subtitle) {
		if (subtitle != 0) {
			this.subtitle.setText(subtitle);
			this.subtitle.setVisibility(View.VISIBLE);
		} else {
			this.subtitle.setVisibility(View.GONE);
		}
	}

	public void setRandomImageSeed(long imageSeed) {
		this.imageAnonymous.setSeed(imageSeed);
		this.imageAnonymous.setVisibility(View.VISIBLE);
		this.imageUser.setVisibility(View.GONE);
	}

	public void setRandomImageSeed(String imageSeed) {
		this.imageAnonymous.setSeed(imageSeed);
		this.imageAnonymous.setVisibility(View.VISIBLE);
		this.imageUser.setVisibility(View.GONE);
	}

	public void setImageUri(String imageUri) {
		ImageUtils.loadUserImage(this.imageUser, imageUri);
		this.imageAnonymous.setVisibility(View.GONE);
		this.imageUser.setVisibility(View.VISIBLE);
	}

	public void setSubtitleImage(@DrawableRes int drawableRes) {
		if (drawableRes == 0) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
			} else {
				subtitle.setCompoundDrawables(null, null, null, null);
			}
		} else {
			Drawable drawable;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				drawable = getResources().getDrawable(drawableRes, getTheme());
			} else {
				drawable = getResources().getDrawable(drawableRes);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null,
																		 null);
			} else {
				subtitle.setCompoundDrawables(drawable, null, null, null);
			}
		}
	}

}
