package com.unknownn.aiproject.listener;

import com.unknownn.aiproject.classes.Cell;

import java.util.List;

import kotlin.Pair;

public interface GeneticListener {
    void onProgress(int progress);
    void onFinished(Pair<Integer,Integer> xy);
    void onDrawRequest(List<Cell> selectedBoard);
    void onError(String message, boolean changeToAlphaBeta);
}
