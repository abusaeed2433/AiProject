package com.unknownn.aiproject.listener;

import kotlin.Pair;

public interface AlphaBetaListener {
    void onProgress(int progress);
    void onFinished(Pair<Integer,Integer> xy);
    void onError(String message);
    void onCellValueUpdated(int x, int y, int movVal);
}
