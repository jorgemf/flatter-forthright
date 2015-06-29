/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.http.okhttp;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.SslUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * Thread-safe HTTP low-level transport based on the {@code java.net} package. <p/> <p> Users should
 * consider modifying the keep alive property on {@link OkHttpTransport} to control whether the
 * socket should be returned to a pool of connected sockets. More information is available <a
 * href='http://docs.oracle.com/javase/7/docs/technotes/guides/net/http-keepalive.html'>here</a>.
 * </p> <p/> <p> We honor the default global caching behavior. To change the default behavior use
 * {@link java.net.HttpURLConnection#setDefaultUseCaches(boolean)}. </p> <p/> <p> Upgrade warning:
 * in prior version 1.14 caching was always disabled, but starting with version 1.15 we honor the
 * default global caching behavior. </p> <p/> <p> Implementation is thread-safe. For maximum
 * efficiency, applications should use a single globally-shared instance of the HTTP transport.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.0
 */
public final class OkHttpTransport extends HttpTransport {

	/**
	 * All valid request methods as specified in {@link java.net.HttpURLConnection#setRequestMethod},
	 * sorted in ascending alphabetical order.
	 */
	private static final String[] SUPPORTED_METHODS = {HttpMethods.DELETE, HttpMethods.GET,
													   HttpMethods.HEAD, HttpMethods.OPTIONS,
													   HttpMethods.POST, HttpMethods.PUT,
													   HttpMethods.TRACE};

	static {
		Arrays.sort(SUPPORTED_METHODS);
	}

	/**
	 * HTTP proxy or {@code null} to use the proxy settings from <a href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
	 * properties</a>.
	 */
	private final Proxy proxy;

	/**
	 * SSL socket factory or {@code null} for the default.
	 */
	private final SSLSocketFactory sslSocketFactory;

	/**
	 * Host name verifier or {@code null} for the default.
	 */
	private final HostnameVerifier hostnameVerifier;

	private final OkHttpClient okHttpClient;

	private final OkUrlFactory okUrlFactory;

	/**
	 * Constructor with the default behavior. <p/> <p> Instead use {@link Builder} to modify
	 * behavior. </p>
	 */
	public OkHttpTransport() {
		this(null, null, null);
	}

	/**
	 * @param proxy
	 *   HTTP proxy or {@code null} to use the proxy settings from <a href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">
	 *   system properties</a>
	 * @param sslSocketFactory
	 *   SSL socket factory or {@code null} for the default
	 * @param hostnameVerifier
	 *   host name verifier or {@code null} for the default
	 */
	OkHttpTransport(Proxy proxy, SSLSocketFactory sslSocketFactory,
					HostnameVerifier hostnameVerifier) {
		this.proxy = proxy;
		this.sslSocketFactory = sslSocketFactory;
		this.hostnameVerifier = hostnameVerifier;
		this.okHttpClient = new OkHttpClient();
		this.okUrlFactory = new OkUrlFactory(this.okHttpClient);
	}

	@Override
	public boolean supportsMethod(String method) {
		return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
	}

	@Override
	protected OkHttpRequest buildRequest(String method, String url) throws IOException {
		Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
		// connection with proxy settings
		URL connUrl = new URL(url);

		if (proxy != null) {
			okHttpClient.setProxy(proxy);
		}

		HttpURLConnection connection = okUrlFactory.open(connUrl);

		connection.setRequestMethod(method);
		// SSL settings
		if (connection instanceof HttpsURLConnection) {
			HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
			if (hostnameVerifier != null) {
				secureConnection.setHostnameVerifier(hostnameVerifier);
			}
			if (sslSocketFactory != null) {
				secureConnection.setSSLSocketFactory(sslSocketFactory);
			}
		}
		return new OkHttpRequest(connection);
	}

	/**
	 * Builder for {@link OkHttpTransport}. <p/> <p> Implementation is not thread-safe. </p>
	 *
	 * @since 1.13
	 */
	public static final class Builder {

		/**
		 * SSL socket factory or {@code null} for the default.
		 */
		private SSLSocketFactory sslSocketFactory;

		/**
		 * Host name verifier or {@code null} for the default.
		 */
		private HostnameVerifier hostnameVerifier;

		/**
		 * HTTP proxy or {@code null} to use the proxy settings from <a
		 * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
		 * properties</a>.
		 */
		private Proxy proxy;

		/**
		 * Sets the HTTP proxy or {@code null} to use the proxy settings from <a
		 * href="http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html">system
		 * properties</a>. <p/> <p> For example: </p>
		 * <p/>
		 * <pre>
		 * setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)))
		 * </pre>
		 */
		public Builder setProxy(Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		/**
		 * Sets the SSL socket factory based on root certificates in a Java KeyStore. <p/> <p>
		 * Example usage: </p>
		 * <p/>
		 * <pre>
		 * trustCertificatesFromJavaKeyStore(new FileInputStream("certs.jks"), "password");
		 * </pre>
		 *
		 * @param keyStoreStream
		 *   input stream to the key store (closed at the end of this method in a finally block)
		 * @param storePass
		 *   password protecting the key store file
		 * @since 1.14
		 */
		public Builder trustCertificatesFromJavaKeyStore(InputStream keyStoreStream,
														 String storePass)
		  throws GeneralSecurityException, IOException {
			KeyStore trustStore = SecurityUtils.getJavaKeyStore();
			SecurityUtils.loadKeyStore(trustStore, keyStoreStream, storePass);
			return trustCertificates(trustStore);
		}

		/**
		 * Sets the SSL socket factory based root certificates generated from the specified stream
		 * using {@link java.security.cert.CertificateFactory#generateCertificates(java.io.InputStream)}.
		 * <p/> <p> Example usage: </p>
		 * <p/>
		 * <pre>
		 * trustCertificatesFromStream(new FileInputStream("certs.pem"));
		 * </pre>
		 *
		 * @param certificateStream
		 *   certificate stream
		 * @since 1.14
		 */
		public Builder trustCertificatesFromStream(InputStream certificateStream)
		  throws GeneralSecurityException, IOException {
			KeyStore trustStore = SecurityUtils.getJavaKeyStore();
			trustStore.load(null, null);
			SecurityUtils.loadKeyStoreFromCertificates(trustStore,
													   SecurityUtils.getX509CertificateFactory(),
													   certificateStream);
			return trustCertificates(trustStore);
		}

		/**
		 * Sets the SSL socket factory based on a root certificate trust store.
		 *
		 * @param trustStore
		 *   certificate trust store (use for example {@link com.google.api.client.util.SecurityUtils#loadKeyStore}
		 *   or {@link com.google.api.client.util.SecurityUtils#loadKeyStoreFromCertificates})
		 * @since 1.14
		 */
		public Builder trustCertificates(KeyStore trustStore) throws GeneralSecurityException {
			SSLContext sslContext = SslUtils.getTlsSslContext();
			SslUtils.initSslContext(sslContext, trustStore, SslUtils.getPkixTrustManagerFactory());
			return setSslSocketFactory(sslContext.getSocketFactory());
		}

		/**
		 * {@link com.google.api.client.util.Beta} <br/> Disables validating server SSL certificates
		 * by setting the SSL socket factory using {@link com.google.api.client.util.SslUtils#trustAllSSLContext()}
		 * for the SSL context and {@link com.google.api.client.util.SslUtils#trustAllHostnameVerifier()}
		 * for the host name verifier. <p/> <p> Be careful! Disabling certificate validation is
		 * dangerous and should only be done in testing environments. </p>
		 */
		@Beta
		public Builder doNotValidateCertificate() throws GeneralSecurityException {
			hostnameVerifier = SslUtils.trustAllHostnameVerifier();
			sslSocketFactory = SslUtils.trustAllSSLContext().getSocketFactory();
			return this;
		}

		/**
		 * Returns the SSL socket factory.
		 */
		public SSLSocketFactory getSslSocketFactory() {
			return sslSocketFactory;
		}

		/**
		 * Sets the SSL socket factory or {@code null} for the default.
		 */
		public Builder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
			this.sslSocketFactory = sslSocketFactory;
			return this;
		}

		/**
		 * Returns the host name verifier or {@code null} for the default.
		 */
		public HostnameVerifier getHostnameVerifier() {
			return hostnameVerifier;
		}

		/**
		 * Sets the host name verifier or {@code null} for the default.
		 */
		public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
			this.hostnameVerifier = hostnameVerifier;
			return this;
		}

		/**
		 * Returns a new instance of {@link OkHttpTransport} based on the options.
		 */
		public OkHttpTransport build() {
			return new OkHttpTransport(proxy, sslSocketFactory, hostnameVerifier);
		}
	}
}