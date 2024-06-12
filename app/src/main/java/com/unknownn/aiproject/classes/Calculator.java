package com.unknownn.aiproject.classes;

import static com.unknownn.aiproject.classes.CellState.MyColor.BLANK;
import static com.unknownn.aiproject.classes.CellState.MyColor.BLUE;
import static com.unknownn.aiproject.classes.CellState.MyColor.RED;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import kotlin.Pair;

public class Calculator {
    public static final int WIN = 100, LOSS = -100;
    public static final int NO_WIN = 100;
    private static final int PATH_LENGTH_WEIGHT = 10;
    private static final int MOBILITY_WEIGHT = 5;


    public static int getBoardScore(CellState.MyColor[][] board, int N){

        int blueScore = 0;
        int redScore = 0;

        final Pair<Integer,Integer> pathLengths = getExpectedLongestPathBlueRed(board, N);

        //System.out.println(pathLengths.getFirst()+" "+pathLengths.getSecond());

        blueScore += pathLengths.getFirst()*PATH_LENGTH_WEIGHT;
        redScore += pathLengths.getSecond()*PATH_LENGTH_WEIGHT;

        final Pair<Integer,Integer> mobilities = calcMobilityBlueRed(board, N);
        //System.out.println(mobilities.getFirst()+" "+mobilities.getSecond());

        blueScore += mobilities.getFirst() * MOBILITY_WEIGHT;
        redScore += mobilities.getSecond() * MOBILITY_WEIGHT;

        //System.out.println(blueScore+" "+redScore);

        return blueScore - redScore;
    }

    public static CellState.MyColor getGameWinner(CellState.MyColor[][] field, int N){
        final Pair<Integer,Integer> scores = getBoardScoreOld(field, N, true);

        if(scores.getFirst() == N){ // Red won
            return CellState.MyColor.RED;
        }

        if(scores.getSecond() == N){
            return CellState.MyColor.BLUE;
        }
        return null;
    }

    private static int spreadThisPath(CellState.MyColor[][] board, int x, int y, boolean[][] visited, int N){
        int left = x, top = y;
        int right = x, bottom = y;

        final int[][] offsets = { {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1},{0, 1} };

        final Queue<Pair<Integer,Integer>> queue = new LinkedList<>();
        queue.add(new Pair<>(x,y));
        visited[x][y] = true;

        while (!queue.isEmpty()){
            final Pair<Integer,Integer> pair = queue.poll();

            assert pair != null;
            final int oldX = pair.getFirst();
            final int oldY = pair.getSecond();

            for(int[] off : offsets){
                final int newX = oldX + off[0];
                final int newY = oldY + off[1];

                if(newX < 0 || newX >= N || newY < 0 || newY >= N) continue;
                if(visited[newX][newY]) continue;
                if(board[newX][newY] != board[x][y]) continue;

                visited[newX][newY] = true;

                left = Math.min(left, newY); // x is height. It's ok
                right = Math.max(right, newY);

                top = Math.min(top, newX); // y is width and it's fine
                bottom = Math.max(bottom, newX);

                queue.add(new Pair<>(newX,newY));
            }
        }

        if(board[x][y] == BLUE){ // vertical
            return bottom - top +1;
        }
        return right - left +1; // for Red
    }

    private static Pair<Integer,Integer> getExpectedLongestPathBlueRed(CellState.MyColor[][] board, int N){
        final boolean[][] visited = new boolean[N][N];

        int horizDisplacement = 0;
        int vertDisplacement = 0;

        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                if(board[x][y] == BLANK || visited[x][y]) continue;

                int length = spreadThisPath(board, x,y,visited,N);

                if(board[x][y] == BLUE){ // bot -> top to bottom path
                    vertDisplacement = Math.max(vertDisplacement, length);
                }
                else{ // RED
                    horizDisplacement = Math.max(horizDisplacement, length);
                }

            }
        }

        return new Pair<>(vertDisplacement, horizDisplacement); // BLUE, RED
    }

    private static Pair<Integer,Integer> calcMobilityBlueRed(CellState.MyColor[][] board, int N){

        int blueMobility = 0;
        int redMobility = 0;

        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                if(board[x][y] != BLANK) continue;

                final int[][] offsets = { {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1},{0, 1} };

                int blue = 0, red = 0;
                for(int[] off : offsets){
                    final int newX = x + off[0];
                    final int newY = y + off[1];

                    if(newX < 0 || newX >= N || newY < 0 || newY >= N) continue;

                    if(board[newX][newY] == RED) red = 1;
                    else if(board[newX][newY] == BLUE) blue = 1;

                    if(red + blue == 2) break;
                }

                blueMobility += blue;
                redMobility += red;
            }
        }

        // the less, the better
        final int max = Math.max(blueMobility, redMobility);
        return new Pair<>(max - blueMobility, max - redMobility); // the more, the better since subtracted
    }


    private static Pair<Integer,Integer> getBoardScoreOld(CellState.MyColor[][] field, int N, boolean noOptimization){
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
        if(field[x][y] == BLANK) return 0;

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
