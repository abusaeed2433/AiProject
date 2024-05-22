package com.unknownn.aiproject.classes;

import static com.unknownn.aiproject.classes.Calculator.LOSS;
import static com.unknownn.aiproject.classes.Calculator.WIN;

import android.hardware.lights.LightsManager;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import kotlin.Pair;

public class GeneticApplier {

    // Remember, the whole thing is running inside a thread
    private final Random random = new Random();
    private static final int POPULATION_SIZE = 5;
    private static final int NO_OF_IT = 10;
    private static final int TOURNAMENT_SIZE = 2;
    private static GeneticApplier instance = null;


    private int N;
    private CellState.MyColor[][] board;

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

    private List<Cell> applyCrossover(List<Cell> parentOne, List<Cell> parentTwo){
        final int half = parentOne.size() / 2;

        final int quarter = half / 2;

        List<Cell>
    }

    private List<Cell> getParentFromTournament(List<List<Cell>> population){
        List<List<Cell>> tournament = new ArrayList<>();

        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            int randomIndex = random.nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }

        return Collections.max(tournament, (chOne, chTwo) -> calcFitness(board,chTwo) - calcFitness(board,chOne));
    }

    private List<Cell> getTheBest(List<List<Cell>> population){
        return Collections.max(population, (chOne, chTwo) -> calcFitness(board,chTwo) - calcFitness(board,chOne));
    }

    private int calcFitness(final CellState.MyColor[][] board, List<Cell> chromosome){
        // copy the board to apply the chromosome
        final CellState.MyColor[][] copiedBoard = new CellState.MyColor[N][N];

        for(int x=0; x<N; x++){
            System.arraycopy(board[x], 0, copiedBoard[x], 0, N);
        }
        for(Cell cell : chromosome){
            copiedBoard[cell.x][cell.y] = cell.myColor;
        }

        final CellState.MyColor winner = Calculator.getGameWinner(copiedBoard, N);

        if( winner == CellState.MyColor.BLUE ) return WIN; // 100

        if( winner == CellState.MyColor.RED ) return LOSS; // -100

        return 0;
    }

    @Nullable
    private List<List<Cell>> initPopulation(CellState.MyColor[][] board){

        final List<Pair<Integer,Integer>> emptyList = new ArrayList<>();

        for(int x = 0; x < N; x++){
            for(int y = 0; y<N; y++){
                if(board[x][y] == CellState.MyColor.BLANK){
                    emptyList.add(new Pair<>(x,y));
                }
            }
        }

        if(emptyList.size() < 5) {
            geneticListener.onError("Can't apply. Too few cells are left", true);
            return null;
        }

        final List<List<Cell>> populations = new ArrayList<>();

        for(int i=0; i<POPULATION_SIZE; i++){
            final List<Cell> chromosome = new ArrayList<>();

            // copy empty list
            final List<Pair<Integer, Integer>> copiedEmptyList = new ArrayList<>(emptyList);

            int half = copiedEmptyList.size() / 2;
            int count = 0;
            while (!copiedEmptyList.isEmpty()){
                final int index = random.nextInt(copiedEmptyList.size());
                final Pair<Integer,Integer> pair = copiedEmptyList.get(index);

                final Cell cell = new Cell(
                        pair.getFirst(),
                        pair.getSecond(),
                        (count < half) ? CellState.MyColor.RED : CellState.MyColor.BLUE
                );

                chromosome.add(cell);
                count++;
                copiedEmptyList.remove(index);
            }

            populations.add(chromosome);
        }

        return populations;
    }

    public void predict(int N, CellState.MyColor[][] board){
        this.N = N;
        this.board = board;

        List<List<Cell>> populations = initPopulation(board);
        if(populations == null) return;


        List<Cell> globalBest = getTheBest(populations);
        int globalBestVal = calcFitness(board, globalBest);

        int it = 0;
        while (it++ < NO_OF_IT){

            List<List<Cell>> offspring = new ArrayList<>();

            while (offspring.size() < populations.size()){

                final List<Cell> parentOne = getParentFromTournament(populations);
                final List<Cell> parentTwo = getParentFromTournament(populations);

                final List<Cell> child = applyCrossover(parentOne, parentTwo);
                final List<Cell> mutatedChild = mutateChild(child);

                offspring.add(mutatedChild);
            }

            final List<Cell> localBest = getTheBest(offspring);

            int localBestVal = calcFitness(board, localBest);

            if(localBestVal > globalBestVal){
                globalBestVal = localBestVal;
                globalBest = localBest;
            }
        }

        for(Cell cell : globalBest){
            if(cell.myColor == CellState.MyColor.RED){
                geneticListener.onFinished(new Pair<>(cell.x, cell.y));
                return;
            }
        }

    }

    public interface GeneticListener {
        void onProgress(int progress);
        void onFinished(Pair<Integer,Integer> xy);
        void onError(String message, boolean changeToAlphaBeta);
    }
}
