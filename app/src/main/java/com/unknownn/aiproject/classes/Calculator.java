package com.unknownn.aiproject.classes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import kotlin.Pair;

public class Calculator {
    public static final int WIN = 100, LOSS = -100;
    private static final int MULTIPLIER = 10;
    public static final int NO_WIN = 100;

    public static CellState.MyColor getGameWinner(CellState.MyColor[][] field, int N){
        final Pair<Integer,Integer> scores = calcBoardScore(field, N, true);

        if(scores.getFirst() == N){ // Red won
            return CellState.MyColor.RED;
        }

        if(scores.getSecond() == N){
            return CellState.MyColor.BLUE;
        }
        return null;
    }

    public static int getBoardScore(CellState.MyColor[][] field, int N){
        final Pair<Integer,Integer> scores = Calculator.calcBoardScore(field, N,false);
        return scores.getFirst() * MULTIPLIER - scores.getSecond() * MULTIPLIER;
    }

    private static Pair<Integer,Integer> calcBoardScore(CellState.MyColor[][] field, int N, boolean noOptimization){
        // left to right for Red
        int redNegScore = -NO_WIN;
        int redPosScore = NO_WIN;
        for(int y=0; y<N; y++){
            if( field[0][y] != CellState.MyColor.RED ) continue;
            final int score = connectedToEndBy(field,N,0,y, true);

            if(score < 0){ // need |score| cells move to win
                redNegScore = Math.max(redNegScore, score);
            }
            else { // won with path length score
                redPosScore = Math.min(redPosScore, score);
            }

            if(noOptimization && redPosScore != NO_WIN){ // no optimization is requested and red won
                 return new Pair<>(N,0); // red won, no optimization is performed
            }
            if(redPosScore == N) break;
        }

        // top to bottom for Blue
        int blueNegScore = -NO_WIN;
        int bluePosScore = NO_WIN;
        for(int x=0; x<N; x++){
            if( field[x][0] != CellState.MyColor.BLUE ) continue;

            final int score = connectedToEndBy(field, N, x, 0, false);
            if(score < 0){ // need |score| cells move to win
                blueNegScore = Math.max(blueNegScore, score);
            }
            else { // won with path length `score`
                bluePosScore = Math.min(bluePosScore, score);
            }

            if(noOptimization && bluePosScore != NO_WIN){ // no optimization is requested and blue won
                return new Pair<>(0,N); // blue won, no optimization is performed
            }

            if(bluePosScore == N) break;
        }

        // NO_WIN - posScore is used to give lower pos value, more score
        return new Pair<>(NO_WIN - redPosScore, NO_WIN - bluePosScore); // use negative score here somehow.
        // red score, blue score -> the more the better
        // pos score -> less better
        // blue score -> less better
    }

    /*
     ex:
     5: can win by using 5 cells
     -3: winning path requires at least 3 cell to win
     negative part is not done yet
     */

    public static int connectedToEndBy(CellState.MyColor[][] field, int N, int x, int y, boolean horizontal){
        if(field[x][y] == CellState.MyColor.BLANK) return 0;

        final Queue<Pair<Integer,Integer>> queue = new LinkedList<>();

        queue.add( new Pair<>(x,y) );
        final boolean[][] visited = new boolean[N][N];
        visited[x][y] = true;

        int score = N*N*2;
        final Map<Integer,Integer> parentMap = new HashMap<>();
        parentMap.put(x*N + y, -1);

        while ( !queue.isEmpty() ){
            final Pair<Integer,Integer> pair = queue.poll();
            if(pair == null) continue;

            final int[][] offsets = { {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1},{0, 1} };

            for(int[] offset : offsets){
                final int row = pair.getFirst() + offset[0];
                final int col = pair.getSecond() + offset[1];

                if(row < 0 || row >= N || col < 0 || col >= N) continue;

                if( field[pair.getFirst()][pair.getSecond()] == field[row][col] && !visited[row][col] ){

                    parentMap.put( pair.getFirst() * N + pair.getSecond(), row*N+col );

                    if( (horizontal && (row == N-1)) || (!horizontal && col == N-1) ) { // someone wins
                        int pathLength = 0;

                        int r = row, c = col;
                        Integer val = parentMap.getOrDefault(r*N + c,-1);
                        while (val != null && val != -1){
                            r = val/N;
                            c = val%N;
                            val = parentMap.getOrDefault(r*N + c,-1);
                            pathLength++;
                        }
                        score = Math.min(score, pathLength);
                        if(score >= N && score <= (N+1)) return score; // won with 5 or 6
                    }
//                    if( !horizontal && (col == N-1) ) return false; // connected
                    queue.add( new Pair<>(row,col) );

                    visited[row][col] = true;
                }
            }
        }
        if(score < N*N) return score;
        return -N; // current now, assuming N cells away from winning. |-N| will be lower
    }

}
