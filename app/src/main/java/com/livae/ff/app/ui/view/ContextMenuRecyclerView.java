package com.livae.ff.app.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

public class ContextMenuRecyclerView extends RecyclerView {

	private RecyclerContextMenuInfo contextMenuInfo;

	public ContextMenuRecyclerView(Context context) {
		super(context);
	}

	public ContextMenuRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ContextMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
		return contextMenuInfo;
	}

	@Override
	public boolean showContextMenuForChild(View originalView) {
		ViewHolder viewHolder = getChildViewHolder(originalView);
		contextMenuInfo = new RecyclerContextMenuInfo(viewHolder);
		return super.showContextMenuForChild(originalView);
	}

	public static class RecyclerContextMenuInfo implements ContextMenu.ContextMenuInfo {

		public final RecyclerView.ViewHolder viewHolder;

		public RecyclerContextMenuInfo(RecyclerView.ViewHolder viewHolder) {
			this.viewHolder = viewHolder;
		}
	}
}
