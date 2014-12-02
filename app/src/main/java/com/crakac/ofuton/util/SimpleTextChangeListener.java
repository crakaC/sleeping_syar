package com.crakac.ofuton.util;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by kosukeshirakashi on 2014/12/02.
 */
public class SimpleTextChangeListener implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
