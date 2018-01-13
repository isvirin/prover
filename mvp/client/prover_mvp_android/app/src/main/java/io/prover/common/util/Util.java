package io.prover.common.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by babay on 12.01.2018.
 */

public class Util {
    public static void hideKeyboard(View any) {
        InputMethodManager imm = (InputMethodManager) any.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(any.getWindowToken(), 0);
    }
}
