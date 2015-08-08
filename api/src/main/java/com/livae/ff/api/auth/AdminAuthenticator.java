package com.livae.ff.api.auth;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.googlecode.objectify.NotFoundException;
import com.livae.ff.api.model.AdminUser;
import com.livae.ff.api.model.PhoneUser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import static com.livae.ff.api.OfyService.ofy;

public class AdminAuthenticator implements Authenticator {

	public static final Long[] VALID_PHONES = {34657139626L, 15555215554L};// mine and emulator

	public static final Set<Long> ALLOWED_PHONES = new HashSet<>(Arrays.asList(VALID_PHONES));

	public AdminAuthenticator() {
	}

	public static boolean isAdminUser(PhoneUser user) {
		Long phone = user.getPhone();
		return ALLOWED_PHONES.contains(phone) ||
			   ofy().load().type(AdminUser.class).id(phone).now() != null;
	}

	@Override
	public User authenticate(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		if (token != null) {
			try {
				PhoneUser phoneUser =
				  ofy().load().type(PhoneUser.class).filter("token", token).first().safe();
				if (isAdminUser(phoneUser)) {
					return new User(phoneUser.getPhone().toString());
//					return new User(phoneUser.getPhone().toString(),
//									phoneUser.getPhone() + "@pensamientos.livae.com");
				} else if ("127.0.0.1".equals(request.getRemoteHost())) {
					AdminUser admin = ofy().load().type(AdminUser.class).id(0).now();
					if (admin == null) {
						admin = new AdminUser(0L);
						ofy().save().entity(admin);
					}
					return new User(phoneUser.getPhone().toString());
//					return new User(phoneUser.getPhone().toString(),
//									phoneUser.getPhone() + "@pensamientos.livae.com");
				}
			} catch (NotFoundException e) {
				// return null
			}
		}
		return null;
	}
}
