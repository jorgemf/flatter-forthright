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
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

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
	  {"Noah Jones", "Linda Allen", "James Lee", "Olivia West", "Paul Smith", "Sophia Moore",
	   "William White", "Isabella  Rodriguez", "Liam Taylor", "Emily Miller"};

	private static final String[] textEnglish =
	  // private conversations initial
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
		// 6 guy 2
		"\uD83C\uDCCF\n\uD83D\uDC7E\n\uD83D\uDE02",
		// 7 me
		"How was your trip?\n\uD83D\uDE80\uD83D\uDE80",
		// 8 guy 2
		"\uD83D\uDE0E\nWe went to that place you told me, it was awesome",
		"I will show you some pics later", "\uD83D\uDE06",
		// 11 me
		"\uD83D\uDC4D",
		// male friend forthright
		// 12 guy 6
		"that tshirt...",
		// 13 me
		"dude, we love you as your friends, but your tshirt has to go",
		"it scares all the girls in the bar",
		// 15 guy 8
		"finally someone said what we all were thinking", "thanks bro",
		// 17 guy 2
		"\uD83D\uDE31",

		// male friend conversation
		// 15 guy 2
		"What are you doing tonight? I have to talk about going to the bar",
		// 16 me
		"I was thinking about her",
		// 17 guy 2
		"the same girl again?? \uD83D\uDE34",
		// 19 me
		"I have to say something to her",
		// 19 guy 2
		"same story again and again...", "DO IT!!", "NOW!!!",
		// 22 me
		"ok \uD83D\uDE13",
		// female friend flatterer
		// 23 girl 1
		"you are a great person, I loooveee you too much. BFF",
		"❤❤❤❤❤\uD83D\uDC9B\uD83D\uDC9B\uD83D\uDC9B\uD83D\uDC9B",
		// 25 guy 2
		"I am bit shy but I want you to know you are very pretty",
		"I would want to be your " + "friend", "\uD83D\uDE18",
		// 28 the girl
		"I don't know who you are but I have a BF",
		// 29 me
		"Well... I know you have a boyfriend but sometimes you just need to say it",
		"I think you are a great girl",
		// 31 the girl
		"thanks, you are kind",
		// female friend private anonymous
		"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
		"", "", "", "", "", "", "", "", "",};

	private static final String[] aliasEnglish =
	  {"Someone had to tell you", "Football star", "Terminator", "", "", "", "", "", "", ""};

	private static final String[] usersNamesSpanish =
	  {"Noah Jones", "Linda Allen", "Liam Taylor", "Olivia West", "Paul Smith", "Sophia Moore",
	   "William White", "Isabella  Rodriguez", "James Lee", "Emily Miller"};

	private static final String[] textSpanish =
	  {"Te veo luego", "Sabes donde está?", "Ta lueg :)", "jajaja", "pasalo bien!", "", "", "", "",
	   "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
	   "", "", "", "", "", "",};

	private static final String[] aliasSpanish =
	  {"Tengo algo que decirte", "Futbol superstar", "Terminator", "", "", "", "", "", "", ""};

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
		comments = new Comment[100];
		// private chats
		comments[0] = new Comment(6001L, Constants.ChatType.PRIVATE, users[9].phone, null, text[0],
								  TimeUnit.MINUTES.toSeconds(157), null);
		comments[1] = new Comment(6002L, Constants.ChatType.PRIVATE, users[8].phone, null, text[1],
								  TimeUnit.HOURS.toSeconds(3), null);
		comments[2] = new Comment(6003L, Constants.ChatType.PRIVATE, users[7].phone, null, text[2],
								  TimeUnit.DAYS.toSeconds(1), null);
		comments[3] = new Comment(6004L, Constants.ChatType.PRIVATE, users[6].phone, null, text[3],
								  TimeUnit.HOURS.toSeconds(33), null);
		comments[4] = new Comment(6005L, Constants.ChatType.PRIVATE, users[5].phone, null, text[4],
								  TimeUnit.DAYS.toSeconds(2), null);
		comments[5] = new Comment(6006L, Constants.ChatType.PRIVATE, users[4].phone, null, text[5],
								  TimeUnit.DAYS.toSeconds(2), null);
		// friend chat
		comments[6] = new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[6],
								  TimeUnit.HOURS.toSeconds(30), null);
		comments[7] = new Comment(1002L, Constants.ChatType.PRIVATE, users[0].phone, null, text[7],
								  TimeUnit.HOURS.toSeconds(30), null);
		comments[8] = new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[8],
								  TimeUnit.HOURS.toSeconds(30), null);
		comments[9] = new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[9],
								  TimeUnit.HOURS.toSeconds(30), null);
		comments[10] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[10],
					  TimeUnit.HOURS.toSeconds(30), null);
		comments[11] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[0].phone, null, text[11],
					  TimeUnit.HOURS.toSeconds(30), null);
		// write in forthright
		comments[12] =
		  new Comment(2002L, Constants.ChatType.FORTHRIGHT, users[6].phone, alias[1], text[12],
					  TimeUnit.MINUTES.toSeconds(97), users[2].phone);
		comments[13] =
		  new Comment(2002L, Constants.ChatType.FORTHRIGHT, users[0].phone, alias[0], text[13], 0,
					  users[2].phone);
		comments[14] =
		  new Comment(2002L, Constants.ChatType.FORTHRIGHT, users[0].phone, alias[0], text[14], 0,
					  users[2].phone);
		comments[15] =
		  new Comment(2002L, Constants.ChatType.FORTHRIGHT, users[8].phone, alias[2], text[15], 0,
					  users[2].phone);
		comments[16] =
		  new Comment(2002L, Constants.ChatType.FORTHRIGHT, users[8].phone, alias[2], text[16], 0,
					  users[2].phone);
		comments[17] =
		  new Comment(2002L, Constants.ChatType.FORTHRIGHT, users[2].phone, null, text[16], 0,
					  users[2].phone);
		// real time chat with friend
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
		// private chats
		addComment(context, comments[0]);
		addComment(context, comments[1]);
		addComment(context, comments[2]);
		addComment(context, comments[3]);
		addComment(context, comments[4]);
		addComment(context, comments[5]);
		// friend chat
		addComment(context, comments[6]);
		addComment(context, comments[7]);
		addComment(context, comments[8]);
		addComment(context, comments[9]);
		addComment(context, comments[10]);
		addComment(context, comments[11]);
		// friend forthright initial comments
		addComment(context, comments[12]);
		// girl flatterer initial comments
		// TODO

		// Launch the app
		Intent intent;
		intent = context.getPackageManager().getLaunchIntentForPackage(PENSAMIENTOS_PACKAGE);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		device.wait(Until.hasObject(By.pkg(PENSAMIENTOS_PACKAGE).depth(0)), TIMEOUT);

		sleep(1000);
		pressText(FORTHRIGHT);
		sleep(600);
		pressText(users[2].name);
		typeText("dialog_edit_text", comments[13].alias);
		pressText("OK");
		sleep(1000);
		pressId("comment_text");
		typeText("comment_text",comments[13].comment);
		pressId("button_post_comment");
		addComment(context, comments[13]);
		pressId("comment_text");
		typeText("comment_text",comments[14].comment);
		pressId("button_post_comment");
		addComment(context, comments[14]);
		sleep(2500);
		addComment(context, comments[15]);
		sleep(1500);
		addComment(context, comments[16]);
		sleep(1000);
		addComment(context, comments[17]);
		sleep(1000);
		device.pressBack();

		// private friend conversation
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
			  .swipeUp(10);
	}

	private void typeText(String resId, String text)
	  throws UiObjectNotFoundException {
		UiSelector selector = new UiSelector().resourceId(PENSAMIENTOS_PACKAGE + ":id/" + resId);
		UiObject uiObject = device.findObject(selector);
		for (int i = 1; i < text.length(); i++) {
			uiObject.setText(text.substring(0, i));
			sleep(40 + (int) (Math.random() * 15));
		}
		uiObject.setText(text);
		sleep(40 + (int) (Math.random() * 15));
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
