package com.livae.ff.app.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.livae.ff.app.R;

public class WebViewActivity extends AbstractActivity {

	public static final String EXTRA_URL = "EXTRA_URL";

	public static final String EXTRA_TITLE = "EXTRA_TITLE";

	private ProgressBar progressBar;

	private WebView webView;

	public static void start(Activity activity, String url) {
		Intent intent = new Intent(activity, WebViewActivity.class);
		intent.putExtra(EXTRA_URL, url);
		intent.putExtra(EXTRA_TITLE, "");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Toolbar toolbar = ((AbstractActivity) activity).getToolbar();
			if (toolbar != null) {
				ActivityOptions options;
				options =
				  ActivityOptions.makeSceneTransitionAnimation(activity, toolbar, "toolbar");
				activity.startActivity(intent, options.toBundle());
			} else {
				//noinspection unchecked
				activity.startActivity(intent,
									   ActivityOptions.makeSceneTransitionAnimation(activity)
													  .toBundle());
			}
		} else {
			activity.startActivity(intent);
		}
	}

	public static void start(Context context, String url, String title) {
		Intent intent = new Intent(context, WebViewActivity.class);
		intent.putExtra(EXTRA_URL, url);
		intent.putExtra(EXTRA_TITLE, title);
		context.startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case android.R.id.home:
				super.onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(menuItem);
		}
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			super.onBackPressed();
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);

		// Set up the action bar.
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getIntent().getStringExtra(EXTRA_TITLE));
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//noinspection ConstantConditions
		progressBar = (ProgressBar) findViewById(R.id.progress_bar);

		webView = (WebView) findViewById(R.id.web_view);
		//noinspection ConstantConditions
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setSupportZoom(true);
		webView.setWebViewClient(new WebViewClient() {

			public void onReceivedError(WebView view, int errorCode, String description,
										String failingUrl) {
				Snackbar.make(webView, description, Snackbar.LENGTH_LONG).show();
			}
		});
		progressBar.setIndeterminate(true);
		webView.setWebChromeClient(new WebChromeClient() {

			public void onProgressChanged(WebView view, int progress) {
				if (progress > 15) {
					progressBar.setIndeterminate(false);
					progressBar.setProgress(progress);
					if (progress >= 90) {
						progressBar.setVisibility(View.GONE);
					}
				}
			}
		});

		Intent intent = getIntent();
		String url = intent.getStringExtra(EXTRA_URL);
		webView.loadUrl(url);
	}
}
