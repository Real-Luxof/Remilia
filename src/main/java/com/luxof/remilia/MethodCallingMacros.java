package com.luxof.remilia;

public class MethodCallingMacros {

    private static String padStart(String str) {
        return (str.length() < 2 ? "0" : "") + str;
    }
    private static String hex(int[] color) {
        return padStart(Integer.toHexString(color[0]))
            + padStart(Integer.toHexString(color[1]))
            + padStart(Integer.toHexString(color[2]));
    }


    private static int[] blue = {0x00, 0x00, 0xff};
    private static int[] red = {0xff, 0x00, 0x00};
    public static String blueRedGradient(String text) {
        int len = text.length();
        int[] step = {
            (red[0] - blue[0]) / len,
            (red[1] - blue[1]) / len,
            (red[2] - blue[2]) / len
        };
        int[] curr = blue.clone();

        String newText = "";
        for (int i = 0; i < len; i++) {
            char chr = text.charAt(i);
            newText += "$(#" + hex(curr) + ")" + chr;
            curr[0] += step[0];
            curr[1] += step[1];
            curr[2] += step[2];
        }
        newText += "$()";

        return newText;
    }
}
