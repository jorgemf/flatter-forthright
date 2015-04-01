package com.livae.ff.api.auth;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.livae.ff.api.model.PhoneUser;

import javax.servlet.http.HttpServletRequest;

import static com.livae.ff.api.OfyService.ofy;

public class AppAuthenticator implements Authenticator {

	public AppAuthenticator() {
	}

	@Override
	public User authenticate(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null) {
			PhoneUser phoneUser = AuthUtil.getPhoneUser(token);
			if (phoneUser != null) {
				return new User(token);
			}
		}
		return null;
	}
}
