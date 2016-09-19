/**
 * Question.java
 * A Yang
 * Ajay Vijayakumaran Nair
 * Nachiket Doke
 */
package com.example.quiz;

import java.util.ArrayList;
import java.util.List;

public class Question {
	private String question;
	private List<String> choices;
	private int indexOfCorrectAnswer;
	private String url;
	
	{
		this.choices = new ArrayList<String>();
	}
	
	public Question() {
		super();
		
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public List<String> getChoices() {
		return choices;
	}
	public void setChoices(List<String> choices) {
		this.choices = choices;
	}
	public int getIndexOfCorrectAnswer() {
		return indexOfCorrectAnswer;
	}
	public void setIndexOfCorrectAnswer(int indexOfCorrectAnswer) {
		this.indexOfCorrectAnswer = indexOfCorrectAnswer;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
}
