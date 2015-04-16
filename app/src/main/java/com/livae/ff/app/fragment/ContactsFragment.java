package com.livae.ff.app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.livae.android.loading.CursorRecyclerAdapter;
import com.livae.apphunt.app.Analytics;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.activity.UserActivity;
import com.livae.apphunt.app.adapter.CommentsAdapter;
import com.livae.apphunt.app.async.NetworkAsyncTask;
import com.livae.apphunt.app.provider.DataProvider;
import com.livae.apphunt.app.task.ListResult;
import com.livae.apphunt.app.task.QueryParamId;
import com.livae.apphunt.app.task.TaskGetUserComments;
import com.livae.apphunt.app.viewholders.CommentsViewHolder;
import com.livae.apphunt.common.Constants.Order;

public class ContactsFragment extends AbstractCommentsFragment<QueryParamId> {

	private long userId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO

		// create ContextThemeWrapper from the original Activity Context with the custom theme
		final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(),
																	R.style.yourCustomTheme);

		// clone the inflater using the ContextThemeWrapper
		LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

		// inflate the layout using the cloned inflater, not default inflater
		return localInflater.inflate(R.layout.yourLayout, container, false);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setOrder(Order.DATE);
		Intent intent = getActivity().getIntent();
		userId = intent.getLongExtra(UserActivity.EXTRA_USER_ID, Application.appUser().getUserId());
	}

	@Override
	protected NetworkAsyncTask<QueryParamId, ListResult> getLoaderTask() {
		return new TaskGetUserComments();
	}

	@Override
	protected Uri getUriCursor() {
		return DataProvider.getUriUserApplicationsComments(userId);
	}

	@Override
	protected QueryParamId getBaseQueryParams() {
		return new QueryParamId(userId);
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getActivity().getIntent();
		Long thisUserId = Application.appUser().getUserId();
		userId = intent.getLongExtra(UserActivity.EXTRA_USER_ID, thisUserId);
		if (userId == thisUserId) {
			Analytics.screen(Analytics.Screen.MY_COMMENTS);
		} else {
			Analytics.screen(Analytics.Screen.USER_COMMENTS);
		}
	}

	@Override
	protected CursorRecyclerAdapter<CommentsViewHolder> getAdapter() {
		CommentsAdapter commentsAdapter = (CommentsAdapter) super.getAdapter();
		commentsAdapter.setShowApp(true);
		return commentsAdapter;
	}

}
