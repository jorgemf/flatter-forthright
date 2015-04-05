package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.annotation.Nonnull;

public abstract class EndlessCursorAdapter<k extends RecyclerView.ViewHolder>
  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_ERROR = -2;

	private static final int TYPE_LOADING = -3;

	protected LayoutInflater layoutInflater;

	private boolean isLoading;

	private boolean isError;

	private Cursor cursor;

	private ViewCreator viewCreator;

	public EndlessCursorAdapter(@Nonnull Context context, @Nonnull ViewCreator viewCreator) {
		this.layoutInflater = LayoutInflater.from(context);
		isLoading = false;
		isError = false;
		this.viewCreator = viewCreator;
	}

	public void setCursor(Cursor cursor) {
		if (cursor != this.cursor) {
			if (cursor != null) {
				findIndexes(cursor);
			}
			int currentSize = this.cursor == null ? 0 : this.cursor.getCount();
			int newSize = cursor == null ? 0 : cursor.getCount();
			this.cursor = cursor;
			int diff = newSize - currentSize;
			if (diff > 0) {
				notifyItemRangeInserted(currentSize, diff);
			} else if (diff < 0) {
				notifyItemRangeRemoved(0, -diff);
			}
			setIsLoading(false);
			setIsError(false);
		}
	}

	protected abstract void findIndexes(@Nonnull Cursor cursor);

	public void setIsLoading(boolean loading) {
		if (loading != isLoading) {
			int size = cursor == null ? 0 : cursor.getCount();
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
			int size = cursor == null ? 0 : cursor.getCount();
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
				return new EndlessViewHolder(viewCreator.createLoadingView(layoutInflater));
//			break;
			case TYPE_ERROR:
				return new EndlessViewHolder(viewCreator.createErrorView(layoutInflater));
//			break;
			default:
				return createCustomViewHolder(viewGroup, type);
		}
//		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		int size = cursor == null ? 0 : cursor.getCount();
		if (position < size) {
			//noinspection ConstantConditions
			cursor.moveToPosition(position);
			//noinspection unchecked
			bindCustomViewHolder((k) viewHolder, position, cursor);
		}
	}

	public int getItemViewType(int position) {
		int size = cursor == null ? 0 : cursor.getCount();
		if (size == position) {
			if (isError) {
				return TYPE_ERROR;
			} else {
				return TYPE_LOADING;
			}
		} else if (size > position) {
			return TYPE_LOADING;
		}
		return getCustomItemViewType(position);
	}

	@Override
	public int getItemCount() {
		int size = cursor == null ? 0 : cursor.getCount();
		if (isError) {
			size++;
		}
		if (isLoading) {
			size++;
		}
		return size;
	}

	protected int getCustomItemViewType(int position) {
		return 0;
	}

	protected abstract k createCustomViewHolder(ViewGroup viewGroup, int type);

	protected abstract void bindCustomViewHolder(k viewHolder, int position, Cursor cursor);

	interface ViewCreator {

		public View createLoadingView(LayoutInflater layoutInflater);

		public View createErrorView(LayoutInflater layoutInflater);
	}

	class EndlessViewHolder extends RecyclerView.ViewHolder {

		public EndlessViewHolder(View itemView) {
			super(itemView);
		}
	}

}