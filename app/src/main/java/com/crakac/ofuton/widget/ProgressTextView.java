package com.crakac.ofuton.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crakac.ofuton.R;

public class ProgressTextView extends FrameLayout {
    private TextView mText;
    private ProgressBar mProgressBar;

    public ProgressTextView(Context context) {
        this(context, null);
    }

    public ProgressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.progress_text_view, null);
        addView(v);

        mText = (TextView) v.findViewById(R.id.text);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressTextView);
        mText.setText(a.getString(R.styleable.ProgressTextView_text));
        a.recycle();
    }

    public void loading() {
        mText.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void standby() {
        mText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void setText(String text) {
        mText.setText(text);
    }

    public void setText(int resId) {
        setText(getContext().getString(resId));
    }
}