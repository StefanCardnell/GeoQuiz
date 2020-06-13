package com.bignerdranch.android.geoquiz;

import android.os.Parcel;
import android.os.Parcelable;

public class Question implements Parcelable {

    private int mTextResId;
    private boolean mAnswer;
    private boolean mCheated;
    private Boolean mGivenAnswer;

    public Question(int textResId, boolean answer) {
        mTextResId = textResId;
        mAnswer = answer;
    }

    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }

    public boolean isAnswerTrue() {
        return mAnswer;
    }

    public void setAnswerTrue(boolean answerTrue) {
        mAnswer = answerTrue;
    }

    public Boolean getGivenAnswer() {
        return mGivenAnswer;
    }

    public void setGivenAnswer(Boolean givenAnswer) {
        this.mGivenAnswer = givenAnswer;
    }

    public boolean notAnswered() {
        return mGivenAnswer == null;
    }

    public boolean isAnswerCorrect() {
        return Boolean.valueOf(mAnswer).equals(mGivenAnswer);
    }

    public boolean getCheated() {
        return mCheated;
    }

    public void setCheated(boolean cheated) {
        if(mCheated) return; // Once a cheater always a cheater
        mCheated = cheated;
    }

    // Parcelable implementation
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeInt(mTextResId);
        out.writeInt(mAnswer ? 1 : 0);
        out.writeInt(mCheated ? 1 : 0);
        out.writeValue(mGivenAnswer);
    }

    public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>(){
        public Question createFromParcel(Parcel in){
            return new Question(in);
        }

        public Question[] newArray(int size){
            return new Question[size];
        }

    };

    private Question(Parcel in){
        mTextResId = in.readInt();
        mAnswer = in.readInt() != 0;
        mCheated = in.readInt() != 0;
        mGivenAnswer = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

}
