package com.livae.ff.app.task;

import com.livae.ff.api.ff.model.Text;

public class TextId {

	private String text;

	private long id;

	private String alias;

	public TextId(String text, long id) {
		this.text = text;
		this.id = id;
	}

	public TextId(String text, long id, String alias) {
		this(text, id);
		this.alias = alias;
	}

	public Text getText() {
		Text t = new Text();
		t.setText(text);
		return t;
	}

	public long getId() {
		return id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
