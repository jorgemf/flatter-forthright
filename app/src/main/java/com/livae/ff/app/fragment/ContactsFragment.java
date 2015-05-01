package com.livae.ff.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.adapter.UsersAdapter;
import com.livae.ff.app.listener.UserClickListener;

public class ContactsFragment extends AbstractFragment implements UserClickListener {

	private UsersAdapter usersAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		LayoutInflater localInflater = getLayoutInflater(inflater);
		return localInflater.inflate(R.layout.fragment_list_items, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		usersAdapter = new UsersAdapter(getActivity(), this, this);
		recyclerView.setAdapter(usersAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		view.findViewById(R.id.toggle_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AbstractActivity activity = (AbstractActivity) getActivity();
				activity.toggleApp();
			}
		});
	}

	@Override
	public void userClicked(Long userId, TextView name, ImageView image) {
		// TODO
	}

	//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setOrder(Order.DATE);
//		Intent intent = getActivity().getIntent();
//		userId = intent.getLongExtra(UserActivity.EXTRA_USER_ID, Application.appUser().getUserId());
//	}
//
//	@Override
//	protected NetworkAsyncTask<QueryComments, ListResult> getLoaderTask() {
//		return new TaskGetUserComments();
//	}
//
//	@Override
//	protected Uri getUriCursor() {
//		return DataProvider.getUriUserApplicationsComments(userId);
//	}
//
//	@Override
//	protected QueryComments getBaseQueryParams() {
//		return new QueryComments(userId);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		Intent intent = getActivity().getIntent();
//		Long thisUserId = Application.appUser().getUserId();
//		userId = intent.getLongExtra(UserActivity.EXTRA_USER_ID, thisUserId);
//		if (userId == thisUserId) {
//			Analytics.screen(Analytics.Screen.MY_COMMENTS);
//		} else {
//			Analytics.screen(Analytics.Screen.USER_COMMENTS);
//		}
//	}
//
//	@Override
//	protected CursorRecyclerAdapter<CommentsViewHolder> getAdapter() {
//		CommentsAdapter commentsAdapter = (CommentsAdapter) super.getAdapter();
//		commentsAdapter.setShowApp(true);
//		return commentsAdapter;
//	}

}
