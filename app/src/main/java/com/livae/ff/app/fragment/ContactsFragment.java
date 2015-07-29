package com.livae.ff.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.ContactsActivity;
import com.livae.ff.app.adapter.UsersAdapter;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.model.UserModel;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.Debug;

public class ContactsFragment extends AbstractFragment
  implements UserClickListener, SearchView.OnQueryTextListener,
			 LoaderManager.LoaderCallbacks<Cursor> {

	private static final int LOAD_CONTACTS = 1;

	protected UsersAdapter usersAdapter;

	private String searchText;

	private TextView emptyView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getLoaderManager().initLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_items, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		emptyView = (TextView) view.findViewById(R.id.empty_view);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		usersAdapter = new UsersAdapter(getActivity(), this);
		recyclerView.setAdapter(usersAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_search, menu);

		MenuItem searchMenuItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchMenuItem.getActionView();
		searchView.setOnQueryTextListener(this);
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public boolean onQueryTextChange(String text) {
		if (!TextUtils.isEmpty(text)) {
			text = text.trim().toLowerCase();
		}
		searchText = text;
		getLoaderManager().restartLoader(LOAD_CONTACTS, Bundle.EMPTY, this);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
			case LOAD_CONTACTS:
				String selection;
				String[] selectionArgs;
				String order;
				String myPhone = Application.appUser().getUserPhone().toString();
				if (TextUtils.isEmpty(searchText)) {
					selection = Table.LocalUser.IS_MOBILE_NUMBER + " AND " +
								Table.LocalUser.ACCEPTS_PRIVATE + " AND " +
								Table.LocalUser.PHONE + " !=?";
					selectionArgs = new String[]{myPhone};
				} else {
					selection = Table.LocalUser.IS_MOBILE_NUMBER + " AND " +
								Table.LocalUser.ACCEPTS_PRIVATE + " AND " +
								Table.LocalUser.CONTACT_NAME + " LIKE ? AND " +
								Table.LocalUser.PHONE + " !=?";
					selectionArgs = new String[]{"%" + searchText + "%", myPhone};
				}
				order = Table.LocalUser.CONTACT_NAME + " COLLATE NOCASE";
				return new CursorLoader(getActivity(), ContactsProvider.getUriContacts(),
										UsersAdapter.PROJECTION, selection, selectionArgs, order);
			// break
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Debug.print(data);
		usersAdapter.setSearch(searchText);
		usersAdapter.setCursor(data);
		usersAdapter.notifyDataSetChanged();
		if (data.getCount() == 0) {
			emptyView.setVisibility(View.VISIBLE);
			if (searchText == null) {
				emptyView.setText(R.string.no_contacts_in_app);
			} else {
				emptyView.setText(R.string.nothing_found);
			}
		} else {
			emptyView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	@Override
	public void userClicked(UserModel userModel) {
		FragmentActivity activity = getActivity();
		Intent data = new Intent();
		data.putExtra(ContactsActivity.SELECTED_DISPLAY_NAME, userModel.userDisplayName);
		data.putExtra(ContactsActivity.SELECTED_PHONE, userModel.userId);
		data.putExtra(ContactsActivity.SELECTED_USER_BLOCKED, userModel.userBlocked);
		activity.setResult(Activity.RESULT_OK, data);
		activity.finish();
	}

}
