package com.livae.ff.app;

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
import android.widget.Button;

import com.livae.ff.common.Constants;
import com.livaee.ff.app.service.TestService;
import com.livaee.ff.app.service.TestService.Comment;
import com.livaee.ff.app.service.TestService.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class VideoTest {

	private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

	private static final String PENSAMIENTOS_PACKAGE = "com.livae.ff.app.dev";

	private static final String TEST_SERVICE = TestService.class.getCanonicalName();

	private static final User[] users =
	  {new User(7000000L, "Noah Jones", null), new User(7000111L, "Linda Allen", null),
	   new User(7000222L, "Liam Taylor", null), new User(7000333L, "Olivia West", null),
	   new User(7000444L, "Paul Smith", null), new User(7000555L, "Sophia Moore", null),
	   new User(7000666L, "William White", null), new User(7000777L, "Isabella  Rodriguez", null),
	   new User(7000888L, "James Lee", null), new User(7000999L, "Emily Miller", null),};

	private static final Comment[] comments =
	  {new Comment(1L, 1L, Constants.ChatType.PRIVATE, 7000111L, "alias", "Hi cute, how are you?",
				   TimeUnit.HOURS.toSeconds(1), true),
	   new Comment(2L, 2L, Constants.ChatType.PRIVATE, 7000333L, "alias",
				   "Have you seen my brother Wally?", TimeUnit.MINUTES.toSeconds(5), false),
	   new Comment(3L, 3L, Constants.ChatType.PRIVATE, 7000222L, "alias",
				   "Dude, come to the party now!", TimeUnit.MINUTES.toSeconds(10), false),
	   new Comment(4L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, true),
	   new Comment(5L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, true),
	   new Comment(6L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, true),
	   new Comment(7L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, true),
	   new Comment(8L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, true),
	   new Comment(9L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, true),
	   new Comment(10L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(11L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(12L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(13L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(14L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(15L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(16L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(17L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(18L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(19L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),
	   new Comment(20L, 1L, Constants.ChatType.PRIVATE, 7000000L, "alias", "Comment", 0, false),};

	private UiDevice device;

	@Before
	public void setUp() {
		// Initialize UiDevice instance
		device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
		// Start from the home screen
		device.pressHome();
		device.waitForIdle();
	}

	@Test
	public void test() {
		Context context = InstrumentationRegistry.getInstrumentation().getContext();
		// configure the users
		setMyNumber(context, users[0].phone);
		for (User user : users) {
			addUser(context, user);
		}
		// add initial comments
		addComment(context, comments[0]);
		addComment(context, comments[1]);
		addComment(context, comments[2]);
		// Launch the app
		Intent intent;
		intent = context.getPackageManager().getLaunchIntentForPackage(PENSAMIENTOS_PACKAGE);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		device.wait(Until.hasObject(By.pkg(PENSAMIENTOS_PACKAGE).depth(0)), TIMEOUT);
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

	private void setMyNumber(Context context, Long number) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(PENSAMIENTOS_PACKAGE, TEST_SERVICE));
		intent.putExtra(TestService.EXTRA_ACTION, TestService.ACTION.REGISTER_MY_PHONE);
		intent.putExtra(TestService.EXTRA_DATA, number);
		context.startService(intent);
	}

	private void pressKey(char key) {
		UiObject keyObject = device.findObject(new UiSelector().text(String.valueOf(key))
															   .className(Button.class.getName()));
		try {
			keyObject.click();
		} catch (UiObjectNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
