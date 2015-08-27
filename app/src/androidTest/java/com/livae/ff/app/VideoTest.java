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
	  {"Noah Jones", "Isabella  Rodriguez", "James Lee", "Olivia West", "Paul Smith",
	   "Sophia Moore", "William White", "Linda Allen", "Liam Taylor", "Emily Miller"};

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
		"\uD83D\uDE0E\nWe went to that place you told me, it was " +
		"awesome\n\uD83C\uDF7B\uD83C\uDF78", "I will show you some pics later", "\uD83D\uDE06",
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
		// 18 guy 2
		"What are you doing? I was thinking about going to the bar tonight",
		// 19 me
		"I was thinking about her",
		// 20 guy 2
		"the same girl again?? \uD83D\uDE34",
		// 21 me
		"I have to say something to her",
		// 22 guy 2
		"same story again and again...", "DO IT!!", "NOW!!!",
		// 25 me
		"ok",
		// female friend flatterer
		// 26 girl 3
		"you are the best friend\nI loooveee you too much. BFF",
		"  ❤❤❤    ❤❤❤\n❤❤❤❤❤❤❤❤\n   ❤❤❤❤❤❤❤\n        ❤❤❤❤❤\n           ❤❤❤\n               ❤\n",
		// 28 girl
		"\uD83D\uDE0D\uD83D\uDE0D\uD83D\uDE0D\uD83D\uDE0D\uD83D\uDE0D", "\uD83D\uDC8B\uD83D\uDE18",
		// 30 me
		"I love you",
		// 31 girl
		"What?",
		// 32
		"I needed to say how I feel",
		// 33 the girl
		"Let's talk in private",
		// female friend private anonymous
		// 34 me
		"Here I am", "I love you since long time ago",
		// 36 girl
		"Sorry, but I got a boyfriend recently", "Don't tell anyone",
		// 38 me
		"I am late",
		// 39 girl
		"you are",
		// 40 me
		"ok", "well... at least I said it",
		// new private conversation
		// 42 anom girl
		"Hi",
		// 43 me
		"Hi", "Who are you?",
		// 45 anom girl
		"Your secret lover \uD83D\uDE09"};

	private static final String[] aliasEnglish =
	  {"Priest", "Football star", "Terminator", "BFF", "Half heart", "Sweet heart"};

	private static final String[] usersNamesSpanish =
	  {"Alejandro García", "Isabel Gonzalez", "Juan Lopez", "Olivia Fernandez", "Pedro Martínez",
	   "Sofia Torres", "Tomas Alonso", "Linda Suarez", "Lorenzo Vazquez", "Elena Sánchez"};

	private static final String[] textSpanish =
	  // private conversations initial
	  {
		// 0 girl 9
		"jajaja",
		// 1 guy 8
		"Si! Te entiendo",
		// 2 girl 7
		"Ciao \uD83D\uDE0A",
		// 3 guy 6
		"Diviertete!",
		// 4 anonymous girl 5
		"Me parece bien",
		// 5 guy 4
		"Esta app es alucinante, estaba hablando con una chica y...",
		// male friend conversation
		// 6 guy 2
		"\uD83C\uDCCF\n\uD83D\uDC7E\n\uD83D\uDE02",
		// 7 me
		"Cómo fue tu viaje?\n\uD83D\uDE80\uD83D\uDE80",
		// 8 guy 2
		"\uD83D\uDE0E\nFuimos al sitio que me dijistes y era increible " +
		"\n\uD83C\uDF7B\uD83C\uDF78", "Te mando luego unas fotos", "\uD83D\uDE06",
		// 11 me
		"\uD83D\uDC4D",
		// male friend forthright
		// 12 guy 6
		"esa camisa...",
		// 13 me
		"tio, somos tus amigos y te queremos pero esa camisa tiene que acabar en la basura",
		"asusta a todas las chicas del bar",
		// 15 guy 8
		"al final alguien dijo lo que todos pensabamos", "gracias en nombre de la humanidad",
		// 17 guy 2
		"\uD83D\uDE31",

		// male friend conversation
		// 18 guy 2
		"Qué estas haciendo? Estaba pensando en ir al bar esta noche",
		// 19 me
		"Estaba pensando en ella",
		// 20 guy 2
		"Otra vez?? \uD83D\uDE34",
		// 21 me
		"Debería decirla algo",
		// 22 guy 2
		"la misma historia de siempre...", "HAZLO!!", "AHORA!!!",
		// 25 me
		"ok",
		// female friend flatterer
		// 26 girl 3
		"Eres la mejor amiga\nTQM",
		"  ❤❤❤    ❤❤❤\n❤❤❤❤❤❤❤❤\n   ❤❤❤❤❤❤❤\n        ❤❤❤❤❤\n           ❤❤❤\n               ❤\n",
		// 28 girl
		"\uD83D\uDE0D\uD83D\uDE0D\uD83D\uDE0D\uD83D\uDE0D\uD83D\uDE0D", "\uD83D\uDC8B\uD83D\uDE18",
		// 30 me
		"Creo que estoy enamorado de ti",
		// 31 girl
		"Qué?!",
		// 32
		"Necesitaba decir como me sentia",
		// 33 the girl
		"Mejor hablame en privado",
		// female friend private anonymous
		// 34 me
		"Aqui estoy", "Tengo sentimientos hacia ti desde hace un tiempo",
		// 36 girl
		"Lo siento, pero tengo novio desde hace poco", "Por favor no lo vayas contando",
		// 38 me
		"Llego tarde",
		// 39 girl
		"Un poco",
		// 40 me
		"Bueno...", "Al menos ya lo solté",
		// new private conversation
		// 42 anom girl
		"Hola",
		// 43 me
		"Hola", "Quien eres?",
		// 45 anom girl
		"Tu amante secreta \uD83D\uDE09"};

	private static final String[] aliasSpanish =
	  {"Predicador", "Superestrella", "Terminator", "Best friend", "Corazón vacio", "Dulce"};

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
		  new Comment(2002L, Constants.ChatType.FORTHRIGHT, users[2].phone, null, text[17], 0,
					  users[2].phone);
		// real time chat with friend
		comments[18] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[18], 0, null);
		comments[19] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[0].phone, null, text[19], 0, null);
		comments[20] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[20], 0, null);
		comments[21] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[0].phone, null, text[21], 0, null);
		comments[22] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[22], 0, null);
		comments[23] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[23], 0, null);
		comments[24] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[2].phone, null, text[24], 0, null);
		comments[25] =
		  new Comment(1002L, Constants.ChatType.PRIVATE, users[0].phone, null, text[25], 0, null);
		// flatter the girl
		comments[26] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[3].phone, alias[3], text[26],
					  TimeUnit.DAYS.toSeconds(7), users[1].phone);
		comments[27] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[3].phone, alias[3], text[27],
					  TimeUnit.DAYS.toSeconds(7), users[1].phone);
		comments[28] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[1].phone, null, text[28],
					  TimeUnit.DAYS.toSeconds(7), users[1].phone);
		comments[29] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[1].phone, null, text[29],
					  TimeUnit.DAYS.toSeconds(7), users[1].phone);
		comments[30] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[0].phone, alias[4], text[30], 0,
					  users[1].phone);
		comments[31] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[1].phone, null, text[31], 0,
					  users[1].phone);
		comments[32] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[0].phone, alias[4], text[32], 0,
					  users[1].phone);
		comments[33] =
		  new Comment(2001L, Constants.ChatType.FLATTER, users[1].phone, null, text[33], 0,
					  users[1].phone);
		// private anonymous
		comments[34] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[0].phone, null, text[34],
					  0, users[1].phone);
		comments[35] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[0].phone, null, text[35],
					  0, users[1].phone);
		comments[36] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[1].phone, null, text[36],
					  0, users[1].phone);
		comments[37] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[1].phone, null, text[37],
					  0, users[1].phone);
		comments[38] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[0].phone, null, text[38],
					  0, users[1].phone);
		comments[39] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[1].phone, null, text[39],
					  0, users[1].phone);
		comments[40] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[0].phone, null, text[40],
					  0, users[1].phone);
		comments[41] =
		  new Comment(3001L, Constants.ChatType.PRIVATE_ANONYMOUS, users[0].phone, null, text[41],
					  0, users[1].phone);
		// other anonymous talk
		comments[42] =
		  new Comment(3007L, Constants.ChatType.PRIVATE_ANONYMOUS, users[7].phone, alias[5],
					  text[42], 0, users[0].phone);
		comments[43] =
		  new Comment(3007L, Constants.ChatType.PRIVATE_ANONYMOUS, users[0].phone, null, text[43],
					  0, users[0].phone);
		comments[44] =
		  new Comment(3007L, Constants.ChatType.PRIVATE_ANONYMOUS, users[0].phone, null, text[44],
					  0, users[0].phone);
		comments[45] =
		  new Comment(3007L, Constants.ChatType.PRIVATE_ANONYMOUS, users[7].phone, alias[5],
					  text[45], 0, users[0].phone);

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
		addComment(context, comments[26]);
		addComment(context, comments[27]);
		addComment(context, comments[28]);
		addComment(context, comments[29]);

		// Launch the app
		Intent intent;
		intent = context.getPackageManager().getLaunchIntentForPackage(PENSAMIENTOS_PACKAGE);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		device.wait(Until.hasObject(By.pkg(PENSAMIENTOS_PACKAGE).depth(0)), TIMEOUT);

		// forthright your friend
		sleep(900);
		pressText(FORTHRIGHT);
		sleep(600);
		pressText(users[2].name);
		sleep(700);
		typeText("dialog_edit_text", comments[13].alias);
		pressText("OK");
		sleep(600);
		writeComment(context, comments[13]);
		writeComment(context, comments[14]);
		writeComment(context, comments[15]);
		writeComment(context, comments[16]);
		writeComment(context, comments[17]);
		sleep(1000);
		device.pressBack();
		device.pressBack();

		// private friend conversation
		sleep(500);
		addComment(context, comments[18]);
		sleep(200);
		pressText(PRIVATE);
		pressText(users[2].name);
		sleep(1500);
		writeComment(context, comments[19]);
		writeComment(context, comments[20]);
		writeComment(context, comments[21]);
		writeComment(context, comments[22]);
		writeComment(context, comments[23]);
		writeComment(context, comments[24]);
		writeComment(context, comments[25]);
		sleep(1000);
		device.pressBack();
		device.pressBack();

		// flatterer the girl
		pressText(FLATTERER);
		pressText(users[1].name);
		sleep(700);
		typeText("dialog_edit_text", comments[30].alias);
		pressText("OK");
		sleep(1500);
		writeComment(context, comments[30]);
		writeComment(context, comments[31]);
		writeComment(context, comments[32]);
		writeComment(context, comments[33]);
		sleep(1600);
		device.pressBack();
		device.pressBack();
		pressText(PRIVATE);
		pressId("create_chat_button");
		sleep(1000);
		pressId("create_chat_anonymous_button");
		sleep(500);
		pressText(users[1].name);
		typeText("dialog_edit_text", comments[30].alias);
		sleep(1000);
		pressText("OK");
		writeComment(context, comments[34]);
		writeComment(context, comments[35]);
		writeComment(context, comments[36]);
		writeComment(context, comments[37]);
		writeComment(context, comments[38]);
		writeComment(context, comments[39]);
		writeComment(context, comments[40]);
		writeComment(context, comments[41]);
		sleep(1600);
		device.pressBack();
		device.pressBack();
		sleep(1500);

		// anonymous girl
		writeComment(context, comments[42]);
		pressText(comments[42].alias);
		writeComment(context, comments[43]);
		writeComment(context, comments[44]);
		writeComment(context, comments[45]);
		sleep(10000);
	}

	private void writeComment(Context context, Comment comment)
	  throws UiObjectNotFoundException {
		if (comment.userId == users[0].phone) {
			pressId("comment_text");
			typeText("comment_text", comment.comment);
			pressId("button_post_comment");
			addComment(context, comment);
			sleep(comment.comment.length() * 30 + 200);
		} else {
			sleep(comment.comment.length() * 30 + 300);
			addComment(context, comment);
			sleep(comment.comment.length() * 30 + 500);
		}
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
			sleep(50 + (int) (Math.random() * 20));
		}
		uiObject.setText(text);
		sleep(50 + (int) (Math.random() * 20));
	}

	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
