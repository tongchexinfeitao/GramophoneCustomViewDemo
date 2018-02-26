package com.example.gramophonecustomviewdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private GramophoneView gramophoneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gramophoneView = (GramophoneView) findViewById(R.id.gramopone);
    }

    public void pauseOrstart(View view) {
        gramophoneView.pauseOrstart();
    }
}
