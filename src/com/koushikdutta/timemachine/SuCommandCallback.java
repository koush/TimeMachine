package com.koushikdutta.timemachine;

public abstract class SuCommandCallback implements Callback<Integer> {
    void onOutputLine(String line) {};
    void onResultBackround(int result) {};
}
