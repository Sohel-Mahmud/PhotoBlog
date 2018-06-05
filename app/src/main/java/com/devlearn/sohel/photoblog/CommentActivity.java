package com.devlearn.sohel.photoblog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.budiyev.android.circularprogressbar.CircularProgressBar;

public class CommentActivity extends AppCompatActivity {

    CircularProgressBar progressBar1, progressBar2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        progressBar1 = (CircularProgressBar)findViewById(R.id.progressBarImpression);
        progressBar2 = (CircularProgressBar)findViewById(R.id.progressBarClicks);
        progressBar1.setProgress(45f);
        progressBar2.setProgress(90f);
    }
}
