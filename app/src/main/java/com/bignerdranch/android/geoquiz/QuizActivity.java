package com.bignerdranch.android.geoquiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_CHEATS_LEFT = "cheats";
    private static final String KEY_QUESTIONS = "questions";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mQuestionTextView;
    private TextView mCheatsRemainTextView;

    private int mCurrentIndex = 0;
    private int mAnswered = 0;
    private int mCheatsLeft = 3;

    private Question[] mQuestionBank = new Question[]{
        new Question(R.string.question_australia, true),
        new Question(R.string.question_oceans, true),
        new Question(R.string.question_mideast, false),
        new Question(R.string.question_africa, false),
        new Question(R.string.question_americas, true),
        new Question(R.string.question_asia, true),
    };

    protected class ChangeQuestionListener implements View.OnClickListener {
        private boolean mForward;

        ChangeQuestionListener() {
            mForward = true;
        }

        ChangeQuestionListener(boolean forward) {
            mForward = forward;
        }

        @Override
        public void onClick(View v) {
            int q_len = mQuestionBank.length;
            // Add q_len to cater for minus (% is remainder op, not mod)
            mCurrentIndex = (mCurrentIndex + (mForward ? 1 : -1) + q_len) % q_len;
            QuizActivity.this.updateQuestion();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mAnswered = savedInstanceState.getInt(KEY_ANSWER, 0);
            mCheatsLeft = savedInstanceState.getInt(KEY_CHEATS_LEFT, mCheatsLeft);

            // Unfortunately can't directly use return type, as Parcelable[] is returned that
            // cannot be cast to Question[] if the app was removed from memory. See
            // https://stackoverflow.com/questions/8745893/i-dont-get-why-this-classcastexception-occurs/14866690#14866690
            Parcelable[] ps = savedInstanceState.getParcelableArray(KEY_QUESTIONS);
            for(int i = 0; i < ps.length; ++i) mQuestionBank[i] = (Question) ps[i];
        }

        mQuestionTextView = findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new ChangeQuestionListener());

        mCheatsRemainTextView = findViewById(R.id.cheats_remain);

        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new ChangeQuestionListener());

        mPrevButton = findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new ChangeQuestionListener(false));

        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });
        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        updateQuestion();
        reviewCheatButton();
        setApiLevel();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putInt(KEY_ANSWER, mAnswered);
        savedInstanceState.putInt(KEY_CHEATS_LEFT, mCheatsLeft);
        savedInstanceState.putParcelableArray(KEY_QUESTIONS, mQuestionBank);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK){
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT){
            if(data == null){
                return;
            }
            if(CheatActivity.wasAnswerShown(data)){
                mCheatsLeft--;
                mQuestionBank[mCurrentIndex].setCheated(true);
                reviewCheatButton();
            }
        }
    }

    private void checkAnswer(boolean userPressedTrue) {
        Question currentQ = mQuestionBank[mCurrentIndex];
        currentQ.setGivenAnswer(userPressedTrue);
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
        mAnswered++;

        if (mAnswered == mQuestionBank.length) {
            showPercentageToast(); // Present finishing message
        } else {
            int messageResId = 0;
            if(currentQ.getCheated()) {
                messageResId = R.string.judgement_toast;
            } else {
                messageResId = currentQ.isAnswerCorrect() ? R.string.correct_toast : R.string.incorrect_toast;
            }
            Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        }

    }

    @SuppressLint("DefaultLocale")
    private void showPercentageToast() {
        int correct = 0;
        int cheated = 0;
        for (Question q : mQuestionBank) {
            if(q.getCheated()) cheated++;
            else if (q.isAnswerCorrect()) correct++;
        }
        int pct = (correct * 100) / mQuestionBank.length;
        Toast.makeText(this, String.format("Quiz complete, %s%% correct. %s cheats used.", pct, cheated), Toast.LENGTH_LONG).show();
    }

    private void updateQuestion() {
        Question currentQ = mQuestionBank[mCurrentIndex];
        mQuestionTextView.setText(currentQ.getTextResId());
        mTrueButton.setEnabled(currentQ.notAnswered());
        mFalseButton.setEnabled(currentQ.notAnswered());
    }

    private void reviewCheatButton() {
        mCheatsRemainTextView.setText(getString(R.string.cheats_remain, mCheatsLeft));
        mCheatButton.setEnabled(mCheatsLeft > 0);
    }

    private void setApiLevel(){
        String api_level = getString(R.string.api_level, Build.VERSION.SDK_INT);
        ((TextView) findViewById(R.id.api_v_text_view)).setText(api_level);
    }
}
