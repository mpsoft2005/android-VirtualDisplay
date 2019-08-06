package com.ben.testvirtualdisplay;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.widget.TextView;

public class CounterPresentation extends Presentation {

    final String TAG = "Presentation";

    Context mContext;

    int count = 0;
    TextView counter_text;

    Handler mHandler;

    long updateTextDelay = 1000; // ms
    Runnable updateTextRunnable = new Runnable() {
        @Override
        public void run() {
            updateCounterText();
            mHandler.postDelayed(updateTextRunnable, updateTextDelay);
        }
    };

    public CounterPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        mContext = outerContext;
    }

    void updateCounterText() {
        Log.i(TAG, "updateCounterText " + count);
        counter_text.setText(String.format("Counter %d", count));
        count++;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.counter_presentation);
        counter_text = findViewById(R.id.counter_text);

        updateCounterText();

        mHandler = new Handler(mContext.getMainLooper());
        mHandler.postDelayed(updateTextRunnable, updateTextDelay);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        mHandler.removeCallbacks(updateTextRunnable);
    }
}
