package com.livae.ff.app.task;

import com.livae.ff.api.ff.model.Text;

public class TextId {

	private String text;

	private long id;

	public TextId(String text, long id) {
		this.text = text;
		this.id = id;
	}

	public Text getText() {
		Text t = new Text();
		t.setText(text);
		return t;
	}

	public long getId() {
		return id;
	}
}
