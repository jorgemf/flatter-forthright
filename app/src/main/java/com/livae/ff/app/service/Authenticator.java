package com.livae.ff.app.service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

public class Authenticator extends AbstractAccountAuthenticator {

	public Authenticator(Context context) {
		super(context);
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
		// Editing properties is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse r,
							 String s,
							 String s2,
							 String[] strings,
							 Bundle bundle)
	  throws NetworkErrorException {
		// Don't add additional accounts
		return null;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse r, Account account, Bundle
																							bundle)
	  throws NetworkErrorException {
		// Ignore attempts to confirm credentials
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse r,
							   Account account,
							   String s,
							   Bundle bundle)
	  throws NetworkErrorException {
		// Getting an authentication token is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthTokenLabel(String s) {
		// Getting a label for the auth token is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse r,
									Account account,
									String s,
									Bundle bundle)
	  throws NetworkErrorException {
		// Updating user credentials is not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse r, Account account, String[] strings)
	  throws NetworkErrorException {
		// Checking features for the account is not supported
		throw new UnsupportedOperationException();
	}
}
