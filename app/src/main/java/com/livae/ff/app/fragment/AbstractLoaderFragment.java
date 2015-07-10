package com.livae.ff.app.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryParam;
import com.livae.ff.app.utils.Debug;

public abstract class AbstractLoaderFragment<VH extends RecyclerView.ViewHolder, QUERY extends QueryParam>
  extends AbstractFragment
  implements LoaderManager.LoaderCallbacks<Cursor>, Callback<QUERY, ListResult> {

	protected static final int LOADER_ID = 1;

	private final String KEY_SAVED_NEXT_CURSOR = "KEY_SAVED_NEXT_CURSOR";

	private final String KEY_SAVED_TOTAL_LOADED = "KEY_SAVED_TOTAL_LOADED";

	private final String KEY_SAVED_SELECTION = "KEY_SAVED_SELECTION";

	private final String KEY_SAVED_SELECTION_ARGS = "KEY_SAVED_SELECTION_ARGS";

	private final String KEY_SAVED_FINISH_LOADING = "KEY_SAVED_FINISH_LOADING";

	private View emptyView;

	private EndlessCursorAdapter<VH> adapter;

	private String nextCursor;

	private int totalLoaded;

	private NetworkAsyncTask<QUERY, ListResult> loaderTask;

	private String selection;

	private String[] selectionArgs;

	private RecyclerView recyclerView;

	private RecyclerView.OnScrollListener onScrollListener;

	private int preloadAhead;

	private ProgressBar loading;

	private boolean finishLoading;

	private View topPaddingView;

	private View bottomPaddingView;

	public void setSelection(String selection, String[] selectionArgs) {
		this.selection = selection;
		this.selectionArgs = selectionArgs;
	}

	public void setRecyclerViewTopPadding(final View view) {
		if (topPaddingView == null) {
			topPaddingView = createViewWithHeight(view);
			adapter.setHeaderView(topPaddingView);
		} else {
			setSameHeight(view, topPaddingView);
		}
	}

	public void setRecyclerViewBottomPadding(final View view) {
		if (bottomPaddingView == null) {
			bottomPaddingView = createViewWithHeight(view);
			adapter.setFooterView(bottomPaddingView);
		} else {
			setSameHeight(view, bottomPaddingView);
		}
	}

	public void setColumnsLayoutManager(int columns) {
		GridLayoutManager gridLayoutManager;
		gridLayoutManager = new GridLayoutManager(getActivity(), columns,
												  GridLayoutManager.VERTICAL, false);
		gridLayoutManager.setSpanSizeLookup(new GridSpanSize(gridLayoutManager));
		recyclerView.setLayoutManager(gridLayoutManager);
	}

	private View createViewWithHeight(View view) {
		View viewPadding = new View(getActivity());
		setSameHeight(view, viewPadding);
		return viewPadding;
	}

	private void setSameHeight(View view, View viewPadding) {
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int height = view.getMeasuredHeight();
		ViewGroup.MarginLayoutParams lp;
		lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
		int newHeight = height + lp.topMargin + lp.bottomMargin;
		RecyclerView.LayoutParams rlp;
		rlp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, newHeight);
		viewPadding.setLayoutParams(rlp);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		adapter = getAdapter();
		totalLoaded = 0;
		finishLoading = false;
		if (savedInstanceState != null) {
			nextCursor = savedInstanceState.getString(KEY_SAVED_NEXT_CURSOR);
			totalLoaded = savedInstanceState.getInt(KEY_SAVED_TOTAL_LOADED);
			selection = savedInstanceState.getString(KEY_SAVED_SELECTION);
			selectionArgs = savedInstanceState.getStringArray(KEY_SAVED_SELECTION_ARGS);
			finishLoading = savedInstanceState.getBoolean(KEY_SAVED_FINISH_LOADING);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_items, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		emptyView = view.findViewById(R.id.empty_view);
		if (emptyView != null) {
			emptyView.setVisibility(View.GONE);
		}
		recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
		layoutManager.setReverseLayout(true);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(adapter);
		loading = (ProgressBar) view.findViewById(R.id.center_progressbar);
		Resources res = getResources();
		preloadAhead = res.getInteger(R.integer.default_preload_ahead);
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (onScrollListener != null) {
					onScrollListener.onScrollStateChanged(recyclerView, newState);
				}
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (onScrollListener != null) {
					onScrollListener.onScrolled(recyclerView, dx, dy);
				}
				checkLoadNext();
			}
		});
		// fix bug in recycler library when removing the last element
		View viewFooterPadding = new View(getActivity());
		RecyclerView.LayoutParams rlp;
		int height = 1;
		rlp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
		viewFooterPadding.setLayoutParams(rlp);
		adapter.setFooterView(viewFooterPadding);
		// fix auto scroll to the bottom
		View viewHeaderPadding = new View(getActivity());
		height = 1;
		rlp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
		viewHeaderPadding.setLayoutParams(rlp);
		adapter.setHeaderView(viewHeaderPadding);
	}

	@Override
	public void onResume() {
		super.onResume();
		loaderTask = getLoaderTask();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_SAVED_NEXT_CURSOR, nextCursor);
		outState.putInt(KEY_SAVED_TOTAL_LOADED, totalLoaded);
		outState.putString(KEY_SAVED_SELECTION, selection);
		outState.putStringArray(KEY_SAVED_SELECTION_ARGS, selectionArgs);
		outState.putBoolean(KEY_SAVED_FINISH_LOADING, finishLoading);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (loaderTask != null) {
			loaderTask.cancel();
		}
		adapter.setIsLoading(false);
		finishLoading = false;
	}

	protected void startLoading() {
		if (totalLoaded == 0 && nextCursor == null) {
			loadNext();
		} else {
			hideLoading();
			reloadCursor();
		}
	}

	private void checkLoadNext() {
		if (!finishLoading) {
			LinearLayoutManager layoutManager;
			layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
			int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
			if (lastVisiblePosition + preloadAhead >= adapter.getItemCount()) {
				if (!adapter.isLoading() && !adapter.isError() &&
					adapter.getCursorItemCount() > 0) {
					loadNext();
				}
			}
		}
	}

	public void clear() {
		nextCursor = null;
		totalLoaded = 0;
		finishLoading = false;
		adapter.setCursor(null);
	}

	public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
		onScrollListener = listener;
	}

	protected abstract NetworkAsyncTask<QUERY, ListResult> getLoaderTask();

	protected abstract Uri getUriCursor();

	protected abstract EndlessCursorAdapter<VH> getAdapter();

	protected abstract String[] getProjection();

	protected abstract QUERY getBaseQueryParams();

	protected abstract String getOrderString();

	public void loadNext() {
		QUERY query = getBaseQueryParams();
		query.setCursor(nextCursor);
		loaderTask.execute(query, this);
		if (emptyView != null) {
			emptyView.setVisibility(View.GONE);
		}
		if (totalLoaded > 0) {
			adapter.setIsLoading(true);
		} else {
			adapter.setIsError(false);
			showLoading();
		}
	}

	public void reloadCursor() {
		LoaderManager lm = getLoaderManager();
		lm.restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return AbstractLoaderFragment.this.onCreateLoader(LOADER_ID, args);
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				adapter.setCursor(data);
				if (BuildConfig.DEBUG) {
					Debug.print(data);
				}
				setEmptyView();
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				// nothing
			}
		});
	}

	@Override
	public void onComplete(CustomAsyncTask<QUERY, ListResult> task, QUERY param,
						   ListResult result) {
		if (!task.isCancelled() && loaderTask == task) {
			nextCursor = result.getNextCursor();
			totalLoaded += result.getSize();
			finishLoading = result.getSize() == 0;
			getLoaderManager().restartLoader(LOADER_ID, null, this);
		}
	}

	@Override
	public void onError(CustomAsyncTask<QUERY, ListResult> task, QUERY param, Exception e) {
		if (!task.isCancelled()) {
			AbstractActivity activity = (AbstractActivity) getActivity();
			activity.showSnackBarException(e);
			adapter.setIsError(true);
			hideLoading();
		}
	}

	public View createErrorView(LayoutInflater layoutInflater, ViewGroup parent) {
		View view = super.createErrorView(layoutInflater, parent);
		view.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadNext();
			}
		});
		return view;
	}

	public void restart() {
		loaderTask.cancel();
		loaderTask = getLoaderTask();
		totalLoaded = 0;
		nextCursor = null;
		finishLoading = false;
		getLoaderManager().destroyLoader(LOADER_ID);
		adapter.setCursor(null);
		adapter.notifyDataSetChanged();
		showLoading();
		loadNext();
	}

	protected void setEmptyViewText(@StringRes int stringRes) {
		if (emptyView != null && emptyView instanceof TextView) {
			((TextView) emptyView).setText(stringRes);
		}
	}

	protected void setEmptyView() {
		hideLoading();
		if (emptyView != null) {
			if (adapter.getCursorItemCount() == 0) {
				emptyView.setVisibility(View.VISIBLE);
			} else {
				emptyView.setVisibility(View.GONE);
			}
		}
	}

	protected void removeItem(final int adapterPosition) {
		LoaderManager lm = getLoaderManager();
		lm.restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				totalLoaded--;
				return AbstractLoaderFragment.this.onCreateLoader(LOADER_ID, args);
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				adapter.setCursor(data);
				adapter.notifyDataSetChanged();
				setEmptyView();
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				// nothing
			}
		});
	}

	public int getFirstItemTranslationY() {
		LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
		if (layoutManager.findFirstVisibleItemPosition() == 0) {
			View firstView = layoutManager.findViewByPosition(0);
			if (firstView != null) {
				return (int) firstView.getY();
			}
		}
		return 1;
	}

	private void showLoading() {
		if (loading.getVisibility() == View.GONE) {
			loading.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					loading.setVisibility(View.VISIBLE);
				}

			}).start();
		}
	}

	private void hideLoading() {
		if (loading.getVisibility() == View.VISIBLE) {
			loading.animate().alpha(0).setListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					loading.setVisibility(View.GONE);
				}
			}).start();
		}
	}

	public int getTotalLoaded() {
		return totalLoaded;
	}

	public void increaseTotalLoaded() {
		totalLoaded++;
	}

	public class GridSpanSize extends GridLayoutManager.SpanSizeLookup {

		private GridLayoutManager.SpanSizeLookup mSpanSizeLookUpWrapped;

		private int spanCount;

		public GridSpanSize(GridLayoutManager gridLayoutManager) {
			mSpanSizeLookUpWrapped = gridLayoutManager.getSpanSizeLookup();
			spanCount = gridLayoutManager.getSpanCount();
		}

		@Override
		public int getSpanSize(int position) {
			if (adapter.isHeader()) {
				position -= 1;
			}
			if (position < 0 || position >= adapter.getCursorItemCount()) {
				return spanCount;
			} else {
				return mSpanSizeLookUpWrapped.getSpanSize(position);
			}
		}

		@Override
		public int getSpanIndex(int position, int spanCount) {
			if (adapter.isHeader()) {
				position -= 1;
			}
			if (position < 0 || position >= adapter.getCursorItemCount()) {
				return 0;
			} else {
				return position % spanCount;
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
			case LOADER_ID:
				return new CursorLoader(getActivity(), getUriCursor(), getProjection(), selection,
										selectionArgs, getOrderString() + " LIMIT " +
													   totalLoaded);
			// break
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		switch (objectLoader.getId()) {
			case LOADER_ID:
				if (BuildConfig.DEBUG) {
					Debug.print(cursor);
				}
				adapter.setCursor(cursor);
				setEmptyView();
				checkLoadNext();
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> objectLoader) {
		// nothing
	}

}
