/**
 * ResultActivity.java
 * A Yang
 * Ajay Vijayakumaran Nair
 * Nachiket Doke
 */
package com.example.quiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ResultActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		Intent intent= getIntent();
		if(intent.getExtras() != null){
			int correctAnsCount = intent.getExtras().getInt(MainActivity.NO_OF_CORRECT_ANS_KEY);
			int questionCount = intent.getExtras().getInt(MainActivity.NO_OF_Q_KEY);
			((TextView)findViewById(R.id.textViewPercentCorrect)).setText(((correctAnsCount* 100)/questionCount ) + "%");
			ProgressBar pb = ((ProgressBar)findViewById(R.id.progressBar1));
			pb.setMax(questionCount);
			pb.setProgress(correctAnsCount);
			TextView resultDescTv = (TextView) findViewById(R.id.textViewResultDesc);
			if(questionCount == correctAnsCount){
				resultDescTv.setText(getResources().getString(R.string.resultDescTxtAllCorrect));
			}else{
				resultDescTv.setText(getResources().getString(R.string.resultDescTxtSomeCorrect));
			}
		}else{
			Log.d(MainActivity.LOGGING_KEY, "Extras are null");
		}
	}
	public void tryAgainBtnClicked(View view){
		Intent intent = new Intent(this, TriviaActivity.class);
		startActivity(intent);
		finish();
	}
	public void quitBtnClicked(View view){
		finish();
	}
}
