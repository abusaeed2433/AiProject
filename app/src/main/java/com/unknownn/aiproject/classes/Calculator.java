package com.unknownn.aiproject.classes;

import java.util.LinkedList;
import java.util.Queue;

import kotlin.Pair;

public class Calculator {
    public static final int WIN = 100, LOSS = -100;

    public static CellState.MyColor getGameWinner(CellState.MyColor[][] field, int N){
        // left to right for Red
        for(int y=0; y<N; y++){
            if( field[0][y] != CellState.MyColor.RED ) continue;

            if(isNotConnectedToEnd(field,N,0,y, true)) continue;
            return CellState.MyColor.RED;
        }

        // top to bottom for Blue
        for(int x=0; x<N; x++){
            if( field[x][0] != CellState.MyColor.BLUE ) continue;

            if(isNotConnectedToEnd(field, N, x, 0, false)) continue;
            return CellState.MyColor.BLUE;
        }
        return null;
    }

    public static boolean isNotConnectedToEnd(CellState.MyColor[][] field, int N, int x, int y,boolean horizontal){
        if(field[x][y] == CellState.MyColor.BLANK) return true;

        final Queue<Pair<Integer,Integer>> queue = new LinkedList<>();

        queue.add( new Pair<>(x,y) );
        final boolean[][] visited = new boolean[N][N];
        visited[x][y] = true;

        while ( !queue.isEmpty() ){
            Pair<Integer,Integer> pair = queue.poll();
            if(pair == null) continue;

            final int[][] offsets = { {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1},{0, 1} };

            for(int[] offset : offsets){
                int row = pair.getFirst() + offset[0];
                int col = pair.getSecond() + offset[1];

                if(row < 0 || row >= N || col < 0 || col >= N) continue;

                if( field[pair.getFirst()][pair.getSecond()] == field[row][col] && !visited[row][col] ){

                    if( horizontal && (row == N-1) ) return false;

                    if( !horizontal && (col == N-1) ) return false;
                    queue.add( new Pair<>(row,col) );
                    visited[row][col] = true;
                }
            }
        }
        return true;
    }
}
