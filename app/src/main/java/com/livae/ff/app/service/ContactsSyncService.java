package com.livae.ff.app.service;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;

public class ContactsSyncService extends Service {

	private static SyncAdapterImpl sSyncAdapter = null;

	@Override
	public IBinder onBind(Intent intent) {
		return getSyncAdapter().getSyncAdapterBinder();
	}

	private SyncAdapterImpl getSyncAdapter() {
		if (sSyncAdapter == null) {
			sSyncAdapter = new SyncAdapterImpl(this);
		}
		return sSyncAdapter;
	}

	private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {

		private Context context;

		public SyncAdapterImpl(Context context) {
			super(context, true);
			this.context = context;
		}

		@Override
		public void onPerformSync(Account account, Bundle extras, String authority,
								  ContentProviderClient provider, SyncResult syncResult) {
			// TODO
		}
	}
}
