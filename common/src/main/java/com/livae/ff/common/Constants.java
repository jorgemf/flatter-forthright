package com.livae.ff.common;

public class Constants {

	public enum CommentVoteType {
		AGREE, DISAGREE
	}

	public enum ChatType {
		FLATTER, // Only positive comments about the person
		FORTHRIGHT, // Real things about the person
		PRIVATE_ANONYMOUS, // Private and anonymous chat
		PRIVATE, // Private chat between 2
		SECRET // Secret chat, comments are deleted after reading them
	}

	public enum Profile {
		ADMIN
	}

	public enum Platform {
		ANDROID
	}

	public enum FlagReason {
		ABUSE, INSULT, LIE, OTHER
	}

	public enum UserMark {
		BULLY, TROLL, LIAR, CONTROVERSIAL
	}
}
