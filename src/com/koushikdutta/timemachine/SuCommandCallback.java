package com.koushikdutta.timemachine;

public interface SuCommandCallback extends Callback<Integer> {
    void onOutputLine(String line);
}
