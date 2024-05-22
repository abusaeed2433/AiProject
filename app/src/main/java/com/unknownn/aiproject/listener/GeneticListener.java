package com.unknownn.aiproject.listener;

import kotlin.Pair;

public interface GeneticListener {
    void onProgress(int progress);
    void onFinished(Pair<Integer,Integer> xy);
    void onError(String message, boolean changeToAlphaBeta);
}
