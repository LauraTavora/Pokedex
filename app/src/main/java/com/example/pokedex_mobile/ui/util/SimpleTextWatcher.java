package com.example.pokedex_mobile.ui.util;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {
    public interface OnChange { void onChange(String s); }
    private final OnChange cb;
    public SimpleTextWatcher(OnChange cb) { this.cb = cb; }
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) { cb.onChange(s != null ? s.toString() : ""); }
    @Override public void afterTextChanged(Editable s) {}
}

