package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EndlessCursorAdapter<k extends RecyclerView.ViewHolder>
  extends RecyclerView.Adapter<k> {

	protected final Context context;

	private final AtomicBoolean keepOnAppending;

	private boolean isErrorView;

	private Cursor cursor;

	public EndlessCursorAdapter(Context context, Cursor cursor, ViewCreator viewCreator) {
		this.context = context;
		keepOnAppending = new AtomicBoolean(true);
		this.appendingView = appendingView;
		this.errorView = errorView;
	}

	@Override
	public void clear() {
		swapCursor(null);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parentView) {
		if (position == super.getCount() && keepOnAppending.get()) {
			if (isErrorView) {
				return errorView;
			} else {
				return this.appendingView;
			}
		}
		return super.getView(position, convertView, parentView);
	}

	public void setAppendingElements(boolean newValue) {
		boolean same = (newValue == keepOnAppending.get());

		keepOnAppending.set(newValue);

		if (!same) {
			notifyDataSetChanged();
		}
	}

	public boolean isOnAppendingElements() {
		return keepOnAppending.get();
	}

	public boolean isErrorView() {
		return isErrorView;
	}

	public void setErrorView(boolean isErrorView) {
		if (this.isErrorView != isErrorView) {
			this.isErrorView = isErrorView;
			notifyDataSetChanged();
		}
	}

	@Override
	public final void onBindViewHolder(k holder, int position) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.moveToPosition(position);
		}
		onBindViewHolder(holder, cursor, position);
	}

	@Override
	public final int getItemViewType(int position) {
		if (position >= super.getCount()) {
			return IGNORE_ITEM_VIEW_TYPE;
		}
		return getItemType(position);
	}

	@Override
	public int getItemCount() {
		// TODO
		int loadingEnd =0;
		if (keepOnAppending.get()) {
			loadingEnd = 1;
		}

		if (cursor != null && !cursor.isClosed()) {
			return cursor.getCount();
		} else {
			return 0;
		}
		return super.getCount();
	}

	public Cursor getCursor() {
		return cursor;
	}

	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == cursor) {
			return null;
		}
		Cursor oldCursor = cursor;
		cursor = newCursor;
		if (cursor != null) {
			findIndexes(cursor);
		}
		return oldCursor;
	}

	public abstract void findIndexes(Cursor cursor);

	public abstract void onBindViewHolder(k holder, Cursor cursor, int position);
}

}