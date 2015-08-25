package com.livae.ff.app.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import javax.annotation.Nonnull;

public abstract class CursorAdapter<VH extends RecyclerView.ViewHolder>
  extends RecyclerView.Adapter<VH> {

	protected LayoutInflater layoutInflater;

	private Cursor cursor;

	private int rowIDColumn;

	public CursorAdapter(@Nonnull Context context) {
		this.layoutInflater = LayoutInflater.from(context);
		setHasStableIds(true);
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == cursor) {
			return null;
		}
		Cursor oldCursor = cursor;
		cursor = newCursor;
		if (newCursor != null) {
			rowIDColumn = newCursor.getColumnIndexOrThrow(BaseColumns._ID);
			findIndexes(newCursor);
		} else {
			rowIDColumn = -1;
		}
		notifyDataSetChanged();
		return oldCursor;
	}

	@Override
	public void onBindViewHolder(VH holder, int position) {
		cursor.moveToPosition(position);
		bindCustomViewHolder(holder, position, cursor);
	}

	@Override
	public long getItemId(int position) {
		if (hasStableIds() && cursor != null && !cursor.isClosed()) {
			if (cursor.moveToPosition(position)) {
				return cursor.getLong(rowIDColumn);
			}
		}
		return RecyclerView.NO_ID;
	}

	@Override
	public int getItemCount() {
		if (cursor != null && !cursor.isClosed()) {
			return cursor.getCount();
		} else {
			return 0;
		}
	}

	protected abstract void bindCustomViewHolder(@NonNull VH holder,
												 int position,
												 @NonNull Cursor cursor);

	protected abstract void findIndexes(@NonNull Cursor cursor);

	protected Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

}
