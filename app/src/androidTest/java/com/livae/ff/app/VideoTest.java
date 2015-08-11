package com.livae.ff.app;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.view.KeyEvent;
import android.widget.Button;

import com.livae.ff.common.Constants;
import com.livaee.ff.app.service.TestService;
import com.livaee.ff.app.service.TestService.Comment;
import com.livaee.ff.app.service.TestService.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class VideoTest {

	private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

	private static final String PENSAMIENTOS_PACKAGE = "com.livae.ff.app.dev";

	private static final String TEST_SERVICE = TestService.class.getCanonicalName();

	private static final String[] usersNamesEnglish =
	  {"Noah Jones", "Linda Allen", "Liam Taylor", "Olivia West", "Paul Smith", "Sophia Moore",
	   "William White", "Isabella  Rodriguez", "James Lee", "Emily Miller"};

	private static final String[] textEnglish =
	  // private conversations
	  {
		// 0 girl 9
		"Hahaha",
		// 1 guy 8
		"Yeah, I think I know what you mean",
		// 2 girl 7
		"Bye \uD83D\uDE0A",
		// 3 guy 6
		"Have fun!",
		// 4 anonymous girl 5
		"Sounds good to me",
		// 5 guy 4
		"This is app is amazing, I was talking with this girl and...",
		// male friend conversation
		// 6 me
		"How was your trip?",
		// 7 guy 2
		"We went to that place you told me, it was awesome", "I will show you some pics later",
		"\uD83D\uDE06",
		// 10 me
		"\uD83D\uDC4D",
		// 11 guy 2
		"What are you doing tonight? I have to talk about going to the bar",
		// male friend forthright
		// me
		"dude, we love you as your friends, but your tshirt has to go",
		"it scares all the girls in the bar",
		// guy 8
		"finally someone said what we all were thinking", "thanks bro",

		// male friend conversation
		// 12 me
		"I was thinking about her",
		// 13 guy 2
		"the same girl again?? \uD83D\uDE34",
		// 14 me
		"I have to say something to her",
		// 15 guy 2
		"same story again and again...", "DO IT!!", "NOW!!!",
		// 16 me
		"ok \uD83D\uDE13",
		// female friend flatterer
		// girl 1
		"you are a great person, I loooveee you too much. BFF",
		"❤❤❤❤❤\uD83D\uDC9B\uD83D\uDC9B\uD83D\uDC9B\uD83D\uDC9B",
		// guy 2
		"I am bit shy but I want you to know you are very pretty",
		"I would want to be your " + "friend", "\uD83D\uDE18",
		// the girl
		"I don't know who you are but I have a BF",
		// me
		"Well... I know you have a boyfriend but sometimes you just need to say it",
		"I think you are a great girl",
		// the girl
		"thanks, you are kind",
		// female friend private anonymous
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "",};

	private static final String[] aliasEnglish =
	  {"Someone had to tell you", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
	   "", "", "", "", "", "", "", "",};

	private static final String[] usersNamesSpanish =
	  {"Noah Jones", "Linda Allen", "Liam Taylor", "Olivia West", "Paul Smith", "Sophia Moore",
	   "William White", "Isabella  Rodriguez", "James Lee", "Emily Miller"};

	private static final String[] textSpanish =
	  {"Te veo luego", "Sabes donde está?", "Ta lueg :)", "jajaja", "pasalo bien!", "", "", "", "",
	   "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
	   "", "", "", "", "", "",};

	private static final String[] aliasSpanish =
	  {"Tengo algo que decirte", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
	   "",
	   "", "", "", "", "", "", "",};

	private static final long[] PHONES =
	  {785639L, 757392L, 753286L, 799438L, 711204L, 784585L, 765890L, 712943L, 749598L, 712534L};

	private String FLATTERER;

	private String FORTHRIGHT;

	private String PRIVATE;

	private String[] userNames;

	private String[] text;

	private String[] alias;

	private Comment[] comments;

	private User[] users;

	private UiDevice device;

	@Before
	public void setUp() {
		// common test fields
		final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
		Context context = instrumentation.getContext();

		Locale current = context.getResources().getConfiguration().locale;
		if (current.getCountry().equalsIgnoreCase("es")) {
			userNames = usersNamesSpanish;
			text = textSpanish;
			alias = aliasSpanish;
			FLATTERER = "Adulador";
			FORTHRIGHT = "Honesto";
			PRIVATE = "Privado";
		} else {
			userNames = usersNamesEnglish;
			text = textEnglish;
			alias = aliasEnglish;
			FLATTERER = "Flatterer";
			FORTHRIGHT = "Forthright";
			PRIVATE = "Private";
		}

		initUsers();
		initComments();

		// Initialize UiDevice instance
		device = UiDevice.getInstance(instrumentation);
		// Start from the home screen
		device.pressHome();
		device.waitForIdle();
	}

	private void initUsers() {
		final String imageResDir = "android.resource://" + PENSAMIENTOS_PACKAGE + "/";
		users = new User[]{new User(PHONES[0], userNames[0], imageResDir + R.raw.user0),
						   new User(PHONES[1], userNames[1], imageResDir + R.raw.user1),
						   new User(PHONES[2], userNames[2], imageResDir + R.raw.user2),
						   new User(PHONES[3], userNames[3], imageResDir + R.raw.user3),
						   new User(PHONES[4], userNames[4], imageResDir + R.raw.user4),
						   new User(PHONES[5], userNames[5], imageResDir + R.raw.user5),
						   new User(PHONES[6], userNames[6], imageResDir + R.raw.user6),
						   new User(PHONES[7], userNames[7], imageResDir + R.raw.user7),
						   new User(PHONES[8], userNames[8], imageResDir + R.raw.user8),
						   new User(PHONES[9], userNames[9], imageResDir + R.raw.user9)};
	}

	private void initComments() {
		comments =
		  new Comment[]{new Comment(1L, 1L, Constants.ChatType.PRIVATE, users[6].phone, alias[0],
									text[3], TimeUnit.DAYS.toSeconds(2), true),
						new Comment(2L, 2L, Constants.ChatType.PRIVATE, users[9].phone, alias[0],
									text[2], TimeUnit.DAYS.toSeconds(1), true),
						new Comment(3L, 3L, Constants.ChatType.PRIVATE, users[8].phone, alias[0],
									text[1], TimeUnit.HOURS.toSeconds(2), true),
						new Comment(4L, 1L, Constants.ChatType.PRIVATE, users[7].phone, alias[0],
									text[0], TimeUnit.HOURS.toSeconds(1), true),
						new Comment(5L, 1L, Constants.ChatType.PRIVATE, users[5].phone, alias[0],
									text[4], TimeUnit.MINUTES.toSeconds(90), true),
						new Comment(6L, 1L, Constants.ChatType.FORTHRIGHT, users[4].phone,
									alias[0],
									text[5], 0, true),
						new Comment(7L, 1L, Constants.ChatType.FORTHRIGHT, users[4].phone,
									alias[0],
									text[6], 0, true),
						new Comment(8L, 1L, Constants.ChatType.FORTHRIGHT, users[4].phone,
									alias[0],
									text[7], 0, true),
						new Comment(9L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[8], 0, true),
						new Comment(10L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[9], 0, false),
						new Comment(11L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[10], 0, false),
						new Comment(12L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[11], 0, false),
						new Comment(13L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[12], 0, false),
						new Comment(14L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[13], 0, false),
						new Comment(15L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[14], 0, false),
						new Comment(16L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[15], 0, false),
						new Comment(17L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[16], 0, false),
						new Comment(18L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[17], 0, false),
						new Comment(19L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[18], 0, false),
						new Comment(20L, 1L, Constants.ChatType.PRIVATE, users[0].phone, alias[0],
									text[19], 0, false)};
	}

	@Test
	public void test()
	  throws UiObjectNotFoundException {
		Context context = InstrumentationRegistry.getInstrumentation().getContext();
		// configure the users
		setMyUser(context, users[0]);
		for (User user : users) {
			addUser(context, user);
		}
		// add initial comments
		addComment(context, comments[0]);
		addComment(context, comments[1]);
		addComment(context, comments[2]);
		addComment(context, comments[3]);
		addComment(context, comments[4]);
//		addComment(context, comments[5]);
//		addComment(context, comments[6]);
//		addComment(context, comments[7]);
//		addComment(context, comments[8]);
//		addComment(context, comments[9]);
//		addComment(context, comments[10]);
//		addComment(context, comments[11]);
//		addComment(context, comments[12]);
		// Launch the app
		Intent intent;
		intent = context.getPackageManager().getLaunchIntentForPackage(PENSAMIENTOS_PACKAGE);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		device.wait(Until.hasObject(By.pkg(PENSAMIENTOS_PACKAGE).depth(0)), TIMEOUT);
		sleep(1000);
		pressText(FLATTERER);
		sleep(600);
		swipeUp("recycler_view");
		sleep(500);
		pressText(users[1].name);
		pressId("comment_text");
		typeText(comments[5].comment);
		pressId("button_post_comment");
		addComment(context, comments[5]);
		sleep(2000);
		pressText(FORTHRIGHT);
		sleep(10000);
	}

	private void addUser(Context context, User user) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(PENSAMIENTOS_PACKAGE, TEST_SERVICE));
		intent.putExtra(TestService.EXTRA_ACTION, TestService.ACTION.ADD_USER);
		intent.putExtra(TestService.EXTRA_DATA, user);
		context.startService(intent);
	}

	private void addComment(Context context, Comment comment) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(PENSAMIENTOS_PACKAGE, TEST_SERVICE));
		intent.putExtra(TestService.EXTRA_ACTION, TestService.ACTION.POST_MESSAGE);
		intent.putExtra(TestService.EXTRA_DATA, comment);
		context.startService(intent);
	}

	private void setMyUser(Context context, User user) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(PENSAMIENTOS_PACKAGE, TEST_SERVICE));
		intent.putExtra(TestService.EXTRA_ACTION, TestService.ACTION.REGISTER_MY_PHONE);
		intent.putExtra(TestService.EXTRA_DATA, user);
		context.startService(intent);
	}

	private void pressKey(char key)
	  throws UiObjectNotFoundException {
		device.findObject(new UiSelector().text(String.valueOf(key))
										  .className(Button.class.getName())).click();
	}

	private void pressText(String text)
	  throws UiObjectNotFoundException {
		device.findObject(new UiSelector().text(text)).click();
	}

	private void pressId(String resId)
	  throws UiObjectNotFoundException {
		device.findObject(new UiSelector().resourceId(PENSAMIENTOS_PACKAGE + ":id/" + resId))
			  .click();
	}

	private void swipeUp(String resId)
	  throws UiObjectNotFoundException {
		device.findObject(new UiSelector().resourceId(PENSAMIENTOS_PACKAGE + ":id/" + resId))
			  .swipeUp(20);
	}

	private void typeText(String text)
	  throws UiObjectNotFoundException {
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == ' ') {
				device.pressKeyCode(KeyEvent.KEYCODE_SPACE);
			} else {
				pressKey(text.charAt(i));
			}
			sleep(100 + (int) (Math.random() * 20));
		}
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time / 10); // TODO remove the / 10
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
