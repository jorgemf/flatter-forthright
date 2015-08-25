package com.livae.ff.app.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryParam;
import com.livae.ff.app.ui.activity.AbstractActivity;
import com.livae.ff.app.ui.adapter.CursorAdapter;
import com.livae.ff.app.ui.adapter.HeaderFooterAdapter;
import com.livae.ff.app.utils.Debug;

public abstract class AbstractEndlessCursorLoaderFragment<O> extends AbstractFragment
  implements LoaderManager.LoaderCallbacks<Cursor>, HeaderFooterAdapter.FooterCreator {

	protected static final int LOADER_ITEMS = 1;

	protected static final int LOADER_ITEM = 2;

	private static final String SAVED_TOTAL_LOADED = "SAVED_TOTAL_LOADED";

	private static final String SAVED_CAN_LOAD_MORE = "SAVED_CAN_LOAD_MORE";

	private static final String SAVED_CURSOR = "SAVED_CURSOR";

	private static final String SAVED_IS_LOADING = "SAVED_IS_LOADING";

	private static final String SAVED_IS_ERROR = "SAVED_IS_ERROR";

	private static final String SAVED_ERROR_MESSAGE = "SAVED_ERROR_MESSAGE";

	protected RecyclerView recyclerView;

	private int totalLoaded;

	private String cursor;

	private boolean canLoadMore;

	private boolean isError;

	private boolean isLoading;

	private HeaderFooterAdapter<CursorAdapter<? extends RecyclerView.ViewHolder>> wrapperAdapter;

	private CursorAdapter<? extends RecyclerView.ViewHolder> dataAdapter;

	private RecyclerView.OnScrollListener onScrollListener;

	private int preloadAhead;

	private int pageSize;

	private View loadingCenter;

	private View error;

	private View loading;

	private View emptyView;

	private String errorMessage;

	private CustomAsyncTask<AbstractFragment, QueryParam, ListResult> loaderTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			totalLoaded = savedInstanceState.getInt(SAVED_TOTAL_LOADED);
			isError = savedInstanceState.getBoolean(SAVED_IS_ERROR);
			isLoading = savedInstanceState.getBoolean(SAVED_IS_LOADING);
			errorMessage = savedInstanceState.getString(SAVED_ERROR_MESSAGE);
			canLoadMore = savedInstanceState.getBoolean(SAVED_CAN_LOAD_MORE);
			cursor = savedInstanceState.getString(SAVED_CURSOR);
			hideMainLoading();
		} else {
			totalLoaded = 0;
			isError = false;
			isLoading = false;
			errorMessage = null;
			canLoadMore = true;
		}
		Resources resources = getResources();
		preloadAhead = resources.getInteger(R.integer.default_preload_ahead);
		pageSize = resources.getInteger(R.integer.default_load_size);
		loaderTask = getLoaderTask();
	}

	protected void startLoading() {
		if (totalLoaded == 0 && !isLoading && canLoadMore) {
			loadNext();
		}else{
			reloadCursor();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container,
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
		recyclerView.setLayoutManager(layoutManager);
		dataAdapter = getAdapter();
		wrapperAdapter =
		  new HeaderFooterAdapter<CursorAdapter<? extends RecyclerView.ViewHolder>>(dataAdapter,
																					null, this);
		recyclerView.setAdapter(wrapperAdapter);
		loadingCenter = view.findViewById(R.id.center_progressbar);
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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SAVED_TOTAL_LOADED, totalLoaded);
		outState.putBoolean(SAVED_IS_ERROR, isError);
		outState.putBoolean(SAVED_IS_LOADING, isLoading);
		outState.putString(SAVED_ERROR_MESSAGE, errorMessage);
		outState.putBoolean(SAVED_CAN_LOAD_MORE, canLoadMore);
	}

	private void checkLoadNext() {
		if (canLoadMore && !isLoading && !isError && dataAdapter.getItemCount() > 0) {
			LinearLayoutManager layoutManager;
			layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
			int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
			if (lastVisiblePosition + preloadAhead >= wrapperAdapter.getItemCount()) {
				loadNext();
			}
		}
	}

	private void clear() {
		totalLoaded = 0;
		dataAdapter.setCursor(null);
		isLoading = false;
		isError = false;
		canLoadMore = true;
		cursor = null;
		hideLoading();
		hideError();
		hideMainLoading();
	}

	public void restart() {
		if (isLoading) {
			loaderTask.cancel();
		}
		getLoaderManager().destroyLoader(LOADER_ITEMS);
		clear();
		showMainLoading();
		loadNext();
	}

	protected abstract Uri getUriCursor();

	protected abstract CursorAdapter<? extends RecyclerView.ViewHolder> getAdapter();

	protected abstract String[] getProjection();

	protected abstract String getSelect();

	protected abstract String[] getSelectArgs();

	protected abstract String getOrderString();

	protected abstract CustomAsyncTask<AbstractFragment, QueryParam, ListResult> getLoaderTask();

	protected abstract QueryParam getBaseQueryParams();

	private void loadNext() {
		if (canLoadMore) {
			if (emptyView != null) {
				emptyView.setVisibility(View.GONE);
			}
			hideError();
			if (totalLoaded > 0) {
				showLoading();
			} else {
				showMainLoading();
			}
			isError = false;
			errorMessage = null;
			isLoading = true;
			QueryParam queryParam = getBaseQueryParams();
			queryParam.setCursor(cursor);
			queryParam.setLimit(pageSize);
			loaderTask.execute(queryParam, new ApiCallCallback());
		}
	}

	public void reloadCursor() {
		LoaderManager lm = getLoaderManager();
		lm.restartLoader(LOADER_ITEMS, null, new LoaderManager.LoaderCallbacks<Cursor>() {

			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				return AbstractEndlessCursorLoaderFragment.this.onCreateLoader(LOADER_ITEMS, args);
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				Debug.print(data);
				dataAdapter.setCursor(data);
				setEmptyView();
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				// nothing
			}
		});
	}

	protected void setEmptyViewText(@StringRes int stringRes) {
		if (emptyView != null && emptyView instanceof TextView) {
			((TextView) emptyView).setText(stringRes);
		}
	}

	protected void setEmptyView() {
		hideMainLoading();
		if (emptyView != null) {
			if (dataAdapter.getItemCount() == 0) {
				emptyView.setVisibility(View.VISIBLE);
			} else {
				emptyView.setVisibility(View.GONE);
			}
		}
	}

	private void showMainLoading() {
		if (loadingCenter.getVisibility() == View.GONE) {
			loadingCenter.animate().alpha(1).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					loadingCenter.setVisibility(View.VISIBLE);
				}

			}).start();
		}
	}

	private void hideMainLoading() {
		if (loadingCenter.getVisibility() == View.VISIBLE) {
			loadingCenter.animate().alpha(0).setListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					loadingCenter.setVisibility(View.GONE);
				}
			}).start();
		}
	}

	private void showLoading() {
		if (loading != null && totalLoaded > 0) {
			loading.animate()
				   .alpha(1)
				   .scaleY(1)
				   .scaleX(1)
				   .setListener(new AnimatorListenerAdapter() {
					   @Override
					   public void onAnimationStart(Animator animation) {
						   loading.setVisibility(View.VISIBLE);
					   }

				   })
				   .start();
		}
	}

	private void hideLoading() {
		if (loading != null) {
			loading.animate()
				   .alpha(0)
				   .scaleY(0)
				   .scaleX(0)
				   .setListener(new AnimatorListenerAdapter() {
					   @Override
					   public void onAnimationEnd(Animator animation) {
						   loading.setVisibility(View.GONE);
					   }

				   })
				   .start();
		}
	}

	private void showError(String errorMessage) {
		if (loading != null) {
			loading.setVisibility(View.GONE);
		}
		if (error != null) {
			error.setVisibility(View.VISIBLE);
			TextView textView = (TextView) error.findViewById(R.id.retry_message);
			textView.setError(errorMessage);
			error.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isError) {
						loadNext();
					}
				}
			});
		}
	}

	private void hideError() {
		if (error != null) {
			error.setVisibility(View.GONE);
		}
	}

	public int getTotalLoaded() {
		return totalLoaded;
	}

	public void setTotalLoaded(int totalLoaded) {
		this.totalLoaded = totalLoaded;
	}

	protected void scrollToPosition(int position, boolean smooth) {
		if (recyclerView != null) {
			if (smooth) {
				recyclerView.smoothScrollToPosition(position);
			} else {
				recyclerView.scrollToPosition(position);
			}
		}
	}

	@NonNull
	@Override
	public View createFooter(ViewGroup parent) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.item_footer, parent, false);
		loading = view.findViewById(R.id.item_loading);
		error = view.findViewById(R.id.item_retry);
		if (!isLoading || totalLoaded == 0 || loadingCenter.getVisibility() == View.VISIBLE) {
			loading.setVisibility(View.GONE);
		}
		if (isError) {
			showError(errorMessage);
		} else {
			error.setVisibility(View.GONE);
		}
		return view;
	}

	/**
	 * The callback is implemented in a different class to avoid any reference to this fragment, so
	 * there is no memory leak anywhere.
	 */
	class ApiCallCallback implements Callback<AbstractFragment, QueryParam, ListResult> {

		@Override
		public void onComplete(@NonNull AbstractFragment ab,
							   QueryParam query,
							   ListResult listResult) {
			AbstractEndlessCursorLoaderFragment f = (AbstractEndlessCursorLoaderFragment) ab;
			f.totalLoaded += listResult.getSize();
			f.cursor = listResult.getNextCursor();
			f.canLoadMore = listResult.getSize() == query.getLimit() && f.cursor != null;
			f.getLoaderManager().restartLoader(LOADER_ITEMS, null, f);
		}

		@Override
		public void onError(@NonNull AbstractFragment ab, QueryParam query, @NonNull Exception e) {
			AbstractEndlessCursorLoaderFragment f = (AbstractEndlessCursorLoaderFragment) ab;
			f.showError(AbstractActivity.getExceptionError(f.getActivity(), e));
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
			case LOADER_ITEMS:
				Uri uri = getUriCursor();
				if (uri != null) {
					return new CursorLoader(getActivity(), getUriCursor(), getProjection(),
											getSelect(), getSelectArgs(),
											getOrderString() + " LIMIT " +
											totalLoaded);
				}
				// break
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		switch (objectLoader.getId()) {
			case LOADER_ITEMS:
				Debug.print(cursor);
				dataAdapter.setCursor(cursor);
				setEmptyView();
				hideLoading();
				isLoading = false;
				checkLoadNext();
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> objectLoader) {
		// nothing
	}

}
