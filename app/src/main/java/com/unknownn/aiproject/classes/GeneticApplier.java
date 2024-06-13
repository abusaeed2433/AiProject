package com.unknownn.aiproject.classes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unknownn.aiproject.listener.GeneticListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kotlin.Pair;

public class GeneticApplier {

    // Remember, the whole thing is running inside a thread
    private final Random random = new Random();
    private static final int POPULATION_SIZE = 10;
    private static final int MUTATION_RATE = 5;
    private static final int NO_OF_IT = 250;
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

    // Random point swapped
    private List<Cell> mutateChildren(List<Cell> chromosome){

        final int rand = random.nextInt(100);

        if (rand < MUTATION_RATE) {
            final int indexOne = random.nextInt(chromosome.size());
            int indexTwo = random.nextInt(chromosome.size());

            while (indexOne == indexTwo) {
                indexTwo = random.nextInt(chromosome.size());
            }

            final Cell temp = chromosome.get(indexOne);
            chromosome.set(indexOne, chromosome.get(indexTwo));
            chromosome.set(indexTwo, temp);

            CellState.MyColor color = (indexOne < (chromosome.size()/2)) ? CellState.MyColor.RED : CellState.MyColor.BLUE;
            chromosome.get(indexOne).setMyColor(color);

            color = (indexTwo < (chromosome.size()/2)) ? CellState.MyColor.RED : CellState.MyColor.BLUE;
            chromosome.get(indexTwo).setMyColor(color);
        }

        return chromosome;
    }

    // partially mapped cross-over
    @NonNull
    private static List<List<Cell>> applyCrossover(@NonNull List<Cell> parentOne, List<Cell> parentTwo){
        final int half = parentOne.size() / 2;

        final Cell[] childOne = new Cell[parentOne.size()];
        final Cell[] childTwo = new Cell[parentOne.size()];

        final Random random = new Random();

        int indexOne = random.nextInt(half);
        int indexTwo = half + random.nextInt(half);

        final Map<Cell,Cell> mapOne = new HashMap<>();
        final Map<Cell,Cell> mapTwo = new HashMap<>();

        for(int i = indexOne; i<indexTwo; i++){
            childOne[i] = new Cell(parentTwo.get(i));
            childTwo[i] = new Cell(parentOne.get(i));

            mapTwo.put(parentOne.get(i), parentTwo.get(i));
            mapOne.put(parentTwo.get(i), parentOne.get(i));
        }

        for(int i=0; i<childOne.length; i++){
            if(i < indexOne || i >= indexTwo) {
                Cell cellOne = parentOne.get(i);
                while (mapOne.containsKey(cellOne)) {
                    cellOne = mapOne.get(cellOne);
//                    System.out.println(cellOne);
                }

                assert cellOne != null;
                childOne[i] = new Cell(cellOne);

                Cell cellTwo = parentTwo.get(i);
                while (mapTwo.containsKey(cellTwo)) {
                    cellTwo = mapTwo.get(cellTwo);
                }

                assert cellTwo != null;
                childTwo[i] = new Cell(cellTwo);
            }

            // making the first half of same color
            CellState.MyColor color = (i < half) ? CellState.MyColor.RED : CellState.MyColor.BLUE;

            childOne[i].setMyColor(color);
            childTwo[i].setMyColor(color);
        }

        final List<List<Cell>> offspring = new ArrayList<>();
        final List<Cell> child1 = new ArrayList<>();
        final List<Cell> child2 = new ArrayList<>();

        for(int i=0; i<childOne.length; i++){
            child1.add(childOne[i]);
            child2.add(childTwo[i]);
        }

        offspring.add(child1);
        offspring.add(child2);

        return offspring;
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

        return Calculator.getBoardScore(copiedBoard,N);
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

        if(emptyList.size() < POPULATION_SIZE) {
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

//            chromosome.sort(Comparator.comparingInt(t -> t.myColor.id));
            populations.add(chromosome);
        }

        return populations;
    }

    private boolean isPrevSolutionWinnable(List<Cell> solution, final CellState.MyColor[][] curBoard){
        if(solution == null) return false;

        final CellState.MyColor[][] copiedBoard = new CellState.MyColor[N][N];

        for(int x=0; x<N; x++){
            System.arraycopy(curBoard[x], 0, copiedBoard[x], 0, N);
        }

        for(int i=solution.size()-1; i>=0; i--){
            final Cell cell = solution.get(i);
            if(copiedBoard[cell.x][cell.y] == CellState.MyColor.BLANK) {
                copiedBoard[cell.x][cell.y] = cell.myColor;
            }
            else{
                solution.remove(cell); // not valid anymore
            }
        }

        final CellState.MyColor winner = Calculator.getGameWinner(copiedBoard, N);

        return winner == CellState.MyColor.BLUE;
    }

    private Pair<Integer,Integer> chooseTheBestCellToPlace(List<Cell> solution){
        for(Cell cell : solution){
            if(cell.myColor == CellState.MyColor.BLUE){
                solution.remove(cell);
                return new Pair<>(cell.x, cell.y);
            }
        }
        return null;
    }

    private List<Cell> prevBestSolution = null;
    private volatile CellState lastClickedCell = null;
    public void predict(int N, CellState.MyColor[][] board, CellState lastClickedCell){
        this.N = N;
        this.board = board;
        this.lastClickedCell = lastClickedCell;

        if( isPrevSolutionWinnable(prevBestSolution, board) ){
            geneticListener.onProgress(100);
            geneticListener.onDrawRequest(prevBestSolution);


            final Pair<Integer,Integer> toPlace = chooseTheBestCellToPlace(prevBestSolution);
            if(toPlace == null){
                if(geneticListener != null) {
                    geneticListener.onError("Something went wrong",true);
                }
                return;
            }

            if(geneticListener != null) {
                geneticListener.onFinished(new Pair<>(toPlace.getFirst(), toPlace.getSecond()));
            }

            return;
        }

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

                final List<List<Cell>> childAfterCross = applyCrossover(parentOne, parentTwo);

                for(List<Cell> child : childAfterCross) {
                    final List<Cell> mutatedChild = mutateChildren(child);
                    offspring.add(mutatedChild);
                }
            }

            final List<Cell> localBest = getTheBest(offspring);

            int localBestVal = calcFitness(board, localBest);

            if(localBestVal > globalBestVal){
                globalBestVal = localBestVal;
                globalBest = localBest;
            }

            int progress = (100*it) / NO_OF_IT;
            geneticListener.onProgress(progress);
        }

        prevBestSolution = globalBest;
        geneticListener.onDrawRequest(globalBest);


        final Pair<Integer,Integer> toPlace = chooseTheBestCellToPlace(globalBest);
        if(toPlace == null){
            if(geneticListener != null) {
                geneticListener.onError("Something went wrong",true);
            }
            return;
        }

        if(geneticListener != null) {
            geneticListener.onFinished(new Pair<>(toPlace.getFirst(), toPlace.getSecond()));
        }
    }

}
