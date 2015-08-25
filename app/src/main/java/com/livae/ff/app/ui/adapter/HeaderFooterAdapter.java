package com.livae.ff.app.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class HeaderFooterAdapter<ADAPTER extends RecyclerView.Adapter>
  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int HEADER_TYPE = -1001;

	private static final int FOOTER_TYPE = -1002;

	private ADAPTER adapter;

	private HeaderCreator headerCreator;

	private FooterCreator footerCreator;

	public HeaderFooterAdapter(@NonNull ADAPTER adapter,
							   HeaderCreator headerCreator,
							   FooterCreator footerCreator) {
		this.adapter = adapter;
		setHasStableIds(this.adapter.hasStableIds());
		this.headerCreator = headerCreator;
		this.footerCreator = footerCreator;
		this.adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				HeaderFooterAdapter.this.notifyDataSetChanged();
			}

			@Override
			public void onItemRangeChanged(int positionStart, int itemCount) {
				super.onItemRangeChanged(positionStart, itemCount);
				positionStart += (HeaderFooterAdapter.this.headerCreator != null ? 1 : 0);
				HeaderFooterAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
			}

			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				positionStart += (HeaderFooterAdapter.this.headerCreator != null ? 1 : 0);
				HeaderFooterAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
			}

			@Override
			public void onItemRangeRemoved(int positionStart, int itemCount) {
				super.onItemRangeRemoved(positionStart, itemCount);
				positionStart += (HeaderFooterAdapter.this.headerCreator != null ? 1 : 0);
				HeaderFooterAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
			}

			@Override
			public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
				super.onItemRangeMoved(fromPosition, toPosition, itemCount);
				if (itemCount == 1) {
					fromPosition += (HeaderFooterAdapter.this.headerCreator != null ? 1 : 0);
					toPosition += (HeaderFooterAdapter.this.headerCreator != null ? 1 : 0);
					HeaderFooterAdapter.this.notifyItemMoved(fromPosition, toPosition);
				} else {
					throw new UnsupportedOperationException("Not supported");
				}
			}
		});
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case HEADER_TYPE:
				return new CustomViewHolder(headerCreator.createHeader(parent));
			// break;
			case FOOTER_TYPE:
				return new CustomViewHolder(footerCreator.createFooter(parent));
			// break;
			default:
				return adapter.createViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (holder.getItemViewType()) {
			case HEADER_TYPE:
				// nothing
				break;
			case FOOTER_TYPE:
				// nothing
				break;
			default:
				adapter.onBindViewHolder(holder, position);
				break;
		}
	}

	@Override
	public int getItemViewType(int position) {
		if (headerCreator != null && position == 0) {
			return HEADER_TYPE;
		}
		if (footerCreator != null) {
			if (headerCreator == null && position == adapter.getItemCount()) {
				return FOOTER_TYPE;
			} else if (headerCreator != null && position == adapter.getItemCount() + 1) {
				return FOOTER_TYPE;
			}
		}
		return adapter.getItemViewType(getAdapterPos(position));
	}

	@Override
	public void setHasStableIds(boolean hasStableIds) {
		super.setHasStableIds(hasStableIds);
		adapter.setHasStableIds(hasStableIds);
	}

	public long getItemId(int position) {
		position = getAdapterPos(position);
		if (position < 0) {
			return super.getItemId(position); // returns NO_ID
		}
		return adapter.getItemId(position);
	}

	@Override
	public int getItemCount() {
		return adapter.getItemCount() + (headerCreator != null ? 1 : 0) +
			   (footerCreator != null ? 1 : 0);
	}

	private int getAdapterPos(int pos) {
		if (headerCreator != null) {
			pos--;
		}
		if (pos < 0 || pos >= adapter.getItemCount()) {
			return -1;
		}
		return pos;
	}

	public interface HeaderCreator {

		@NonNull
		View createHeader(ViewGroup parent);
	}

	public interface FooterCreator {

		@NonNull
		View createFooter(ViewGroup footer);
	}

	class CustomViewHolder extends RecyclerView.ViewHolder {

		public CustomViewHolder(View itemView) {
			super(itemView);
		}
	}
}
