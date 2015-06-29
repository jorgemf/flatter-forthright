package com.livae.ff.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.adapter.UsersAdapter;
import com.livae.ff.app.listener.UserClickListener;

public abstract class ContactsFragment extends AbstractFragment implements UserClickListener {

	protected UsersAdapter usersAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_items, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		usersAdapter = new UsersAdapter(getActivity(), this);
		recyclerView.setAdapter(usersAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

}
