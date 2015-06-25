package com.livae.ff.api.auth;

import com.livae.ff.api.model.PhoneUser;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.livae.ff.api.OfyService.ofy;

public class AuthUtil {

	private static final byte[] KEY = {61 - 128, 163 - 128, 4 - 128, 48 - 128, 244 - 128, 148 - 128,
									   69 - 128, 133 - 128, 16 - 128, 111 - 128, 110 - 128,
									   237 - 128, 231 - 128, 70 - 128, 100 - 128, 178 - 128,
									   210 - 128, 144 - 128, 148 - 128, 221 - 128, 192 - 128,
									   254 - 128, 1 - 128, 242 - 128, 214 - 128, 210 - 128,
									   183 - 128, 229 - 128, 23 - 128, 130 - 128, 163 - 128,
									   102 - 128, 203 - 128};

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	public static String createAuthToken(Long someId) {
		byte[] bytesLong = ByteBuffer.allocate(8).putLong(someId).array();
		byte[] hash = hash(bytesLong);
		byte[] bytes = ByteBuffer.allocate(32).putLong(SECURE_RANDOM.nextLong()).putLong(System
																						   .currentTimeMillis())
								 .put(hash, 0, 16).array();
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (bytes[i] ^ KEY[i]);
		}
		return convertToHex(bytes);
	}

	private static byte[] hash(byte[] byteString) {
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
			return byteString;
		} else {
			md.update(byteString, 0, byteString.length);
			return md.digest();
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

	public static PhoneUser getPhoneUser(com.google.appengine.api.users.User gUser) {
//		return PhoneUser.get(Long.parseLong(gUser.getUserId()));
		return PhoneUser.get(Long.parseLong(gUser.getEmail()));
	}

	public static PhoneUser getPhoneUser(String token) {
		return ofy().load().type(PhoneUser.class).filter("authToken", token).first().now();
	}
}
