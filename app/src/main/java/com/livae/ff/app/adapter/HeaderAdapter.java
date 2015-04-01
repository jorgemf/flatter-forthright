package com.livae.ff.app.adapter;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.apphunt.app.R;
import com.livae.apphunt.app.viewholders.HeaderTextViewHolder;

public class HeaderAdapter<VH extends RecyclerView.ViewHolder>
  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_HEADER = -100;

	private RecyclerView.Adapter<VH> wrappedAdapter;

	private int headerTextResId;

	public HeaderAdapter(RecyclerView.Adapter<VH> wrappedAdapter, @StringRes int headerTextResId) {
		this.wrappedAdapter = wrappedAdapter;
		this.headerTextResId = headerTextResId;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_HEADER) {
			LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
			View view = layoutInflater.inflate(R.layout.item_header_text, parent, false);
			return new HeaderTextViewHolder(view);
		} else {
			return wrappedAdapter.onCreateViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (position == 0) {
			HeaderTextViewHolder textViewHolder = (HeaderTextViewHolder) holder;
			textViewHolder.setText(headerTextResId);
		} else {
			wrappedAdapter.onBindViewHolder((VH) holder, position - 1);
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return TYPE_HEADER;
		} else {
			return wrappedAdapter.getItemViewType(position - 1);
		}
	}

	@Override
	public int getItemCount() {
		return 1 + wrappedAdapter.getItemCount();
	}
}
