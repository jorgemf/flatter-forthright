package com.livae.ff.app.api;

import android.content.Context;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.okhttp.OkHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.livae.ff.api.ff.Ff;
import com.livae.ff.api.ff.FfRequest;
import com.livae.ff.api.ff.FfRequestInitializer;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.Settings;

import java.io.IOException;

public class API {

	private static final String LOG_TAG = "API";

	private static API instance = new API(Settings.API_URL);

	private Ff endpoint;

//	private Admin adminEndpoint;

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
		FfRequestInitializer apphuntRequestInitializer = (new FfRequestInitializer() {
			@Override
			protected void initializeFfRequest(FfRequest<?> request) throws IOException {
				super.initializeFfRequest(request);
				AppUser appUser = Application.appUser();
				if (appUser.isDeviceConnected()) {
					HttpHeaders headers = request.getRequestHeaders();
					headers.setAuthorization(appUser.getAccessToken());
				}
			}

		});
		OkHttpTransport okHttpTransport = new OkHttpTransport();
		JacksonFactory jacksonFactory = new JacksonFactory();
		Ff.Builder builder = new Ff.Builder(okHttpTransport, jacksonFactory, null);
		endpoint = builder.setRootUrl(url).setGoogleClientRequestInitializer(initializer)
						  .setFfRequestInitializer(apphuntRequestInitializer).build();
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

//	public static Version version() throws IOException {
//		return instance.endpoint.apiEndpoint().version(Settings.PLATFORM.name()).execute();
//	}

	public static void wakeup() throws IOException {
		instance.endpoint.apiEndpoint().wakeup().execute();
	}

	public static Ff.ApiEndpoint endpoint() {
		return instance.endpoint.apiEndpoint();
	}

}
