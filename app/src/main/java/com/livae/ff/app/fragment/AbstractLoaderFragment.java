package com.livae.ff.app.fragment;

import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.loading.CursorRecyclerAdapter;
import com.livae.ff.loading.LoadingHelper;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryParam;
import com.livae.ff.app.utils.Debug;

public abstract class AbstractLoaderFragment<VH extends RecyclerView.ViewHolder, QUERY extends QueryParam>
  extends Fragment implements LoadingHelper.LoadListener, LoadingHelper.ErrorViewsCreator,
							  LoaderManager.LoaderCallbacks<Cursor>, Callback<QUERY, ListResult> {

	private static final int LOADER_ID = 1;

	private final String KEY_SAVED_ORDER = "KEY_SAVED_ORDER";

	private final String KEY_SAVED_NEXT_CURSOR = "KEY_SAVED_NEXT_CURSOR";

	private final String KEY_SAVED_TOTAL_LOADED = "KEY_SAVED_TOTAL_LOADED";

	private final String KEY_SAVED_SELECTION = "KEY_SAVED_SELECTION";

	private final String KEY_SAVED_SELECTION_ARGS = "KEY_SAVED_SELECTION_ARGS";

	private View emptyView;

	private LoadingHelper<VH> loadingHelper;

	private CursorRecyclerAdapter<VH> adapter;

	private Order order;

	private String nextCursor;

	private int totalLoaded;

	private NetworkAsyncTask<QUERY, ListResult> loaderTask;

	private boolean loadingInitial;

	private String selection;

	private String[] selectionArgs;

	private RecyclerView recyclerView;

	private LifeCycleListener lifeCycleListener;

	private Menu menu;

	public void setSelection(String selection, String[] selectionArgs) {
		this.selection = selection;
		this.selectionArgs = selectionArgs;
	}

	public void setRecyclerViewTopPadding(final View view) {
		loadingHelper.setHeaderView(createViewWithHeight(view));
	}

	public void setRecyclerViewBottomPadding(final View view) {
		loadingHelper.setFooterView(createViewWithHeight(view));
	}

	public void setColumnsLayoutManager(int columns) {
		loadingHelper.setLayoutManager(new GridLayoutManager(getActivity(), columns,
															 GridLayoutManager.VERTICAL, false));
	}

	public View createViewWithHeight(View view) {
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int height = view.getMeasuredHeight();
		ViewGroup.MarginLayoutParams lp;
		lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
		View viewPadding = new View(getActivity());
		int newHeight = height + lp.topMargin + lp.bottomMargin;
		RecyclerView.LayoutParams rlp;
		rlp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, newHeight);
		viewPadding.setLayoutParams(rlp);
		return viewPadding;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		adapter = getAdapter();
		order = Order.VOTES;
		totalLoaded = 0;
		if (savedInstanceState != null) {
			order = (Order) savedInstanceState.getSerializable(KEY_SAVED_ORDER);
			nextCursor = savedInstanceState.getString(KEY_SAVED_NEXT_CURSOR);
			totalLoaded = savedInstanceState.getInt(KEY_SAVED_TOTAL_LOADED);
			selection = savedInstanceState.getString(KEY_SAVED_SELECTION);
			selectionArgs = savedInstanceState.getStringArray(KEY_SAVED_SELECTION_ARGS);
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
		ContentLoadingProgressBar loading;
		loading = (ContentLoadingProgressBar) view.findViewById(R.id.center_progressbar);
		loadingHelper = new LoadingHelper<>(getActivity(), recyclerView, adapter, this, loading,
											this);
		loadingHelper.enableInitialProgressLoading(true);
		loadingHelper.enableEndlessLoading(true);
		loadingHelper.enablePullToRefreshUpdate(true);
		Resources res = getResources();
		loadingHelper.setColorCircularLoading(res.getColor(R.color.primary));
		loadingHelper.setColorCircularLoadingActive(res.getColor(R.color.accent));
		loadingHelper.endlessLoadingPreloadAhead(res.getInteger(R.integer.default_preload_ahead));
		if (lifeCycleListener != null) {
			lifeCycleListener.onViewCreated(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (lifeCycleListener != null) {
			lifeCycleListener.onResumed(this);
		}
		loaderTask = getLoaderTask();
		loadingHelper.onResume();
		if (totalLoaded == 0 && nextCursor == null) {
			loadingHelper.start();
		} else if (!loadingHelper.isLoading()) {
			reloadCursor();
		}
		if (menu != null) {
			switch (order) {
				case DATE:
					MenuItem orderLatest = menu.findItem(R.id.action_order_latest);
					if (orderLatest != null) {
						orderLatest.setChecked(true);
					}
					break;
				case VOTES:
					MenuItem orderVotes = menu.findItem(R.id.action_order_votes);
					if (orderVotes != null) {
						orderVotes.setChecked(true);
					}
					break;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(KEY_SAVED_ORDER, order);
		outState.putString(KEY_SAVED_NEXT_CURSOR, nextCursor);
		outState.putInt(KEY_SAVED_TOTAL_LOADED, totalLoaded);
		outState.putString(KEY_SAVED_SELECTION, selection);
		outState.putStringArray(KEY_SAVED_SELECTION_ARGS, selectionArgs);
	}

	@Override
	public void onPause() {
		super.onPause();
		loaderTask.cancel();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_order, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		this.menu = menu;
		switch (order) {
			case DATE:
				menu.findItem(R.id.action_order_latest).setChecked(true);
				break;
			case VOTES:
				menu.findItem(R.id.action_order_votes).setChecked(true);
				break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_order_latest:
				item.setChecked(true);
				if (order != Order.DATE) {
					order = Order.DATE;
					restart();
				}
				return true;
			case R.id.action_order_votes:
				item.setChecked(true);
				if (order != Order.VOTES) {
					order = Order.VOTES;
					restart();
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void clear() {
		nextCursor = null;
		totalLoaded = 0;
	}

	public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
		recyclerView.setOnScrollListener(listener);
	}

	protected abstract NetworkAsyncTask<QUERY, ListResult> getLoaderTask();

	protected abstract Uri getUriCursor();

	protected abstract CursorRecyclerAdapter<VH> getAdapter();

	protected abstract String[] getProjection();

	protected abstract QUERY getBaseQueryParams();

	protected abstract String getOrderString(Order order);

	@Override
	public void clearAdapter() {
		adapter.changeCursor(null);
	}

	@Override
	public void loadPrevious() {
		restart();
	}

	@Override
	public void loadNext() {
		QUERY query = getBaseQueryParams();
		query.setOrder(order);
		query.setCursor(nextCursor);
		loaderTask.execute(query, this);
	}

	@Override
	public void loadInitial() {
		loadingHelper.enableEndlessLoading(true);
		if (totalLoaded > 0) {
			LoaderManager lm = getLoaderManager();
			lm.restartLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
					return AbstractLoaderFragment.this.onCreateLoader(LOADER_ID, args);
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
					int previous = adapter.getItemCount();
					adapter.changeCursor(data);
					int difference = data.getCount() - previous;
					loadingHelper.finishLoadingInitial(false, difference, nextCursor != null);
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {
					// nothing
				}
			});
		} else {
			loadingInitial = true;
			QUERY query = getBaseQueryParams();
			query.setOrder(order);
			loaderTask.execute(query, this);
		}
		if (emptyView != null) {
			emptyView.setVisibility(View.GONE);
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
				adapter.changeCursor(data);
				adapter.notifyDataSetChanged();
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
		if (loaderTask == task) {
			if (task.isCancelled()) {
				finishLoadingHelper(false, 0, false);
			} else {
				nextCursor = result.getNextCursor();
				totalLoaded += result.getSize();
				if (result.getSize() == 0) {
					nextCursor = null;
					loadingHelper.enableEndlessLoading(false);
					setEmptyView();
					finishLoadingHelper(false, 0, false);
				} else {
					getLoaderManager().restartLoader(LOADER_ID, null, this);
				}
			}
		}
	}

	@Override
	public void onError(CustomAsyncTask<QUERY, ListResult> task, QUERY param, Exception e) {
		finishLoadingHelper(true, 0, false);
		if (!task.isCancelled()) {
			AbstractActivity activity = (AbstractActivity) getActivity();
			activity.showSnackBarException(e);
		}
	}

	private void finishLoadingHelper(boolean errorView, int added, boolean continueLoading) {
		if (loadingHelper.isLoading()) {
			if (loadingInitial) {
				loadingInitial = false;
				loadingHelper.finishLoadingInitial(errorView, added, continueLoading);
			} else {
				loadingHelper.finishLoadingNext(errorView, added, continueLoading);
			}
		}
	}

	@Override
	public View createTopErrorView(ViewGroup viewGroup) {
		LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		View view = layoutInflater.inflate(R.layout.item_retry, viewGroup, false);
		view.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadingHelper.retryLoadPrevious();
			}
		});
		return view;
	}

	@Override
	public View createBottomErrorView(ViewGroup viewGroup) {
		LayoutInflater layoutInflater = getActivity().getLayoutInflater();
		View view = layoutInflater.inflate(R.layout.item_retry, viewGroup, false);
		view.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadingHelper.retryLoadNext();
			}
		});
		return view;
	}

	@Override
	public boolean hasTopErrorView() {
		return true;
	}

	@Override
	public boolean hasBottomErrorView() {
		return true;
	}

	public void restart() {
		loaderTask.cancel();
		loaderTask = getLoaderTask();
		totalLoaded = 0;
		nextCursor = null;
		loadingInitial = false;
		getLoaderManager().destroyLoader(LOADER_ID);
		loadingHelper.reset();
	}

	protected void setEmptyView() {
		if (emptyView != null) {
			if (adapter.getItemCount() == 0) {
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
				adapter.changeCursor(data);
				adapter.notifyItemRemoved(adapterPosition);
				if (totalLoaded == 0) {
					setEmptyView();
				}
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				// nothing
			}
		});
	}

	public void setLifeCycleListener(LifeCycleListener lifeCycleListener) {
		this.lifeCycleListener = lifeCycleListener;
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

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public interface LifeCycleListener {

		public void onViewCreated(AbstractLoaderFragment fragment);

		public void onResumed(AbstractLoaderFragment fragment);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
			case LOADER_ID:
				return new CursorLoader(getActivity(), getUriCursor(), getProjection(), selection,
										selectionArgs, getOrderString(order) + " LIMIT " +
													   totalLoaded);
			// break
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		Cursor previousCursor = adapter.getCursor();
		int initialElements = 0;
		if (previousCursor != null) {
			initialElements = previousCursor.getCount();
		}
		int newElements = cursor.getCount() - initialElements;
		if (BuildConfig.DEBUG) {
			Debug.print(cursor);
		}
		adapter.changeCursor(cursor);
		finishLoadingHelper(false, newElements, newElements != 0);
		setEmptyView();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> objectLoader) {
		Cursor previousCursor = adapter.getCursor();
		int elements = 0;
		if (previousCursor != null) {
			elements = previousCursor.getCount();
		}
		adapter.changeCursor(null);
		if (elements > 0) {
			adapter.notifyItemRangeRemoved(0, elements);
		}
		totalLoaded = 0;
	}

}
