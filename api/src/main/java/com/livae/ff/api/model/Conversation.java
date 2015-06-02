package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Id;

public class Conversation {

	@Id
	private Long id;

	private Long[] usersId;

	private Boolean open;



}
