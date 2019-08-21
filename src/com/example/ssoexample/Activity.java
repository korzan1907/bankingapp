package com.example.ssoexample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ssoexample.number2.R;
import com.l7tech.msso.MobileSso;
import com.l7tech.msso.MobileSsoConfig;
import com.l7tech.msso.MobileSsoFactory;

public class Activity extends FragmentActivity implements
		JsonDownloaderFragment.UserActivity {
	public static final String TAG = "CA Banking App";

	// Explore
	static final String PRODUCT_ENDPOINT = "https://explore.apim.ca:8443/bankaccount?operation=listAccounts";
	static final String FOO_ENDPOINT = "https://explore.apim.ca:8443/authenticate";
	static String lastEP = "";

	static String endpoint = "";
	static final Bundle ssoConf = Config.ssoConf;
	private static final String STATE_PROGRESS_VISIBILITY = "MainActivity.progressVisibility";
	private TextView stateview;
	private ListView listView;
	private ProgressBar progressbar;
	private String password;
	static boolean usedMobileSso = false;

	@Override
	public MobileSso mobileSso() {
		MobileSso mobileSso = MobileSsoFactory.getInstance(this, ssoConf);
		usedMobileSso = true;
		return mobileSso;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.listState);
		progressbar = (ProgressBar) findViewById(R.id.progressbar);
		stateview = (TextView) findViewById(R.id.txtViewKeyChain);

		if (savedInstanceState != null) {
			progressbar.setVisibility(savedInstanceState
					.getInt(STATE_PROGRESS_VISIBILITY));
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		JsonDownloaderFragment httpFragment = (JsonDownloaderFragment) fragmentManager
				.findFragmentByTag("httpResponseFragment");
		if (httpFragment == null) {
			httpFragment = new JsonDownloaderFragment();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(httpFragment, "httpResponseFragment");
			ft.commit();
		}

		final JsonDownloaderFragment finalHttpFragment = httpFragment;

		final Button btnProductList = (Button) findViewById(R.id.btnProductEndPoint);
		btnProductList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listView.setAdapter(null);
				lastEP = PRODUCT_ENDPOINT;
				setJsonDownloadUri(PRODUCT_ENDPOINT);
				finalHttpFragment.downloadJson();
			}
		});
		final Button btnFoo = (Button) findViewById(R.id.btnFooEndpoint);
		btnFoo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listView.setAdapter(null);
				lastEP = FOO_ENDPOINT;
				setJsonDownloadUri(FOO_ENDPOINT);
				finalHttpFragment.downloadJson();
			}
		});
		final Button resetButton = (Button) findViewById(R.id.btnClearStore);
		resetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listView.setAdapter(null);
				mobileSso().destroyAllPersistentTokens();
				stateview.setText("Clear keychain (On Client side only)");
				stateview.append("\n");
			}
		});

		final Button logoutButton = (Button) findViewById(R.id.btnLogout);
		logoutButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				listView.setAdapter(null);
				doServerLogout();
				stateview.setText("Logout (Both Server and Client)");
				stateview.append("\n");
			}
		});

		final Button logoutClientButton = (Button) findViewById(R.id.btnLogoutClient);
		logoutClientButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				listView.setAdapter(null);
				mobileSso().logout(false);
				stateview.setText("Logout (On Client side only)");
				stateview.append("\n");
			}
		});

		final Button unregisterButton = (Button) findViewById(R.id.btnDeregistration);
		unregisterButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				listView.setAdapter(null);
				doServerUnregisterDevice();
				stateview.setText("Remove device registration");
				stateview.append("\n");
			}
		});

		final CheckBox ssoEnableCheckBox = (CheckBox) findViewById(R.id.chkBoxSso);
		ssoEnableCheckBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean checked = ((CheckBox) v).isChecked();
				if (checked) {
					ssoConf.putBoolean(MobileSsoConfig.PROP_SSO_ENABLED, true);
					stateview.append("MSSO is enabled\n");
				} else {
					ssoConf.putBoolean(MobileSsoConfig.PROP_SSO_ENABLED, false);
					stateview.append("MSSO is disabled\n");
				}

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (usedMobileSso)
			mobileSso().processPendingRequests();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_PROGRESS_VISIBILITY, progressbar.getVisibility());
	}

	@Override
	public void showMessage(final String message, final int toastLength) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(Activity.this, message, toastLength).show();
			}
		});
	}

	public void setJsonDownloadUri(String ep) {
		endpoint = ep;
	}

	@Override
	public URI getJsonDownloadUri() {
		return uri(endpoint);
	}

	@Override
	public void setDownloadedJson(String json) {
		try {
			List<Object> objects;

			if (json == null || json.trim().length() < 1) {
				objects = Collections.emptyList();
			} else {
				objects = parseProductListJson(json);
			}
			listView.setAdapter(new ArrayAdapter<Object>(this,
					R.layout.listitem, objects));

		} catch (JSONException e) {
			showMessage("Error: " + e.getMessage(), Toast.LENGTH_LONG);
		}
	}

	@Override
	public void setProgressVisibility(int visibility) {
		progressbar.setVisibility(visibility);

	}

	@Override
	public void showHttpResponseContent(HttpResponse httpResponse) {
		if (httpResponse != null) {
			StatusLine status = httpResponse.getStatusLine();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte bytes[] = new byte[1024];

			InputStream is;
			try {
				is = httpResponse.getEntity().getContent();
				int n = is.read(bytes);

				while (n != -1) {
					out.write(bytes, 0, n);
					n = is.read(bytes);
				}

			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String response = String.format("%s - %s ", status.getStatusCode(),
					status.getReasonPhrase());
			stateview.setText("HTTP RESPONSE : ");
			stateview.append(response);
			stateview.append("\n");
			stateview.append("BODY : ");
			stateview.append("\n");
			stateview.append(out.toString());
			stateview.append("\n");
			stateview.append("\n");
		}

	}

	public void writeToTextView(String text) {
		stateview.setText("");
		stateview.append(text);
	}

	/**
	 * Utility method that parses a URI without throwing a checked exception if
	 * parsing fails.
	 * 
	 * @param uri
	 *            a URI to parse, or null.
	 * @return a parsed URI, or null. Never null if uri is a valid URI.
	 */
	protected final URI uri(String uri) {
		try {
			if (uri != null)
				return new URI(uri);
		} catch (URISyntaxException e) {
			Log.d(TAG, "invalid URI: " + uri, e);
		}
		return null;
	}

	private static List<Object> parseProductListJson(String json)
			throws JSONException {
		try {
			List<Object> objects = new ArrayList<Object>();
			Object isJson = new JSONTokener(json).nextValue();

			if (!(isJson instanceof JSONObject)) {
				objects.add("Error: Not JSON format");
				return objects;
			}

			JSONObject parsed = (JSONObject) isJson;
			boolean isProduct = parsed.has("products");
			if (isProduct) {
				JSONArray items = parsed.getJSONArray("products");
				for (int i = 0; i < items.length(); ++i) {
					JSONObject item = (JSONObject) items.get(i);
					String price = (String) item.get("price");
					String name = (String) item.get("name");
					objects.add(new Pair<String, String>(price, name) {
						@Override
						public String toString() {
							return first + "  " + second;
						}
					});
				}
			} else {
				Iterator<String> keys = parsed.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					objects.add(new Pair<String, String>(key, parsed.get(key)
							.toString()) {
						@Override
						public String toString() {
							return first + "  " + second;
						}
					});
				}
			}

			return objects;
		} catch (ClassCastException e) {
			throw (JSONException) new JSONException(
					"Response JSON was not in the expected format")
					.initCause(e);
		}

	}

	private void doServerLogout() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {

				try {
					mobileSso().logout(true);
					showMessage("Client logout successful", Toast.LENGTH_SHORT);
				} catch (Exception e) {
					String msg = "Client logout failed: " + e.getMessage();
					Log.e(TAG, msg, e);
					showMessage(msg, Toast.LENGTH_LONG);
				}
				return null;
			}

		}.execute((Void) null);
	}

	private void doServerUnregisterDevice() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {

				try {
					mobileSso().removeDeviceRegistration();
					showMessage("Device registration removed from server",
							Toast.LENGTH_LONG);
				} catch (Exception e) {
					String msg = "Device registration removal failed: "
							+ e.getMessage();
					Log.e(TAG, msg, e);
					showMessage(msg, Toast.LENGTH_LONG);
				}

				try {
					mobileSso().destroyAllPersistentTokens();
					showMessage("Persistent tokens removed from device",
							Toast.LENGTH_LONG);
				} catch (Exception e) {
					String msg = "Persistent token removal failed: "
							+ e.getMessage();
					Log.e(TAG, msg, e);
					showMessage(msg, Toast.LENGTH_LONG);
				}

				return null;
			}
		}.execute((Void) null);
	}

//	@Override
//	public String getPassword() {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
