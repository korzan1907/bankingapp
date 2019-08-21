package com.example.ssoexample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.l7tech.msso.MobileSso;
import com.l7tech.msso.gui.HttpResponseFragment;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Retained fragment that downloads and/or caches a downloaded resource in JSON
 * format.
 * <p/>
 * Activities that wish to make use of this fragment must implement
 * {@link UserActivity}.
 */
public class JsonDownloaderFragment extends HttpResponseFragment {
	public static final int MAX_JSON_SIZE = 100 * 1024;
	private String m_Text = "";
	private String lastDownloadedJson;
	static final String OTP_VERIFY = "https://explore.apim.ca:8443/otp/verify";
	static final String RISK_CHECK = "https://explore.apim.ca:8443/risk/check";
	private static int number = 0;
	public static boolean riskstatus = true;
	public static String username="";
	public static String password="";

	private UserActivity userActivity() {
		return (UserActivity) getActivity();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (lastDownloadedJson != null)
			sendJsonToActivity(lastDownloadedJson);
	}

	void downloadJson() {
		UserActivity activity = (UserActivity) getActivity();
		activity.setProgressVisibility(ProgressBar.VISIBLE);

		final URI uri = activity.getJsonDownloadUri();
		HttpGet httpGet = new HttpGet(uri);
		httpGet.addHeader("username", username);
		httpGet.addHeader("password", password);
		activity.mobileSso().processRequest(httpGet, getResultReceiver());
	}

	void sendJsonToActivity(String json) {
		UserActivity activity = userActivity();
		if (activity != null) {
			activity.setDownloadedJson(json);
		}
	}

	void sendHttpResponseToActivity(HttpResponse httpResponse) {
		UserActivity activity = userActivity();
		if (activity != null) {
			activity.showHttpResponseContent(httpResponse);
		}
	}

	@Override
	protected void onResponse(long requestId, int resultCode,
			String errorMessage, HttpResponse httpResponse) {
		final TextView textView= new TextView(getActivity());
		UserActivity activity = userActivity();
		if (activity != null)
			activity.setProgressVisibility(ProgressBar.GONE);
		if (errorMessage != null) {
			if (activity != null)
				activity.showMessage(errorMessage, Toast.LENGTH_LONG);
		} else {
			lastDownloadedJson = toString(httpResponse, MAX_JSON_SIZE, true);
			String state = "State: ";
			String scoretext = "OTP";
			String successText="successful_verification";
			try {
				if (lastDownloadedJson.toLowerCase().contains(state.toLowerCase())) {
					number = Integer.parseInt(lastDownloadedJson.replace(state,""));
					if (number == 0||number==2) {
						riskstatus = false;
						riskCheck();
					} else if(number==1||number==3){
						riskstatus = true;
						makeCall();
					}
				}
				String riskStatus = jsonParser(lastDownloadedJson,scoretext);
				if (riskStatus.indexOf(scoretext)!=-1) {
					String status=riskStatus.replace(scoretext+" ","");
					if (Boolean.parseBoolean(status.toString())==true) {
						otpVerify();
					}
					else makeCall();
				}
				else if (jsonParser(lastDownloadedJson,successText).indexOf(successText)!=-1) { 
					String status=jsonParser(lastDownloadedJson,successText).replace(successText+" ","");
					boolean verification=Boolean.parseBoolean(status.toString());
					if(verification==true) {
						makeCall();
					}
					else {
						textView.setText("OTP is not verified!");
						errorMessage="OTP is not verified!";
						activity.showMessage(errorMessage, Toast.LENGTH_LONG);
						otpVerify();
					}
				}
				else {
					sendHttpResponseToActivity(httpResponse);
					sendJsonToActivity(lastDownloadedJson);
				}
			} catch (JSONException e) {
				Log.w("JSON Exception", "result code = " + resultCode);
			}
		}

		if (httpResponse != null && riskstatus == true) {
			Log.w("APP A", "result code = " + resultCode);
			sendHttpResponseToActivity(httpResponse);
		}
	}

	private void otpVerify() {
		final EditText input = new EditText(getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());

		builder.setTitle("OTP");
		builder.setMessage("Please enter your OTP:");
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				m_Text = input.getText().toString();
				verifyOTP(m_Text);
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		builder.show();

	}

	private void riskCheck() {
		Activity activity = (Activity) userActivity();
		FragmentManager fragmentManager = ((FragmentActivity) activity)
				.getSupportFragmentManager();
		JsonDownloaderFragment httpFragment = (JsonDownloaderFragment) fragmentManager
				.findFragmentByTag("httpResponseFragment");
		final JsonDownloaderFragment finalHttpFragment = httpFragment;
		activity.setJsonDownloadUri(RISK_CHECK);
		finalHttpFragment.downloadJson();
	}
	private void makeCall() {
		Activity activity = (Activity) userActivity();
		FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
		JsonDownloaderFragment httpFragment = (JsonDownloaderFragment) fragmentManager.findFragmentByTag("httpResponseFragment");
		final JsonDownloaderFragment finalHttpFragment = httpFragment;
		activity.setJsonDownloadUri(Activity.lastEP);
		finalHttpFragment.downloadJson();
	}
	private void verifyOTP(String OTP) {
		Activity activity = (Activity) userActivity();
		FragmentManager fragmentManager = ((FragmentActivity) activity)
				.getSupportFragmentManager();
		JsonDownloaderFragment httpFragment = (JsonDownloaderFragment) fragmentManager
				.findFragmentByTag("httpResponseFragment");
		final JsonDownloaderFragment finalHttpFragment = httpFragment;
		String url = OTP_VERIFY + "?OTP=" + OTP;
		activity.setJsonDownloadUri(url);
		finalHttpFragment.downloadJson();
	}

	private static String jsonParser(String json,String value) throws JSONException {
		try {
			String riskScore = "";
			List<Object> objects = new ArrayList<Object>();
			Object isJson = new JSONTokener(json).nextValue();
			JSONObject parsed = (JSONObject) isJson;
			Iterator<String> keys = parsed.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				if(key.equalsIgnoreCase(value)) return value+" "+parsed.get(key).toString();
			}
			return riskScore;
		} catch (ClassCastException e) {
			throw (JSONException) new JSONException(					"Response JSON was not in the expected format")
					.initCause(e);
		}

	}

	/**
	 * Interface that must be implemented by an Activity that wishes to use this
	 * fragment.
	 */
	public interface UserActivity {
		Context getBaseContext();

		MobileSso mobileSso();

		// MssoContext getMssoContext();

		void setProgressVisibility(int visibility);

		void setDownloadedJson(String json);

		void showHttpResponseContent(HttpResponse httpResponse);

		void showMessage(String errorMessage, final int toastLength);

		URI getJsonDownloadUri();
		
//		String getPassword();

	}
}
