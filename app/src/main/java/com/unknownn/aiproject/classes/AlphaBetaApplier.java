package com.unknownn.aiproject.classes;

import static com.unknownn.aiproject.classes.Calculator.LOSS;
import static com.unknownn.aiproject.classes.Calculator.WIN;

import com.unknownn.aiproject.listener.AlphaBetaListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.Pair;

public class AlphaBetaApplier {

    private int N;
    private int N_N;
    private int DEPTH_LIMIT;

    private AlphaBetaListener alphaBetaListener = null;

    private ExecutorService services = null;

    private static AlphaBetaApplier instance = null;
    public static AlphaBetaApplier getInstance(){
        if(instance == null) {
            instance = new AlphaBetaApplier();
        }
        return instance;
    }

    private AlphaBetaApplier() {}

    public AlphaBetaApplier setAlphaBetaListener(AlphaBetaListener alphaBetaListener) {
        this.alphaBetaListener = alphaBetaListener;
        return this;
    }

    private final AtomicInteger botProgressInt = new AtomicInteger(Integer.MIN_VALUE);
    public void predict(final CellState.MyColor[][] fieldItOnly, int N){
        this.N = N;
        this.N_N = N*N;

        if(services == null){
            services = Executors.newFixedThreadPool(N*N);
        }

        AtomicInteger bestVal = new AtomicInteger(Integer.MIN_VALUE);
        AtomicReference<Pair<Integer,Integer>> cellToPlace = new AtomicReference<>(null);

        DEPTH_LIMIT = predictDepthLimit(fieldItOnly);

        final List<Future<?>> futures = new ArrayList<>();

        for(int x = 0; x<N; x++){
            for(int y = 0; y<N; y++){
                if ( fieldItOnly[x][y] != CellState.MyColor.BLANK ){
                    botProgressInt.incrementAndGet();
                    int progress = (100 * botProgressInt.get()) / N_N;
                    alphaBetaListener.onProgress(progress);
                    continue;
                }

                final Future<?> future = submitToThread(fieldItOnly, x,y, bestVal, cellToPlace);
                futures.add(future);
            }
        }

        for(Future<?> future : futures) {
            try{
                future.get();
            }catch (InterruptedException | ExecutionException ignored){}
        }

        alphaBetaListener.onFinished(cellToPlace.get());
    }

    private int applyAlphaBeta(CellState.MyColor[][] field, int depth, final boolean isMax, int alpha, int beta){
        int score = evaluate(field);

        if( score == WIN || score == LOSS ) {
            return score; // someone wins
        }

        if( depth >= DEPTH_LIMIT ) return 0;

        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        mainLoop:
        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                if( field[x][y] != CellState.MyColor.BLANK ) continue;

                if(isMax){ // bot
                    field[x][y] = CellState.MyColor.BLUE;
                    int res = applyAlphaBeta(field,depth+1, false, alpha, beta);
                    best = Math.max(best, res);
                    alpha = Math.max( alpha, best);
                    field[x][y] = CellState.MyColor.BLANK;
                    if(beta <= alpha) break mainLoop;
                }
                else{ // user
                    field[x][y] = CellState.MyColor.RED;
                    int res = applyAlphaBeta(field,depth+1, true,alpha,beta);
                    best = Math.min(best, res);

                    beta = Math.min( beta, best);
                    field[x][y] = CellState.MyColor.BLANK;
                    if(beta <= alpha) break mainLoop;
                }

            }
        }
        return best;
    }

    private Future<?> submitToThread(
            final CellState.MyColor[][] fieldItOnly, final int x, final int y,
            AtomicInteger bestVal, AtomicReference<Pair<Integer,Integer>> cellToPlace){

        return services.submit(()->{
            System.out.println("Depth limit: "+DEPTH_LIMIT);

            final CellState.MyColor[][] field = new CellState.MyColor[N][N];
            for(int i=0; i<N; i++){
                System.arraycopy(fieldItOnly[i], 0, field[i], 0, N);
            }

            field[x][y] = CellState.MyColor.BLUE;
            int moveVal = applyAlphaBeta(field,0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            field[x][y] = CellState.MyColor.BLANK;

            System.out.println(moveVal);

            if (moveVal > bestVal.get()) {
                cellToPlace.set( new Pair<>(x,y) );
                bestVal.set( moveVal );
            }
            else if( moveVal == bestVal.get() && cellToPlace.get() != null ){
                final Pair<Integer,Integer> prev = cellToPlace.get();

                int prevVal = prev.getFirst() * N + prev.getSecond();
                int curVal = x * N + y;

                if( curVal < prevVal ){ // current one is earlier, so better?
                    cellToPlace.set( new Pair<>(x,y) );
                }
            }

            botProgressInt.incrementAndGet();
            alphaBetaListener.onProgress( (100 * botProgressInt.get()) / N_N );

            alphaBetaListener.onCellValueUpdated(x,y,moveVal);
        });
    }

    private int predictDepthLimit(CellState.MyColor[][] field){
        int emptyCount = 0;
        for(CellState.MyColor[] col : field){
            for(CellState.MyColor item : col){
                if(item == CellState.MyColor.BLANK) emptyCount++;
            }
        }

        int emptyPercent = (100 * emptyCount) / N_N;

        if(emptyPercent > 70) return N; // early

        if(emptyPercent > 50) return N+3; // medium

        return N_N; // critical
    }

    private int evaluate(CellState.MyColor[][] field){
        final CellState.MyColor winner = Calculator.getGameWinner(field,N);

        if( winner == CellState.MyColor.BLUE ) return WIN;

        if( winner == CellState.MyColor.RED ) return LOSS;

        return 0;
    }

}
