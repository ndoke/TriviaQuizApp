/**
 * TriviaActivity.java
 * A Yang
 * Ajay Vijayakumaran Nair
 * Nachiket Doke
 */
package com.example.quiz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TriviaActivity extends Activity {

	private int currentQIndex = -1;
	private List<Question> questions;
	private CountDownTimer countDownTimer;
	private AsyncTask<String, Void, Bitmap> currentPhotoTask;
	AsyncTask<Void, Void, List<Question>> fetchQTask;
	private int correctAnsCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trivia);
		fetchQTask = new FetchQuestions().execute();

	}

	public void quitBtnClicked(View view) {
		stopAsyncTasks();
		finish();
	}

	public void nextBtnClicked(View view) {
		displayNextQuestion();
	}

	public void stopAsyncTasks() {
		if (fetchQTask != null) {
			fetchQTask.cancel(true);
		}
		if (currentPhotoTask != null) {
			currentPhotoTask.cancel(true);
		}
		if (this.countDownTimer != null) {
			this.countDownTimer.cancel();
		}
	}

	private void collectAnswer(int questionIndex) {
		int answerIdx = ((RadioGroup) findViewById(R.id.radioGroup1)).getCheckedRadioButtonId();
		if (questions.get(questionIndex).getIndexOfCorrectAnswer() == answerIdx) {
			this.correctAnsCount++;
		} else {
			Log.d(MainActivity.LOGGING_KEY, "incorrect answer for Q:" + questionIndex);
		}
	}

	private void displayNextQuestion() {
		currentQIndex++;

		if (this.countDownTimer != null) {
			this.countDownTimer.cancel();
		}
		if (this.currentPhotoTask != null) {
			this.currentPhotoTask.cancel(true);
		}
		if (currentQIndex != 0) {
			collectAnswer(currentQIndex - 1);
		}
		if (currentQIndex >= questions.size()) {
			Intent intent = new Intent(this, ResultActivity.class);
			intent.putExtra(MainActivity.NO_OF_Q_KEY, this.questions.size());
			intent.putExtra(MainActivity.NO_OF_CORRECT_ANS_KEY, this.correctAnsCount);
			// finish();
			Log.d(MainActivity.LOGGING_KEY, "Q :" + questions.size() + ", A :" + correctAnsCount);
			// stopAsyncTasks();
			startActivity(intent);
			return;
		}
		((ScrollView) findViewById(R.id.scrollView1)).scrollTo(0, 0);
		((TextView) findViewById(R.id.textViewTimeLeft)).setText("Time Left: 24 seconds");
		Question question = this.questions.get(currentQIndex);
		((TextView) findViewById(R.id.textViewQNo)).setText("Q " + (currentQIndex + 1));
		((TextView) findViewById(R.id.textViewQ)).setText(question.getQuestion());
		int index = 0;
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		radioGroup.clearCheck();
		radioGroup.removeAllViews();
		for (String choice : question.getChoices()) {
			RadioButton radioButton = new RadioButton(TriviaActivity.this);
			radioButton.setText(choice);
			radioButton.setId(index++);
			radioButton.setEnabled(true);
			radioGroup.addView(radioButton);
		}
		if (question.getUrl() != null && !question.getUrl().isEmpty()) {
			currentPhotoTask = new LoadPhotoTask().execute(question.getUrl());
		} else {
			ImageView iv = ((ImageView) findViewById(R.id.imageViewAddOpt));
			iv.setImageDrawable(getResources().getDrawable(R.drawable.ni));
			iv.setVisibility(View.VISIBLE);
		}
		this.countDownTimer = new CountDownTimer(24 * 1000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {
				((TextView) findViewById(R.id.textViewTimeLeft)).setText("Time Left: " + (int) millisUntilFinished
						/ 1000 + " seconds");
			}

			@Override
			public void onFinish() {
				displayNextQuestion();
			}
		}.start();
	}

	@Override
	protected void onStop() {
		stopAsyncTasks();
		super.onStop();
	}

	private class FetchQuestions extends AsyncTask<Void, Void, List<Question>> {
		private ProgressDialog progressDialogue;

		@Override
		protected void onPreExecute() {
			currentQIndex = -1;
			correctAnsCount = 0;
			progressDialogue = new ProgressDialog(TriviaActivity.this);
			progressDialogue.setTitle("Loading Questions");
			progressDialogue.setCancelable(false);
			progressDialogue.show();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(List<Question> result) {
			TriviaActivity.this.questions = result;
			Log.d(MainActivity.LOGGING_KEY, "Questions fetched " + result.size());
			progressDialogue.dismiss();
			displayNextQuestion();
			super.onPostExecute(result);
		}

		@Override
		protected List<Question> doInBackground(Void... params) {
			URL url;
			BufferedReader breader = null;
			List<Question> questions = new ArrayList<Question>();
			try {
				url = new URL(TriviaActivity.this.getResources().getString(R.string.baseUrl) + "getAll.php");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();
				int statusCode = connection.getResponseCode();
				if (statusCode == HttpURLConnection.HTTP_OK) {
					InputStream inStream = connection.getInputStream();
					breader = new BufferedReader(new InputStreamReader(inStream));
					String line = "";
					while ((line = breader.readLine()) != null) {
						try {
							if (line.isEmpty()) {
								continue;
							}
							String[] questionArray = line.split(";");
							Question question = new Question();
							question.setQuestion(questionArray[0]);
							if (questionArray.length < 5) {
								Log.d(MainActivity.LOGGING_KEY, "Question " + line
										+ " has issues. Not enough fields.\nSkipping..");
								continue;
							}
							for (int i = 1; i < questionArray.length - 2; i++) {
								if (!questionArray[i].isEmpty()) {
									question.getChoices().add(questionArray[i]);
								}
							}
							question.setUrl(questionArray[questionArray.length - 2]);
							question.setIndexOfCorrectAnswer(Integer.parseInt(questionArray[questionArray.length - 1]));
							questions.add(question);
						} catch (NumberFormatException e) {
							e.printStackTrace();
							Log.d(MainActivity.LOGGING_KEY, "Problem while parsing " + line + "\nSkipping..");
						}
					}
				} else {
					Log.d(MainActivity.LOGGING_KEY, "Http status is not OK");
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (breader != null) {
					try {
						breader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return questions;
		}

	}

	private class LoadPhotoTask extends AsyncTask<String, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			if (!MainActivity.isNetworkConnected(TriviaActivity.this)) {
				Toast.makeText(TriviaActivity.this, "No internet connectivity", Toast.LENGTH_LONG).show();
				this.cancel(true);
				return;
			}
			((ImageView) findViewById(R.id.imageViewAddOpt)).setVisibility(View.GONE);
			findViewById(R.id.progressBarWrapper).setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected void onCancelled(Bitmap result) {
			((ImageView) findViewById(R.id.imageViewAddOpt)).setVisibility(View.VISIBLE);
			findViewById(R.id.progressBarWrapper).setVisibility(View.GONE);
			super.onCancelled(result);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			((ImageView) findViewById(R.id.imageViewAddOpt)).setVisibility(View.VISIBLE);
			findViewById(R.id.progressBarWrapper).setVisibility(View.GONE);
			ImageView imageView = (ImageView) findViewById(R.id.imageViewAddOpt);
			imageView.setImageBitmap(result);
			super.onPostExecute(result);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			String photoUrl = params[0];
			Bitmap bmp = null;
			URL url;
			try {
				url = new URL(photoUrl);
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return bmp;
		}

	}

}
