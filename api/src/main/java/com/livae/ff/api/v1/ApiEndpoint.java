package com.livae.ff.api.v1;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.users.User;
import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.cmd.Query;
import com.livae.ff.api.Settings;
import com.livae.ff.api.auth.AppAuthenticator;
import com.livae.ff.api.auth.AuthUtil;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.CommentVote;
import com.livae.ff.api.model.Contact;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.model.Version;
import com.livae.ff.api.util.InputUtil;
import com.livae.ff.api.v1.model.Contacts;
import com.livae.ff.api.v1.model.Text;
import com.livae.ff.common.Constants.CommentType;
import com.livae.ff.common.Constants.CommentVoteType;
import com.livae.ff.common.Constants.Platform;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
		replyToAddress[0] = new InternetAddress("ff@livae.com", "AppHunt");
		javax.mail.Message msg = new MimeMessage(session);
		msg.setFrom(fromAddress);
		msg.setReplyTo(replyToAddress);
		InternetAddress recipientAddress = new InternetAddress(email);
		msg.addRecipient(javax.mail.Message.RecipientType.TO, recipientAddress);
		msg.setSubject(subject);
		msg.setText(body);
		Transport.send(msg);
	}

	private static Long obfuscatePhone(Long userPhone) {
		Random random = new Random();
		return userPhone ^ random.nextLong();
	}

	@ApiMethod(path = "version/{platform}", httpMethod = ApiMethod.HttpMethod.GET)
	public Version version(@Named("platform") Platform platform) throws NotFoundException {
		Version version = Version.getVersion(platform);
		if (version == null) {
			throw new NotFoundException("Platform not found: " + platform.name());
		}
		return version;
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
	public void updateContacts(Contacts contacts, User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (user == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Long userPhone = user.getPhone();
		List<Contact> contactsList;
		contactsList = ofy().load().type(Contact.class).filter("userPhone", userPhone).list();
		// to hash set
		HashMap<Long, Contact> contactsNumbers = new HashMap<>();
		for (Contact contact : contactsList) {
			contactsNumbers.put(contact.getContactPhone(), contact);
		}

		String defaultCountry = contacts.getCountryCode();
		HashSet<Long> newContacts = new HashSet<>();
		for (String numberString : contacts.getNumbers()) {
			Long number = InputUtil.getValidNumber(numberString, defaultCountry);
			if (number != null) {
				newContacts.add(number);
				if (!contactsNumbers.containsKey(number)) {
					// add new contact
					Contact newContact = new Contact(userPhone, number);
					ofy().save().entity(newContact);
				}
			}
		}
		for (Contact contact : contactsList) {
			if (!newContacts.contains(contact.getContactPhone())) {
				// delete old contact
				ofy().delete().entity(contact);
			}
		}
	}

	@ApiMethod(path = "user/{contactPhone}/{commentType}/comment",
			   httpMethod = ApiMethod.HttpMethod.POST)
	public Comment postComment(@Named("contactPhone") Long contactPhone,
							   @Named("commentType") CommentType commentType, Text text, User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (user == null) {
			throw new UnauthorizedException("User not authorized");
		}
		if (!InputUtil.isValidNumber(contactPhone)) {
			throw new BadRequestException("Invalid phone number");
		}
		String commentText = text.getText();
		if (InputUtil.isEmpty(commentText)) {
			throw new BadRequestException("Empty comment");
		}
		Long userId = user.getPhone();
		// get previous comments for previous fake id
		Comment previousComment = ofy().load().type(Comment.class).filter("phone", contactPhone)
									   .filter("userId", userId).order("-date").first().now();
		Long userFakeId;
		if (previousComment == null || previousComment.getDate().getTime() <
									   System.currentTimeMillis() -
									   Settings.TIME_BETWEEN_ANONYMOUS_COMMENTS) {
			userFakeId = obfuscatePhone(userId);
		} else {
			userFakeId = previousComment.getUser();
		}
		Comment comment = new Comment(commentType, contactPhone, userFakeId, userId, commentText);
		ofy().save().entity(comment);
		return comment;
	}

	@ApiMethod(path = "user/{contactPhone}/{commentType}/comments",
			   httpMethod = ApiMethod.HttpMethod.GET)
	public CollectionResponse<Comment> getComments(@Named("contactPhone") Long contactPhone,
												   @Named("commentType") CommentType commentType,
												   @Named("cursor") @Nullable String cursor,
												   @Named("limit") @Nullable Integer limit,
												   User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (user == null) {
			throw new UnauthorizedException("User not authorized");
		}
		if (!InputUtil.isValidNumber(contactPhone)) {
			throw new BadRequestException("Invalid phone number");
		}
		limit = InputUtil.getLimit(limit);
		Query<Comment> query;
		query = ofy().load().type(Comment.class).filter("phone", contactPhone).order("-date")
					 .orderKey(true).limit(limit);
		if (cursor != null) {
			query = query.startAt(Cursor.fromWebSafeString(cursor));
		}
		QueryResultIterator<Comment> queryIterator = query.iterator();
		List<Comment> commentList = new ArrayList<>(limit);
		Long userId = user.getPhone();
		while (queryIterator.hasNext()) {
			Comment comment = queryIterator.next();
			// create user
			comment.getUser();

			// set comment vote type
			CommentVote commentVote;
			commentVote = ofy().load().type(CommentVote.class).filter("userId", userId)
							   .filter("commentId", comment.getId()).first().now();
			if (commentVote != null) {
				comment.setVoteType(commentVote.getType());
			}
			comment.setIsMe(userId);
			commentList.add(comment);
		}
		return CollectionResponse.<Comment>builder().setItems(commentList)
								 .setNextPageToken(queryIterator.getCursor().toWebSafeString())
								 .build();
	}

	@ApiMethod(path = "comment/{commentId}/agree",
			   httpMethod = ApiMethod.HttpMethod.GET)
	public Comment agreeComment(@Named("commentId") Long commentId, User gUser)
	  throws UnauthorizedException, NotFoundException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (user == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Comment comment = Comment.get(commentId);
		if (comment == null) {
			throw new NotFoundException("Comment does not exists");
		}
		CommentVote commentVote;
		commentVote = ofy().load().type(CommentVote.class).filter("commentId", commentId)
						   .filter("userId", user.getPhone()).first().now();
		if (commentVote != null) {
			if (commentVote.getType() == CommentVoteType.DISAGREE) {
				commentVote.setType(CommentVoteType.AGREE);
				comment.setAgreeVotes(comment.getAgreeVotes() + 1);
				comment.setDisagreeVotes(comment.getDisagreeVotes() - 1);
			} else {
				throw new BadRequestException("Comment already agreed");
			}
		} else {
			commentVote = new CommentVote(comment.getId(), user.getPhone(), CommentVoteType.AGREE);
			comment.setAgreeVotes(comment.getAgreeVotes() + 1);
		}
		if (user.getPhone().equals(comment.getPhone())) {
			comment.setUserVoteType(CommentVoteType.AGREE);
		}
		ofy().save().entity(commentVote);
		ofy().save().entity(comment);
		return comment;
	}

	@ApiMethod(path = "comment/{commentId}/disagree",
			   httpMethod = ApiMethod.HttpMethod.GET)
	public Comment disagreeComment(@Named("commentId") Long commentId, User gUser)
	  throws UnauthorizedException, NotFoundException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (user == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Comment comment = Comment.get(commentId);
		if (comment == null) {
			throw new NotFoundException("Comment does not exists");
		}
		CommentVote commentVote;
		commentVote = ofy().load().type(CommentVote.class).filter("commentId", commentId)
						   .filter("userId", user.getPhone()).first().now();
		if (commentVote != null) {
			if (commentVote.getType() == CommentVoteType.AGREE) {
				commentVote.setType(CommentVoteType.DISAGREE);
				comment.setAgreeVotes(comment.getAgreeVotes() - 1);
				comment.setDisagreeVotes(comment.getDisagreeVotes() + 1);
			} else {
				throw new BadRequestException("Comment already disagreed");
			}
		} else {
			commentVote = new CommentVote(comment.getId(), user.getPhone(),
										  CommentVoteType.DISAGREE);
			comment.setDisagreeVotes(comment.getDisagreeVotes() + 1);
		}
		if (user.getPhone().equals(comment.getPhone())) {
			comment.setUserVoteType(CommentVoteType.DISAGREE);
		}
		ofy().save().entity(commentVote);
		ofy().save().entity(comment);
		return comment;
	}

	@ApiMethod(path = "comment/{commentId}/novote",
			   httpMethod = ApiMethod.HttpMethod.DELETE)
	public Comment noVoteComment(@Named("commentId") Long commentId, User gUser)
	  throws UnauthorizedException, NotFoundException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (user == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Comment comment = Comment.get(commentId);
		if (comment == null) {
			throw new NotFoundException("Comment does not exists");
		}
		CommentVote commentVote;
		commentVote = ofy().load().type(CommentVote.class).filter("commentId", commentId)
						   .filter("userId", user.getPhone()).first().now();
		if (commentVote != null) {
			switch (commentVote.getType()) {
				case DISAGREE:
					comment.setDisagreeVotes(comment.getDisagreeVotes() - 1);
					break;
				case AGREE:
					comment.setAgreeVotes(comment.getAgreeVotes() - 1);
					break;
			}
		} else {
			throw new NotFoundException("Comment was never voted");
		}
		if (user.getPhone().equals(comment.getPhone())) {
			comment.setUserVoteType(null);
		}
		ofy().delete().entity(commentVote);
		ofy().save().entity(comment);
		return comment;
	}

}