package com.livae.ff.app.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.annotation.WorkerThread;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.settings.Settings;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DeviceUtils {

	private static Object platform;

	public static String getUniqueDeviceId(Context context) {
		String deviceId = Secure.getString(context.getContentResolver(),
										   Secure.ANDROID_ID); // 64-bit number (as a hex string)

		// wifiMac requires a permission it is not set right now
//		String wifiMac = null;
//		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); //
// XX:XX:XX:XX:XX:XX
//		if (wifiManager != null) {
//			WifiInfo wInfo = wifiManager.getConnectionInfo();
//			if (wInfo != null) {
//				wifiMac = wInfo.getMacAddress();
//			}
//		}
		String serialNumber = Build.SERIAL;

		String uniqueId = "";
		if (deviceId != null) {
			uniqueId += deviceId;
		}
//		if (wifiMac != null) {
//			uniqueId += wifiMac;
//		}
		if (serialNumber != null) {
			uniqueId += serialNumber;
		}
		return hash(uniqueId);
	}

	private static String hash(String text) {
		MessageDigest md = null;
//		if (md == null) {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException ignore) {
		}
//		}
		if (md == null) {
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException ignore) {
			}
		}
		if (md == null) {
			return text;
		} else {
			byte[] byteString = text.getBytes();
			md.update(byteString, 0, byteString.length);
			byte[] sha1hash = md.digest();
			return convertToHex(sha1hash);
		}
	}

	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfByte = (b >>> 4) & 0x0F;
			int twoHalfs = 0;
			do {
				buf.append((0 <= halfByte) && (halfByte <= 9) ? (char) ('0' + halfByte)
															  : (char) ('a' + (halfByte - 10)));
				halfByte = b & 0x0F;
			} while (twoHalfs++ < 1);
		}
		return buf.toString();
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity;
		connectivity = (ConnectivityManager) context.getSystemService(Context
																		.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivity.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static String getCountry(Context context) {
		return context.getResources().getConfiguration().locale.getCountry();
	}

	public static boolean checkCloudMessagesServices(Activity activity) {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
													  Settings.PLAY_SERVICES_RESOLUTION_REQUEST)
									  .show();
			}
			return false;
		}
		return true;
	}

	@WorkerThread
	public static String getCloudMessageId(Context context) {
		try {
			InstanceID instanceID = InstanceID.getInstance(context);
			return instanceID.getToken(Settings.Google.SENDER_ID,
									   GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getModel(Context context) {
		return Build.MODEL;
	}

	public static String getOsVersion(Context context) {
		return "Android " + Build.VERSION.SDK_INT + " " + Build.VERSION.CODENAME;
	}

	public static String getApplicationVersionString() {
		String version = BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_DATE;
		if (!BuildConfig.FLAVOR.equals("production")) {
			version += " " + BuildConfig.FLAVOR;
		}
		if (BuildConfig.DEBUG) {
			version += "-" + BuildConfig.BUILD_TYPE;
		}
		return version;
	}

//	TODO https://developer.android.com/preview/features/runtime-permissions.html
//
//	public static boolean requestPermission(Activity activity, String[] permissions) {
//
//		if (checkSelfPermission(context, Manifest.permission.READ_CONTACTS) !=
//			PackageManager.PERMISSION_GRANTED) {
//
//			// Should we show an explanation?
//			if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
//				// Explain to the user why we need to read the contacts
//			}
//
//			requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
//							   MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//
//			// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//			// app-defined int constant
//		}
//
//		return false;
//	}
//	@Override
//	public void onRequestPermissionsResult(int requestCode,
//										   String permissions[], int[] grantResults) {
//		switch (requestCode) {
//			case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
//				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//					// permission was granted, yay! do the
//					// calendar task you need to do.
//
//				} else {
//
//					// permission denied, boo! Disable the
//					// functionality that depends on this permission.
//				}
//				return;
//			}
//
//			// other 'switch' lines to check for other
//			// permissions this app might request
//		}
//	}
}
