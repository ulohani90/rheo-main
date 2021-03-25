package com.rheotv.android.utils;

import java.text.DecimalFormat;

public class NumberUtils {
    public static String getFormattedCount(int number) {
        if (number > 999999) {
            return Kfy(number / 1000000f, "M");
        } else if (number > 999) {
            return Kfy(number / 1000f, "K");
        } else {
            return Integer.toString(number);
        }
    }

    private static String Kfy(float num, String suffix) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        if (num - (int) num > 0.1) {
            return decimalFormat.format(num) + suffix;
        } else {
            return Integer.toString((int) num) + suffix;
        }
    }
}

