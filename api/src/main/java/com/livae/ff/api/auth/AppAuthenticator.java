package com.livae.ff.api.auth;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.livae.ff.api.model.PhoneUser;

import javax.servlet.http.HttpServletRequest;

public class AppAuthenticator implements Authenticator {

	public AppAuthenticator() {
	}

	@Override
	public User authenticate(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null) {
			PhoneUser phoneUser = AuthUtil.getPhoneUser(token);
			if (phoneUser != null) {
				return new User(phoneUser.getPhone().toString());
//				return new User(phoneUser.getPhone().toString(),
//								phoneUser.getPhone() + "@pensamientos.livae.com");
			}
		}
		return null;
	}
}
