package com.unknownn.aiproject.classes;

import kotlin.Pair;

public class GeneticApplier {

    private int N;
    private static final int POPULATION_SIZE = 5;
    private static GeneticApplier instance = null;

    public static GeneticApplier getInstance(){
        if(instance == null) instance = new GeneticApplier();
        return instance;
    }

    private GeneticApplier() {}

    private GeneticListener geneticListener = null;

    public GeneticApplier setGeneticListener(GeneticListener geneticListener){
        this.geneticListener = geneticListener;
        return this;
    }

    private Pair<Integer,Integer>[] initPopulation(CellState.MyColor[][] board){
        final Pair<Integer,Integer>[] population = new Pair[POPULATION_SIZE];

        int count = 0;
        for(int x = 0; x < N; x++){
            for(int y = 0; y<N; y++){
                if(board[x][y] == CellState.MyColor.BLANK){
                    population[count] = new Pair<>(x,y);
                    count++;
                }
                if(count >= POPULATION_SIZE){
                    return population;
                }
            }
        }
        geneticListener.onError("Can't apply. Too few cells are left",true);
        return null;
    }

    public void predict(int N, CellState.MyColor[][] board){
        this.N = N;

        Pair<Integer,Integer>[] populations = initPopulation(board);
        if(populations == null) return;


        geneticListener.onError("First empty cell is selected",false);
        geneticListener.onFinished(populations[0]);


    }

    public interface GeneticListener {
        void onProgress(int progress);
        void onFinished(Pair<Integer,Integer> xy);
        void onError(String message, boolean changeToAlphaBeta);
    }
}
