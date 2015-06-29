package com.livae.ff.app.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import javax.annotation.Nonnull;

public abstract class CursorAdapter<k extends RecyclerView.ViewHolder>
  extends RecyclerView.Adapter<k> {

	protected LayoutInflater layoutInflater;

	private Cursor cursor;

	private int iId;

	public CursorAdapter(@Nonnull Context context) {
		this.layoutInflater = LayoutInflater.from(context);
		setHasStableIds(true);
	}

	public void setCursor(Cursor cursor) {
		if (cursor != null) {
			iId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
			findIndexes(cursor);
		}
		if (this.cursor != null && this.cursor != cursor) {
			this.cursor.close();
		}
		this.cursor = cursor;
	}

	protected abstract void findIndexes(@Nonnull Cursor cursor);

	@Override
	public void onBindViewHolder(k viewHolder, int position) {
		int size = getItemCount();
		if (position >= 0 && position < size) {
			cursor.moveToPosition(position);
			bindCustomViewHolder( viewHolder, position, cursor);
		}
	}

	protected abstract void bindCustomViewHolder(k viewHolder, int position, Cursor cursor);

	@Override
	public long getItemId(int position) {
		int size = getItemCount();
		if (position >= 0 && position < size) {
			cursor.moveToPosition(position);
			return cursor.getLong(iId);
		} else {
			return RecyclerView.NO_ID;
		}
	}

	@Override
	public int getItemCount() {
		return cursor == null ? 0 : cursor.getCount();
	}

}