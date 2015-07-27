package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.annotation.Nonnull;

public abstract class EndlessCursorAdapter<k extends RecyclerView.ViewHolder>
  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_ERROR = -2;

	private static final int TYPE_LOADING = -3;

	private static final int TYPE_HEADER = -4;

	private static final int TYPE_FOOTER = -5;

	protected LayoutInflater layoutInflater;

	private View headerView;

	private View footerView;

	private boolean isLoading;

	private boolean isError;

	private Cursor cursor;

	private ViewCreator viewCreator;

	private int iId;

	public EndlessCursorAdapter(@Nonnull Context context, @Nonnull ViewCreator viewCreator) {
		this.layoutInflater = LayoutInflater.from(context);
		isLoading = false;
		isError = false;
		this.viewCreator = viewCreator;
		setHasStableIds(true);
	}

	protected Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		if (cursor != null) {
			iId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
			findIndexes(cursor);
		}
		int currentSize = getCursorItemCount();
		if (this.cursor != null && this.cursor != cursor) {
			this.cursor.close();
		}
		this.cursor = cursor;
		int newSize = getCursorItemCount();
		int diff = newSize - currentSize;
		int start = 0;
		if (headerView != null) {
			start++;
		}
//		if (diff > 0) {
//			notifyItemRangeInserted(start + currentSize, diff);
//		} else if (diff < 0) {
//			notifyItemRangeRemoved(start, -diff);
//		} else {
//			notifyDataSetChanged();
//		}
		notifyDataSetChanged();
		setIsLoading(false);
		setIsError(false);
	}

	public void setHeaderView(View headerView) {
		if (this.headerView != null) {
			notifyItemRemoved(0);
		}
		this.headerView = headerView;
		notifyItemInserted(0);
	}

	public void setFooterView(View footerView) {
		int size = getCursorItemCount();
		if (headerView != null) {
			size++;
		}
		if (isError) {
			size++;
		}
		if (isLoading) {
			size++;
		}
		if (this.footerView != null) {
			notifyItemRemoved(size);
		}
		this.footerView = footerView;
		notifyItemInserted(size);
	}

	protected abstract void findIndexes(@Nonnull Cursor cursor);

	public void setIsLoading(boolean loading) {
		if (loading != isLoading) {
			int size = getCursorItemCount();
			if (headerView != null) {
				size++;
			}
			isLoading = loading;
			if (isLoading) {
				if (isError) {
					isError = false;
					notifyItemRemoved(size);
				}
				notifyItemInserted(size);
			} else {
				notifyItemRemoved(size);
			}
		}
	}

	public void setIsError(boolean error) {
		if (error != isError) {
			int size = getCursorItemCount();
			if (headerView != null) {
				size++;
			}
			isError = error;
			if (isError) {
				notifyItemInserted(size);
				if (isLoading) {
					isLoading = false;
					notifyItemRemoved(size + 1);
				}
			} else {
				notifyItemRemoved(size);
			}
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		switch (type) {
			case TYPE_LOADING:
				return new EndlessViewHolder(viewCreator.createLoadingView(layoutInflater,
																		   viewGroup));
//			break;
			case TYPE_ERROR:
				return new EndlessViewHolder(viewCreator.createErrorView(layoutInflater,
																		 viewGroup));
//			break;
			case TYPE_HEADER:
				return new EndlessViewHolder(headerView);
//			break;
			case TYPE_FOOTER:
				return new EndlessViewHolder(footerView);
//			break;
			default:
				return createCustomViewHolder(viewGroup, type);
		}
//		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		int size = getCursorItemCount();
		if (headerView != null) {
			position--;
		}
		if (position >= 0 && position < size) {
			//noinspection ConstantConditions
			cursor.moveToPosition(position);
			//noinspection unchecked
			bindCustomViewHolder((k) viewHolder, position, cursor);
		}
	}

	public int getItemViewType(int position) {
		int size = getCursorItemCount();
		if (headerView != null && position == 0) {
			return TYPE_HEADER;
		}
		if (headerView != null) {
			position--;
		}
		if (position < size) {
			cursor.moveToPosition(position);
			return getCustomItemViewType(position, cursor);
		}
		if (isError && size == position) {
			return TYPE_ERROR;
		} else if (isError && isLoading && size + 1 == position) {
			return TYPE_LOADING;
		} else if (isError && isLoading && footerView != null && size + 2 == position) {
			return TYPE_FOOTER;
		} else if (isError && !isLoading && footerView != null && size + 1 == position) {
			return TYPE_FOOTER;
		} else if (!isError && isLoading && size == position) {
			return TYPE_LOADING;
		} else if (!isError && isLoading && footerView != null && size + 1 == position) {
			return TYPE_FOOTER;
		} else if (!isError && !isLoading && footerView != null && size == position) {
			return TYPE_FOOTER;
		} else {
			throw new RuntimeException("Should not happen");
		}
	}

	@Override
	public long getItemId(int position) {
		int size = getCursorItemCount();
		if (headerView != null) {
			position--;
		}
		if (position >= 0 && position < size) {
			cursor.moveToPosition(position);
			return cursor.getLong(iId);
		} else {
			return RecyclerView.NO_ID;
		}
	}

	@Override
	public int getItemCount() {
		int size = getCursorItemCount();
		if (isError) {
			size++;
		}
		if (isLoading) {
			size++;
		}
		if (headerView != null) {
			size++;
		}
		if (footerView != null) {
			size++;
		}
		return size;
	}

	public int getCursorItemCount() {
		return cursor == null ? 0 : cursor.getCount();
	}

	protected int getCustomItemViewType(int position, Cursor cursor) {
		return 0;
	}

	protected abstract k createCustomViewHolder(ViewGroup viewGroup, int type);

	protected abstract void bindCustomViewHolder(k viewHolder, int position, Cursor cursor);

	public boolean isHeader() {
		return headerView != null;
	}

	public boolean isFooter() {
		return footerView != null;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public boolean isError() {
		return isError;
	}

	public int getCursorPosition(int position) {
		int size = getCursorItemCount();
		if (headerView != null) {
			position--;
		}
		if (position >= 0 && position < size) {
			return position;
		} else {
			return -1;
		}
	}

	public interface ViewCreator {

		public View createLoadingView(LayoutInflater layoutInflater, ViewGroup parent);

		public View createErrorView(LayoutInflater layoutInflater, ViewGroup parent);
	}

	class EndlessViewHolder extends RecyclerView.ViewHolder {

		public EndlessViewHolder(View itemView) {
			super(itemView);
		}
	}

}
