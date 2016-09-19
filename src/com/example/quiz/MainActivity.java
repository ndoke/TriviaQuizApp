/**
 * MainActivity.java
 * A Yang
 * Ajay Vijayakumaran Nair
 * Nachiket Doke
 */
package com.example.quiz;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {

	public static final String LOGGING_KEY = "hw3";
	public static final String NO_OF_Q_KEY = "qc";
	public static final String NO_OF_CORRECT_ANS_KEY = "rac";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void btnClicked(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.buttonCreateQ:
			intent = new Intent(this, CreateQuestionActivity.class);
			startActivity(intent);
			break;
		case R.id.buttonDelAllMyQ:
			new AlertDialog.Builder(this).setTitle("Delete Questions")
					.setMessage("Are you sure you want to delete all your questions?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            new DeleteAllMyQTask().execute();
				        }
				     })
				    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				            // do nothing
				        }
				     })
					.show();
			break;
		case R.id.buttonExit:
			finish();
			break;
		case R.id.buttonStartTrivia:
			intent = new Intent(this, TriviaActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}

	private class DeleteAllMyQTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialogDelMyQ;

		@Override
		protected void onPreExecute() {
			progressDialogDelMyQ = new ProgressDialog(MainActivity.this);
			progressDialogDelMyQ.setTitle("Deleting Questions");
			progressDialogDelMyQ.setMessage("Deleting...");
			progressDialogDelMyQ.setCancelable(false);
			progressDialogDelMyQ.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialogDelMyQ.dismiss();
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(Void... params) {
			URL url;
			Map<String, String> urlParams = new HashMap<String, String>();
			OutputStreamWriter osw = null;
			urlParams.put("gid", MainActivity.this.getResources().getString(R.string.gid));
			try {
				url = new URL(MainActivity.this.getResources().getString(R.string.baseUrl) + "deleteAll.php");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.connect();
				osw = new OutputStreamWriter(connection.getOutputStream());
				osw.write(getEncodedParams(urlParams));
				osw.flush();
				// connection.connect();
				int statusCode = connection.getResponseCode();
				if (statusCode == HttpURLConnection.HTTP_OK) {
					Log.d(MainActivity.LOGGING_KEY, "Http response was OK");
				} else {
					Log.d(MainActivity.LOGGING_KEY, "Http response wasn't OK");
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (osw != null) {
					try {
						osw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return null;
		}

		public String getEncodedParams(Map<String, String> params) throws UnsupportedEncodingException {
			StringBuilder encodedParam = new StringBuilder();
			for (String key : params.keySet()) {
				String encodedValue = URLEncoder.encode(params.get(key), "UTF-8");
				if (encodedParam.length() > 0) {
					encodedParam.append("&");
				}
				encodedParam.append(key + "=" + encodedValue);
			}
			return encodedParam.toString();
		}
	}
}
