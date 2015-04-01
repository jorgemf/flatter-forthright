package com.livae.ff.app.api;

import android.content.Context;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.okhttp.OkHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.livae.ff.api.ff.Apphunt;
import com.livae.ff.api.ff.ApphuntRequest;
import com.livae.ff.api.ff.ApphuntRequestInitializer;
import com.livae.ff.api.ff.model.Version;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.Settings;

import java.io.IOException;

public class API {

	private static final String LOG_TAG = "API";

	private static API instance = new API(Settings.API_URL);

	private Apphunt endpoint;

	private Admin adminEndpoint;

	private API(String url) {
		GoogleClientRequestInitializer initializer = new GoogleClientRequestInitializer() {
			@Override
			public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
			  throws IOException {
				if (BuildConfig.DEBUG) {
					abstractGoogleClientRequest.setDisableGZipContent(true);
				}
			}
		};
		ApphuntRequestInitializer apphuntRequestInitializer = (new ApphuntRequestInitializer() {
			@Override
			protected void initializeApphuntRequest(ApphuntRequest<?> request) throws IOException {
				super.initializeApphuntRequest(request);
				AppUser appUser = Application.appUser();
				if (appUser.isDeviceConnected()) {
					HttpHeaders headers = request.getRequestHeaders();
					headers.setAuthorization(appUser.getAccessToken());
				}
			}

		});
		OkHttpTransport okHttpTransport = new OkHttpTransport();
		JacksonFactory jacksonFactory = new JacksonFactory();
		endpoint = new Apphunt.Builder(okHttpTransport, jacksonFactory, null).setRootUrl(url)
																			 .setGoogleClientRequestInitializer(initializer)
																			 .setApphuntRequestInitializer(apphuntRequestInitializer)
																			 .build();
//		if (Application.isAdmin()) {
//			AdminRequestInitializer adminRequestInitializer = (new AdminRequestInitializer() {
//				@Override
//				protected void initializeAdminRequest(AdminRequest<?> request) throws IOException {
//					super.initializeAdminRequest(request);
//					AppUser appUser = Application.appUser();
//					if (appUser.isDeviceConnected()) {
//						HttpHeaders headers = request.getRequestHeaders();
//						headers.setAuthorization(appUser.getAccessToken());
//					}
//				}
//
//			});
//			adminEndpoint = new Admin.Builder(okHttpTransport, jacksonFactory, null).setRootUrl(url)
//																					.setGoogleClientRequestInitializer(initializer)
//																					.setAdminRequestInitializer(adminRequestInitializer)
//																					.build();
//		}
	}

	public static API instance(Context context) {
		return instance;
	}

	public static void changeAPIUrl(String url) {
		instance = new API(url);
	}

	public static Version version() throws IOException {
		return instance.endpoint.apiEndpoint().version(Settings.PLATFORM.name()).execute();
	}

	public static void wakeup() throws IOException {
		instance.endpoint.apiEndpoint().wakeup().execute();
	}

	public static Apphunt.ApplicationEndpoint app() {
		return instance.endpoint.applicationEndpoint();
	}

	public static Apphunt.UserEndpoint user() {
		return instance.endpoint.userEndpoint();
	}

	public static Apphunt.UserDeviceEndpoint device() {
		return instance.endpoint.userDeviceEndpoint();
	}

	public static Apphunt.CommentEndpoint comment() {
		return instance.endpoint.commentEndpoint();
	}

	public static Admin.AdminEndpoint admin() {
		return instance.adminEndpoint.adminEndpoint();
	}

}
