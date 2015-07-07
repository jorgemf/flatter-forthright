package com.livae.ff.api.v1;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.objectify.cmd.Query;
import com.livae.ff.api.Settings;
import com.livae.ff.api.auth.AppAuthenticator;
import com.livae.ff.api.auth.AuthUtil;
import com.livae.ff.api.model.Comment;
import com.livae.ff.api.model.CommentVote;
import com.livae.ff.api.model.Conversation;
import com.livae.ff.api.model.CounterStats;
import com.livae.ff.api.model.FlagComment;
import com.livae.ff.api.model.PhoneUser;
import com.livae.ff.api.model.Version;
import com.livae.ff.api.util.InputUtil;
import com.livae.ff.api.v1.model.FlagText;
import com.livae.ff.api.v1.model.Numbers;
import com.livae.ff.api.v1.model.Text;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.CommentVoteType;
import com.livae.ff.common.Constants.Platform;
import com.livae.ff.common.Constants.UserMark;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
		InternetAddress fromAddress = new InternetAddress(fromEmail, "Thoughts");
		Address[] replyToAddress = new Address[1];
		replyToAddress[0] = new InternetAddress("thoughts@livae.com", "Thoughts");
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
	public void wakeup(@Nullable @Named("deviceId") String deviceId, User gUser)
	  throws UnauthorizedException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Date now = new Date();
		PhoneUser phoneUser = AuthUtil.getPhoneUser(gUser);
		phoneUser.setLastAccess(now);
		if (deviceId != null) {
			phoneUser.setDeviceId(deviceId);
		}
		ofy().save().entity(phoneUser);
	}

	@ApiMethod(path = "phone/{number}",
				httpMethod = ApiMethod.HttpMethod.GET)
	public PhoneUser register(@Named("number") Long number,
							  @Nullable @Named("deviceId") String deviceId, User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser != null) {
			throw new UnauthorizedException("Cannot register more devices from the same one");
		}
		if (!InputUtil.isValidNumber(number)) {
			throw new BadRequestException("Phone number not valid");
		}

		PhoneUser user = new PhoneUser(number);
		ApiEndpoint.logger.info("Created user [id:" + number + "]");
		user.setDeviceId(deviceId);
		String authToken = AuthUtil.createAuthToken(number);
		user.setAuthToken(authToken);
		ofy().save().entity(user).now();
		return user;
	}

	@ApiMethod(path = "me",
				httpMethod = ApiMethod.HttpMethod.GET)
	public PhoneUser getUserInfo(User gUser) throws UnauthorizedException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		return AuthUtil.getPhoneUser(gUser);
	}

	@ApiMethod(path = "me/chats/forthright/block",
				httpMethod = ApiMethod.HttpMethod.GET)
	public void blockForthright(User gUser) throws UnauthorizedException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		user.setForthrightChatsDateBlocked(new Date());
		ofy().save().entity(user);
	}

	@ApiMethod(path = "me/chats/forthright/unblock",
				httpMethod = ApiMethod.HttpMethod.GET)
	public void unblockForthright(User gUser) throws UnauthorizedException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		user.setForthrightChatsDateBlocked(null);
		ofy().save().entity(user);
	}

	@ApiMethod(path = "conversation/{conversationId}",
				httpMethod = ApiMethod.HttpMethod.GET)
	public Conversation getConversation(@Named("conversationId") Long conversationId, User gUser)
	  throws UnauthorizedException, NotFoundException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Conversation conversation = Conversation.get(conversationId);
		if (conversation != null) {
			ChatType chatType = conversation.getType();
			PhoneUser user = AuthUtil.getPhoneUser(gUser);
			Long userPhone = user.getPhone();
			switch (chatType) {
				case PRIVATE:
				case SECRET:
					for (Long otherPhone : conversation.getUsers()) {
						if (!otherPhone.equals(userPhone)) {
							conversation.setPhone(otherPhone);
						}
					}
					// no break
				case PRIVATE_ANONYMOUS:
					if (!conversation.getUsers().contains(userPhone)) {
						conversation = null;
					}
					break;
				case FLATTER:
				case FORTHRIGHT:
					// nothing
			}
		}
		if (conversation == null) {
			throw new NotFoundException("Conversation not found");
		}
		return conversation;
	}

	@ApiMethod(path = "conversation/{conversationId}/join",
				httpMethod = ApiMethod.HttpMethod.GET)
	public void joinConversation(@Named("conversationId") Long conversationId, User gUser)
	  throws UnauthorizedException, NotFoundException {
		Conversation conversation = getConversation(conversationId, gUser);
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (user.getLastConversationId() != null) {
			Conversation lastConversation = Conversation.get(user.getLastConversationId());
			if (lastConversation != null) {
				lastConversation.removeUser(user.getPhone());
				ofy().save().entity(lastConversation);
			}
		}
		final ChatType conversationType = conversation.getType();
		if (conversationType == ChatType.FLATTER || conversationType == ChatType.FORTHRIGHT) {
			Date timeout = new Date(System.currentTimeMillis() + Settings.CONVERSATION_TIME_OUT);
			conversation.addUserNotification(user.getPhone(), timeout);
			ofy().save().entity(conversation);
			user.setLastConversationId(conversationId);
		} else {
			user.setLastConversationId(null);
		}
		ofy().save().entity(user);
	}

	@ApiMethod(path = "conversation/{conversationId}/leave",
				httpMethod = ApiMethod.HttpMethod.GET)
	public void leaveConversation(@Named("conversationId") Long conversationId, User gUser)
	  throws UnauthorizedException, NotFoundException {
		Conversation conversation = getConversation(conversationId, gUser);
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		final Long lastConversationId = user.getLastConversationId();
		if (lastConversationId != null && lastConversationId.equals(conversationId)) {
			user.setLastConversationId(null);
			ofy().save().entity(user);
		}
		conversation.removeUserNotification(user.getPhone());
		ofy().save().entity(conversation);
	}

	@ApiMethod(path = "conversation/phone/{phoneNumber}/{type}",
				httpMethod = ApiMethod.HttpMethod.GET)
	public Conversation getPhoneConversation(@Named("phoneNumber") Long phoneNumber,
											 @Named("type") ChatType type,
											 @Nullable @Named("roomName") String roomName,
											 User gUser)
	  throws UnauthorizedException, NotFoundException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		PhoneUser userConversation = PhoneUser.get(phoneNumber);
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		Conversation conversation = null;
		switch (type) {
			case PRIVATE_ANONYMOUS:
				if (userConversation == null) {
					throw new NotFoundException("User not in the platform");
				}
				if (roomName == null) {
					throw new BadRequestException("roomName required");
				} else if (roomName.length() > Settings.MAX_ROOM_NAME_CHARS) {
					roomName = roomName.substring(0, Settings.MAX_ROOM_NAME_CHARS - 1);
				}
				conversation = new Conversation(type);
				conversation.setAlias(roomName);
				conversation.addUser(userConversation.getPhone());
				conversation.addUser(user.getPhone());
				ofy().save().entity(conversation).now();
				break;
			case PRIVATE:
			case SECRET:
				if (userConversation == null) {
					throw new NotFoundException("User not in the platform");
				}
				// find previous conversation
				String mixPhones = Conversation.mixPhones(phoneNumber, user.getPhone());
				conversation = ofy().load().type(Conversation.class).filter("type", type)
									.filter("phones", mixPhones).first().now();
				// create a new one
				if (conversation == null) {
					conversation = new Conversation(type);
					conversation.setPhones(mixPhones);
					conversation.addUser(userConversation.getPhone());
					conversation.addUser(user.getPhone());
					ofy().save().entity(conversation).now();
				}
				break;
			case FLATTER:
			case FORTHRIGHT:
				if (!InputUtil.isValidNumber(phoneNumber)) {
					throw new BadRequestException("Number not valid");
				}
				// find previous conversation
				conversation = ofy().load().type(Conversation.class).filter("type", type)
									.filter("phone", phoneNumber).first().now();
				// create a new one
				if (conversation == null) {
					conversation = new Conversation(type);
					conversation.setPhone(phoneNumber);
					ofy().save().entity(conversation).now();
				}
				break;
		}
		return conversation;
	}

	@ApiMethod(path = "conversation/{conversationId}/comment",
				httpMethod = ApiMethod.HttpMethod.POST)
	public Comment postComment(@Named("conversationId") Long conversationId,
							   @Nullable @Named("alias") String alias, Text text, User gUser)
	  throws UnauthorizedException, BadRequestException, NotFoundException, ForbiddenException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		if (text == null || InputUtil.isEmpty(text.getText())) {
			throw new BadRequestException("Comment cannot be empty");
		}
		Conversation conversation = getConversation(conversationId, gUser);
		if (conversation == null) {
			throw new NotFoundException("Conversation does not exists");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		final Long userPhone = user.getPhone();
		Comment comment = new Comment(conversationId, userPhone, text.getText());
		switch (conversation.getType()) {
			case FORTHRIGHT:
			case FLATTER:
				if (conversation.getPhone().equals(user.getPhone())) {
					throw new ForbiddenException("User cannot write in himself/herself");
				}
				if (alias == null) {
					throw new BadRequestException("Alias cannot be empty");
				} else if (alias.length() > Settings.MAX_ROOM_NAME_CHARS) {
					alias = alias.substring(0, Settings.MAX_ROOM_NAME_CHARS - 1);
				}

				Comment previousComment;
				previousComment = ofy().load().type(Comment.class).filter("userId", userPhone)
									   .filter("conversationId", conversationId).filter("deleted",
																						false)
									   .order("-date").first().now();
				long aliasId;
				if (previousComment != null && previousComment.getAlias().equals(alias)) {
					aliasId = previousComment.getAliasId();
				} else {
					aliasId = obfuscatePhone(userPhone);
				}
				comment.setAlias(alias);
				comment.setAliasId(aliasId);
				if (user.getTimesFlagged() != null &&
					user.getTimesFlagged() >= Settings.MIN_FLAG_TO_MARK_USER) {
					Integer[] flaggedType = user.getTimesFlaggedType();
					int maxPos = 0;
					int maxValue = flaggedType[0];
					for (int i = 1; i < flaggedType.length; i++) {
						if (maxValue < flaggedType[i]) {
							maxPos = i;
							maxValue = flaggedType[i];
						}
					}
					comment.setUserMark(UserMark.values()[maxPos]);
				}
				break;
			case SECRET:
			case PRIVATE:
			case PRIVATE_ANONYMOUS:
				break;
		}
		CounterStats counterStats = ofy().load().type(CounterStats.class).first().now();
		if (counterStats == null) {
			counterStats = new CounterStats();
		}
		switch (conversation.getType()) {
			case SECRET:
				counterStats.setSecretMessages(counterStats.getSecretMessages() + 1);
				break;
			case PRIVATE:
				counterStats.setPrivateMessages(counterStats.getPrivateMessages() + 1);
				break;
			case PRIVATE_ANONYMOUS:
				counterStats.setAnonymousMessages(counterStats.getAnonymousMessages() + 1);
				break;
			case FLATTER:
				counterStats.setFlatteredMessages(counterStats.getFlatteredMessages() + 1);
				break;
			case FORTHRIGHT:
				counterStats.setForthrightMessages(counterStats.getForthrightMessages() + 1);
				break;
		}
		ofy().save().entity(counterStats).now();
		ofy().save().entity(comment).now();
		// create notifications
		Queue queue = QueueFactory.getQueue("create-comments-queue");
		String idString = Long.toString(comment.getId());
		queue.add(TaskOptions.Builder.withUrl("/createcommentworker?id=" + idString)
									 .method(TaskOptions.Method.GET));
		return comment;
	}

	@ApiMethod(path = "conversation/{conversationId}/comments",
				httpMethod = ApiMethod.HttpMethod.GET)
	public CollectionResponse<Comment> getComments(@Named("conversationId") Long conversationId,
												   @Named("cursor") @Nullable String cursor,
												   @Named("limit") @Nullable Integer limit,
												   @Named("date") @Nullable Long date, User gUser)
	  throws UnauthorizedException, BadRequestException, NotFoundException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		// verify the user can access to the conversation
		Conversation conversation = getConversation(conversationId, gUser);
		final boolean isPublicChat = conversation.getType() == ChatType.FORTHRIGHT ||
									 conversation.getType() == ChatType.FLATTER;
		if (!isPublicChat) {
			throw new BadRequestException("Cannot get comments from private conversations");
		}
		// get comments
		limit = InputUtil.getLimit(limit);
		Query<Comment> query;
		query = ofy().load().type(Comment.class).filter("conversationId", conversationId)
					 .filter("deleted", false).order("-date").limit(limit);
		if (date != null) {
			query.filter("date <=", new Date(date));
		}
		// anonymous messages are forgotten after 100 days
		query.filter("date >=", new Date(System.currentTimeMillis() - Settings.MAX_COMMENT_DATE));
		if (cursor != null) {
			query = query.startAt(Cursor.fromWebSafeString(cursor));
		}
		Long dateBlocked = null;
		if (conversation.getType() == ChatType.FORTHRIGHT) {
			PhoneUser conversationUser = PhoneUser.get(conversation.getPhone());
			if (conversationUser != null &&
				conversationUser.getForthrightChatsDateBlocked() != null) {
				dateBlocked = conversationUser.getForthrightChatsDateBlocked().getTime();
			}
		}

		QueryResultIterator<Comment> queryIterator = query.iterator();
		List<Comment> commentList = new ArrayList<>(limit);
		Long userPhone = AuthUtil.getPhoneUser(gUser).getPhone();
		while (queryIterator.hasNext()) {
			Comment comment = queryIterator.next();
			final boolean isMe = comment.getUserId().equals(userPhone);
			comment.setIsMe(isMe);
			// set comment vote type
			CommentVote commentVote;
			commentVote = ofy().load().type(CommentVote.class).filter("userId", userPhone)
							   .filter("commentId", comment.getId()).first().now();
			if (commentVote != null) {
				comment.setVoteType(commentVote.getType());
			}
			if (!isMe && dateBlocked != null && dateBlocked > comment.getDate().getTime()) {
				// hide comments since the user blocked the list
				comment.setComment(null);
			}
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
		Conversation conversation = Conversation.get(comment.getConversationId());
		if (conversation.getType() != ChatType.FORTHRIGHT &&
			conversation.getType() != ChatType.FLATTER) {
			throw new BadRequestException("This comment cannot be voted");

		}
		CommentVote commentVote;
		commentVote = ofy().load().type(CommentVote.class).filter("userId", user.getPhone())
						   .filter("commentId", commentId).first().now();
		PhoneUser commentUser = PhoneUser.get(comment.getUserId());
		if (commentVote != null) {
			if (commentVote.getType() == CommentVoteType.DISAGREE) {
				commentVote.setType(CommentVoteType.AGREE);
				comment.setAgreeVotes(comment.getAgreeVotes() + 1);
				comment.setDisagreeVotes(comment.getDisagreeVotes() - 1);
				commentUser.setTimesAgreed(commentUser.getTimesAgreed() + 1);
				commentUser.setTimesDisagreed(commentUser.getTimesDisagreed() - 1);

			} else {
				throw new BadRequestException("Comment already agreed");
			}
		} else {
			commentVote = new CommentVote(comment.getId(), user.getPhone(), CommentVoteType.AGREE);
			comment.setAgreeVotes(comment.getAgreeVotes() + 1);
			commentUser.setTimesAgreed(commentUser.getTimesAgreed() + 1);
		}
		if (user.getPhone().equals(comment.getUserId())) {
			comment.setUserVoteType(CommentVoteType.AGREE);
		}
		ofy().save().entity(commentVote);
		ofy().save().entity(comment);
		ofy().save().entity(commentUser);
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
		Conversation conversation = Conversation.get(comment.getConversationId());
		if (conversation.getType() != ChatType.FORTHRIGHT &&
			conversation.getType() != ChatType.FLATTER) {
			throw new BadRequestException("This comment cannot be voted");

		}
		CommentVote commentVote;
		commentVote = ofy().load().type(CommentVote.class).filter("userId", user.getPhone())
						   .filter("commentId", commentId).first().now();
		PhoneUser commentUser = PhoneUser.get(comment.getUserId());
		if (commentVote != null) {
			if (commentVote.getType() == CommentVoteType.AGREE) {
				commentVote.setType(CommentVoteType.DISAGREE);
				comment.setAgreeVotes(comment.getAgreeVotes() - 1);
				comment.setDisagreeVotes(comment.getDisagreeVotes() + 1);
				commentUser.setTimesAgreed(commentUser.getTimesAgreed() - 1);
				commentUser.setTimesDisagreed(commentUser.getTimesDisagreed() + 1);
			} else {
				throw new BadRequestException("Comment already disagreed");
			}
		} else {
			commentVote = new CommentVote(comment.getId(), user.getPhone(),
										  CommentVoteType.DISAGREE);
			comment.setDisagreeVotes(comment.getDisagreeVotes() + 1);
			commentUser.setTimesDisagreed(commentUser.getTimesDisagreed() + 1);
		}
		if (user.getPhone().equals(comment.getUserId())) {
			comment.setUserVoteType(CommentVoteType.DISAGREE);
		}
		ofy().save().entity(commentVote);
		ofy().save().entity(comment);
		ofy().save().entity(commentUser);
		return comment;
	}

	@ApiMethod(path = "comment/{commentId}/novote",
				httpMethod = ApiMethod.HttpMethod.DELETE)
	public Comment noVoteComment(@Named("commentId") Long commentId, User gUser)
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
		Conversation conversation = Conversation.get(comment.getConversationId());
		if (conversation.getType() != ChatType.FORTHRIGHT &&
			conversation.getType() != ChatType.FLATTER) {
			throw new BadRequestException("This comment cannot be voted");

		}
		CommentVote commentVote;
		commentVote = ofy().load().type(CommentVote.class).filter("userId", user.getPhone())
						   .filter("commentId", commentId).first().now();
		PhoneUser commentUser = PhoneUser.get(comment.getUserId());
		if (commentVote != null) {
			switch (commentVote.getType()) {
				case DISAGREE:
					comment.setDisagreeVotes(comment.getDisagreeVotes() - 1);
					commentUser.setTimesDisagreed(commentUser.getTimesDisagreed() - 1);
					break;
				case AGREE:
					comment.setAgreeVotes(comment.getAgreeVotes() - 1);
					commentUser.setTimesAgreed(commentUser.getTimesAgreed() - 1);
					break;
			}
		} else {
			throw new NotFoundException("Comment was never voted");
		}
		if (user.getPhone().equals(comment.getUserId())) {
			comment.setUserVoteType(null);
		}
		ofy().delete().entity(commentVote);
		ofy().save().entity(comment);
		ofy().save().entity(commentUser);
		return comment;
	}

	@ApiMethod(path = "comment/{commentId}/flag",
				httpMethod = ApiMethod.HttpMethod.POST)
	public Comment flagComment(@Named("commentId") Long commentId, FlagText reason, User gUser)
	  throws UnauthorizedException, NotFoundException, ForbiddenException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Comment comment = Comment.get(commentId);
		if (comment == null) {
			throw new NotFoundException("Comment does not exists");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		FlagComment flag = ofy().load().type(FlagComment.class).filter("userId", user.getPhone())
								.filter("commentId", commentId).first().now();
		if (flag != null) {
			throw new ForbiddenException("Comment already flagged");
		}
		final Constants.FlagReason flagReason = reason.getReason();
		comment.flag(flagReason);
		PhoneUser flaggedUser = PhoneUser.get(comment.getUserId());
		flaggedUser.flag(flagReason);
		ofy().save().entity(comment);
		ofy().save().entity(flaggedUser);
		ofy().save().entity(new FlagComment(commentId, user.getPhone(), flagReason,
											reason.getText()));
		ApiEndpoint.logger.info("Comment flagged [" + comment.getComment() + "] [" +
								flagReason + ":" + reason.getText() + "]");
		return comment;
	}

	@ApiMethod(path = "conversation/{conversationId}/blockUser",
				httpMethod = ApiMethod.HttpMethod.GET)
	public void conversationBlockUser(@Named("conversationId") Long conversationId,
									  @Named("time") Long time, User gUser)
	  throws UnauthorizedException, NotFoundException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		if (time == null || time < 0) {
			throw new BadRequestException("Positive time required");
		}
		if (time > Settings.MAX_TIME_BLOCK_ANONYMOUS_USER) {
			time = Settings.MAX_TIME_BLOCK_ANONYMOUS_USER;
		}
		Conversation conversation = Conversation.get(conversationId);
		if (conversation == null || conversation.getType() != ChatType.PRIVATE_ANONYMOUS) {
			throw new NotFoundException("Conversation not found");
		}
		Collection<Long> phones = conversation.getUsers();
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		final Long userPhone = user.getPhone();
		if (!phones.contains(userPhone)) {
			throw new NotFoundException("Conversation not found");
		}
		if (phones.size() != 2) {
			throw new BadRequestException("Conversation has more than 2 users");
		}
		for (Long phone : phones) {
			if (!userPhone.equals(phone)) {
				user.addBlockedAnonymousPhone(phone, new Date(System.currentTimeMillis() + time));
			}
		}
		ofy().save().entity(user);
	}

	@ApiMethod(path = "user/{phone}/blockUser",
				httpMethod = ApiMethod.HttpMethod.GET)
	public void blockUser(@Named("phone") Long phoneNumber, User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		if (!InputUtil.isValidNumber(phoneNumber)) {
			throw new BadRequestException("Invalid phone number");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		if (!user.getPhone().equals(phoneNumber)) {
			user.addBlockedPhone(phoneNumber);
			ofy().save().entity(user);
		}
	}

	@ApiMethod(path = "user/{phone}/unblockUser",
				httpMethod = ApiMethod.HttpMethod.GET)
	public void unblockUser(@Named("phone") Long phoneNumber, User gUser)
	  throws UnauthorizedException, BadRequestException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		if (!InputUtil.isValidNumber(phoneNumber)) {
			throw new BadRequestException("Invalid phone number");
		}
		PhoneUser user = AuthUtil.getPhoneUser(gUser);
		user.removeBlockedPhone(phoneNumber);
		ofy().save().entity(user);
	}

//	@ApiMethod(path = "user/blocked",
//				httpMethod = ApiMethod.HttpMethod.GET)
//	public Numbers getBlockedUsers(User gUser) throws UnauthorizedException, NotFoundException {
//		if (gUser == null) {
//			throw new UnauthorizedException("User not authorized");
//		}
//		PhoneUser user = AuthUtil.getPhoneUser(gUser);
//		return user.getBlockedChats();
//	}

	@ApiMethod(path = "contacts",
				httpMethod = ApiMethod.HttpMethod.POST)
	public Numbers getContacts(Numbers phones, User gUser) throws UnauthorizedException {
		if (gUser == null) {
			throw new UnauthorizedException("User not authorized");
		}
		Numbers appPhones = new Numbers();
		for (Long phone : phones.getNumbers()) {
			// send all the contacts and return the ones that have or had the app installed
			PhoneUser user = PhoneUser.get(phone);
			if (user != null) {
				appPhones.addNumber(phone);
			}
		}
		return appPhones;
	}

}