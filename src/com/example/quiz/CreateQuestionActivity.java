/**
 * CreateQuestionActivity.java
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CreateQuestionActivity extends Activity {

	private List<String> options = new ArrayList<String>();
	private AsyncTask<String, Void, Void> postQTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_question);
	}

	public void addOptionClicked(View view) {
		EditText optionET = ((EditText) findViewById(R.id.editTextA));
		String optionText = optionET.getText().toString();
		if (optionText.isEmpty()) {
			return;
		}
		options.add(optionText);
		optionET.setText("");
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		RadioButton radioButton = new RadioButton(this);
		radioButton.setText(optionText);
		radioButton.setId(options.size() - 1);
		radioGroup.addView(radioButton);
	}

	public void submitBtnClicked(View view) {
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		if (radioGroup.getChildCount() < 2) {
			Toast.makeText(this, "Atleast two options required", Toast.LENGTH_LONG).show();
			return;
		}
		int indexOfCorrectAnswer = radioGroup.getCheckedRadioButtonId();
		if (indexOfCorrectAnswer == -1) {
			Toast.makeText(this, "You haven't marked the right answer", Toast.LENGTH_LONG).show();
			return;
		}
		EditText questionET = ((EditText) findViewById(R.id.editTextQ));
		if (questionET.getText().toString().isEmpty()) {
			Toast.makeText(this, "Question cannot be blank", Toast.LENGTH_LONG).show();
			return;
		}
		String questionText = questionET.getText().toString();
		String url = ((EditText) findViewById(R.id.editTextUrl)).getText().toString();
		StringBuilder question = new StringBuilder();
		question.append(questionText).append(";").append(TextUtils.join(";", options)).append(";").append(url)
				.append(";").append(indexOfCorrectAnswer);
		Log.d(MainActivity.LOGGING_KEY, "question string : " + question);
		postQTask = new PostQuestionTask().execute(question.toString());
	}

	private class PostQuestionTask extends AsyncTask<String, Void, Void> {

		ProgressDialog progressDialog;
		
		@Override
		protected void onPreExecute() {
			progressDialog  = new ProgressDialog(CreateQuestionActivity.this);
			progressDialog.setTitle("Posting Question..");
			progressDialog.setCancelable(false);
			progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			finish();
			super.onPostExecute(result);
		}

		@Override
		protected Void doInBackground(String... params) {
			URL url;
			Map<String, String> urlParams = new HashMap<String, String>();
			OutputStreamWriter osw=null;
			urlParams.put("gid", CreateQuestionActivity.this.getResources().getString(R.string.gid));
			urlParams.put("q", params[0]);
			try {
				url = new URL(CreateQuestionActivity.this.getResources().getString(R.string.baseUrl) + "saveNew.php");
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
				}else{
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
