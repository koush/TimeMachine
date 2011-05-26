package com.koushikdutta.timemachine;

public abstract class SuCommandCallback implements Callback<Integer> {
    void onStartBackground() {};
    void onOutputLine(String line) {};
    void onResultBackground(int result) {};
}
