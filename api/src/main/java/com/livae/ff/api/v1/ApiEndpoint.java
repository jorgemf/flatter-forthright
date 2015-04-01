package com.livae.ff.api.v1;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.utils.SystemProperty;
import com.livae.ff.api.auth.AppAuthenticator;
import com.livae.ff.api.auth.AuthUtil;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.util.InputUtil;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static com.livae.ff.api.OfyService.ofy;

@Api(
	  name = "ff",
	  version = "v1",
	  backendRoot = "https://ff.livae.com/api",
	  namespace = @ApiNamespace(
								 ownerDomain = "ff.livae.com",
								 ownerName = "livae",
								 packagePath = "api"),
	  authenticators = {AppAuthenticator.class})
public class ApiEndpoint {

	protected static final Logger logger = Logger.getLogger(ApiEndpoint.class.getName());

	public static void sendEmail(Session session, String email, String subject, String body)
	  throws UnsupportedEncodingException, MessagingException {

		String fromEmail = String.format("noreply@%s.appspotmail.com",
										 SystemProperty.applicationId.get());
		InternetAddress fromAddress = new InternetAddress(fromEmail, "AppHunt");
		Address[] replyToAddress = new Address[1];
		replyToAddress[0] = new InternetAddress("apphunt@livae.com", "AppHunt");
		javax.mail.Message msg = new MimeMessage(session);
		msg.setFrom(fromAddress);
		msg.setReplyTo(replyToAddress);
		InternetAddress recipientAddress = new InternetAddress(email);
		msg.addRecipient(javax.mail.Message.RecipientType.TO, recipientAddress);
		msg.setSubject(subject);
		msg.setText(body);
		Transport.send(msg);
	}

	@ApiMethod(path = "wakeup", httpMethod = ApiMethod.HttpMethod.GET)
	public void wakeup(User gUser) throws UnauthorizedException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Date now = new Date();
		PhoneUser phoneUser = AuthUtil.getPhoneUser(gUser);
		phoneUser.setLastAccess(now);
		ofy().save().entity(phoneUser);
	}

	@ApiMethod(path = "phone/{countryCode}/{number}",
			   httpMethod = ApiMethod.HttpMethod.GET)
	public PhoneUser register(@Named("number") String numberString,
							  @Named("countryCode") String countryCode, User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser != null) {
			throw new UnauthorizedException("Cannot register more devices from the same one");
		}
		Long number = InputUtil.getValidNumber(numberString, countryCode);
		if (number == null) {
			throw new BadRequestException("Phone number not valid");
		}

		PhoneUser user = PhoneUser.get(number);
		if (user == null) {
			user = new PhoneUser(number);
			ofy().save().entity(user).now();
			ApiEndpoint.logger.info("Created user [id:" + number + "]");
		}
		String authToken = AuthUtil.createAuthToken(number);
		user.setAuthToken(authToken);
		return user;
	}

	@ApiMethod(path = "contacts",
			   httpMethod = ApiMethod.HttpMethod.POST)
	public void updateContacts(Collection<Long> numbers, User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
	}

}