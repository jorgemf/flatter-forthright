package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.livae.ff.common.Constants.Platform;

import java.io.Serializable;

import static com.livae.ff.api.OfyService.ofy;

@Entity
@Cache
public class Version implements Serializable {

	@Id
	private String platform;

	private Integer number;

	public Version() {
	}

	public static Version getVersion(Platform platform) {
		return ofy().load().type(Version.class).id(platform.toString()).now();
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}
}
