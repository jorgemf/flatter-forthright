package com.livae.ff.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ContactsSyncService extends Service {

	private static final Object syncAdapterLock = new Object();

	private static ContactsSyncAdapter syncAdapter = null;

	@Override
	public void onCreate() {
		synchronized (syncAdapterLock) {
			if (syncAdapter == null) {
				syncAdapter = new ContactsSyncAdapter(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return syncAdapter.getSyncAdapterBinder();
	}
}