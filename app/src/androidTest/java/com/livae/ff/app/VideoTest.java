package com.livae.ff.app;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class VideoTest {

	private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(10);

	private static final String PENSAMIENTOS_PACKAGE = "com.livae.ff.app";

	private static final User[] users = {new User(1L, "User A", 600000111L, null), new User(2L,
																							"User B",
																							600000222L,
																							null),
										 new User(3L, "User C", 600000333L, null), new User(4L,
																							"User D",
																							600000444L,
																							null),
										 new User(5L, "User E", 600000555L, null), new User(6L,
																							"User F",
																							600000666L,
																							null),
										 new User(7L, "User G", 600000777L, null), new User(8L,
																							"User H",
																							600000888L,
																							null),
										 new User(9L, "User I", 600000999L, null),};

	private static final Comment[] comments = {new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""), new Comment(1L, 1L, ""),
											   new Comment(1L, 1L, ""),};

	private UiDevice device;

	@Before
	public void setUp() {
		// Initialize UiDevice instance
		device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
		// Start from the home screen
		device.pressHome();
		device.waitForIdle();
//		device.wait(Until.hasObject(By.pkg(getHomeScreenPackage()).depth(0));
		// create the user
		Application.appUser().setUserPhone(users[2].phone);

	}

	@Test
	public void test() {
		// Launch the app
		Context context = InstrumentationRegistry.getInstrumentation().getContext();
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(PENSAMIENTOS_PACKAGE);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		device.wait(Until.hasObject(By.pkg(PENSAMIENTOS_PACKAGE).depth(0)), TIMEOUT);
		sleep(1000);
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	static class User {

		long serverId;

		String name;

		Long phone;

		String imageUrl;

		User(long serverId, String name, Long phone, String imageUrl) {
			this.serverId = serverId;
			this.name = name;
			this.phone = phone;
			this.imageUrl = imageUrl;
		}
	}

	static class Comment {

		long serverId;

		long userId;

		String comment;

		Comment(long serverId, long userId, String comment) {
			this.serverId = serverId;
			this.userId = userId;
			this.comment = comment;
		}
	}
}
