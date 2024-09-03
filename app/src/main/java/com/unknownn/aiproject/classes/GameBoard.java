package com.unknownn.aiproject.classes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unknownn.aiproject.enums.PredictionAlgo;
import com.unknownn.aiproject.listener.AlphaBetaListener;
import com.unknownn.aiproject.listener.GeneticListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Pair;

public class GameBoard extends View {
    private static final long CLICK_DURATION = 300;
    private static final int NO_OF_INTERMEDIATE_PATHS = 20;
    public static final float STROKE_WIDTH = 4f;
    public static final float BOUNDARY_GAP = 12f;
    private boolean debugMode = true;
    private static int N = 5;
    private static int N_N = 25;

    private CellState[][] states = null;

    private final Paint gridBrush = new Paint();
    private final Paint blueBrush = new Paint();
    private final Paint redBrush = new Paint();
    private final Paint lightRedBrush = new Paint();
    private final Paint lightBlueBrush = new Paint();
    private final Paint textBrush = new Paint();

    private final Paint boundaryBrush = new Paint();

    private float HEIGHT_PAD = 0, WIDTH_PAD = 0;
    private float CELL_WIDTH = 0, CELL_HEIGHT = 0;
    private float CELL_RECT_HEIGHT = 0;
    private long downTime = 0L;
    private boolean redTurn = true;
    private BoardListener boardListener = null;
    private boolean isTheFirstMove = true;

    // horizontal and vertical boundary
    private final Path horizPath = new Path();
    private final Path vertPath = new Path();

    private PredictionAlgo predictionAlgo = PredictionAlgo.ALPHA_BETA_PRUNING;

    private Cell[][] selectedBoardFromGenetic = null;
    private Cell cellToAnimate = null;
    private final Path[] intermediatePaths = new Path[NO_OF_INTERMEDIATE_PATHS];
    private int animateIndex = 0;
    private boolean isReady = false;

    public GameBoard(Context context) {
        super(context);
//        initBrush();
//        initStates();
    }

    public GameBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setBoardListener(BoardListener boardListener) {
        this.boardListener = boardListener;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if(!isReady) return;


        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                CellState cell = states[i][j];
                if(cell == null) continue;

                canvas.drawPath( cell.getStrokePath(), gridBrush );

                if(debugMode) {
                    // todo comment to hide GA solution
                    if(selectedBoardFromGenetic[i][j] != null && selectedBoardFromGenetic[i][j].x == -1){
                        canvas.drawPath( cell.getFillablePath(), selectedBoardFromGenetic[i][j].isRed() ? lightRedBrush : lightBlueBrush );
                    }
                }

                if(!cell.isBlank()) { // draw or animate if first time
                    if(cell == cellToAnimate){
                        if(moveLine){
                            canvas.drawPath( leftVertPath, cell.isRed() ? redBrush : blueBrush );
                        }
                        else{
                            canvas.drawPath( intermediatePaths[animateIndex], cell.isRed() ? redBrush : blueBrush );
                        }
                    }
                    else{
                        canvas.drawPath( cell.getFillablePath(), cell.isRed() ? redBrush : blueBrush );
                    }
                }


                textBrush.setColor( cell.isBlank() ? Color.BLACK : Color.WHITE );

                // todo comment to hide cell number, Alpha Beta score and cell value
                if(debugMode) {
                    Point pt = cell.getTextCenter();
                    canvas.drawText(
                            (i +", "+ j) + (cell.score.isEmpty() ? cell.score : ": " + cell.score),
                            pt.x,
                            pt.y,
                            textBrush
                    );
                }
            }
        }

        // vertical boundary
        boundaryBrush.setColor(Color.RED);
        canvas.drawPath(vertPath, boundaryBrush);

        boundaryBrush.setColor(Color.BLUE);
        canvas.drawPath(horizPath, boundaryBrush);

        // bot progress
        textBrush.setColor( Color.BLACK );
//        canvas.drawText(botProgressPercentStr, botProgressCenter.x, botProgressCenter.y, textBrush);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                long upTime = System.currentTimeMillis();
                if( (upTime - downTime) <= CLICK_DURATION ){
                    processClick(x,y);
                }
                return true;
        }

        return false;
    }

    private static CellState lastClickedCell = null;
    private void processClick(float x, float y){
        CellState clickedCell = null;

        mainLoop:
        for(CellState[] row : states){
            for(CellState cell : row){
                if(cell.isPointInside(x,y)){
                    clickedCell = cell;
                    break mainLoop;
                }
            }
        }

        if(clickedCell == null) return;

        continueProcessing(clickedCell);
    }

    private void continueProcessing(CellState clickedCell){
        if( !redTurn ) {
            if(boardListener != null) boardListener.onMessageToShow("Wait for bot move");
            return;
        }


        if(!clickedCell.isBlank()){

            if(isTheFirstMove && !redTurn){
                clickedCell.setMyColor( CellState.MyColor.BLUE );
                lastClickedCell = clickedCell;

                redTurn = !redTurn;
                if(boardListener != null) boardListener.showWhoseMove(redTurn);
                isTheFirstMove = false;
                invalidate();
                checkForGameOver(true);
                return;
            }

            if(boardListener != null) boardListener.onMessageToShow("Invalid cell");
            return;
        }

        clickedCell.setMyColor( redTurn ? CellState.MyColor.RED : CellState.MyColor.BLUE );
        if(boardListener != null) {
            processForAnimation(clickedCell, true);
            boardListener.onSoundPlayRequest(SoundController.SoundType.MOVE_DONE);
        }

        redTurn = !redTurn;
        if(boardListener != null) boardListener.showWhoseMove(redTurn);
        if(redTurn){
            isTheFirstMove = false;
        }
//        invalidate();
//        checkForGameOver(true);
    }

    public void drawBoard(int n){
        isReady = true;

        N = n;
        N_N = n*n;

        states = new CellState[N][N];
        selectedBoardFromGenetic = new Cell[N][N];

        initBrush();
        initStates();
    }

    private void initBrush(){
        final Paint[] brushes = new Paint[]{ gridBrush, redBrush, lightRedBrush, blueBrush, lightBlueBrush, boundaryBrush };

        for(Paint brush : brushes) {
            brush.setAntiAlias(true);
            brush.setColor(Color.BLACK);
            brush.setStyle(Paint.Style.FILL);
            brush.setStrokeCap(Paint.Cap.ROUND);
            brush.setStrokeJoin(Paint.Join.ROUND);
            brush.setStrokeWidth(STROKE_WIDTH);
        }

        redBrush.setColor(Color.RED);
        lightRedBrush.setColor(Color.argb(30,120,20,20));

        blueBrush.setColor(Color.BLUE);
        lightBlueBrush.setColor(Color.argb(30,20,20,150));

        gridBrush.setStyle(Paint.Style.STROKE);

        boundaryBrush.setStyle(Paint.Style.STROKE);
        boundaryBrush.setColor(Color.GRAY);
        boundaryBrush.setStrokeWidth(2*STROKE_WIDTH);

        // text brush
        textBrush.setColor(Color.BLACK);
        textBrush.setTextAlign(Paint.Align.CENTER);
        textBrush.setStyle(Paint.Style.FILL);
        textBrush.setTextSize(20f);
    }

    private void initStates(){

        post(() -> {
            int width = getWidth();
            int height = getHeight();

            WIDTH_PAD = (int)(0.1f * width);
            HEIGHT_PAD = (int)(0.04f * height);

            width -= (int) (2*WIDTH_PAD);
            height -= (int) (2*HEIGHT_PAD);

            int half = N/2;
            CELL_WIDTH = (float) width / (N + half);
            CELL_HEIGHT = (float) height / N;

            //HEIGHT_PAD += (height - (N*CELL_HEIGHT)) / 2f;

            CELL_RECT_HEIGHT = CELL_HEIGHT * 0.6f;

            final int triangleHeight = (int)( (CELL_HEIGHT - CELL_RECT_HEIGHT)/2f );

            HEIGHT_PAD = (getHeight() - (N*CELL_RECT_HEIGHT + (N-1)*triangleHeight)) / 2f;

            for(int x = 0; x<N; x++){
                for(int y = 0; y<N; y++){

                    final Hexagon hexagon = getHexagon(x, y, triangleHeight);

                    states[x][y] = new CellState(x,y, hexagon);
                }
            }

            setUpAnimator();
            initTwoBoundaries();

            invalidate();
            isReady = true;
        });
    }

    private void stopAnimatorNow(){
        animateIndex = NO_OF_INTERMEDIATE_PATHS - 1;
        cellAnimator.cancel();

        if(lineAnimator != null) lineAnimator.cancel();
        // stop immediately
    }

    private final ValueAnimator cellAnimator = ValueAnimator.ofInt(0,NO_OF_INTERMEDIATE_PATHS-1);
    private ValueAnimator lineAnimator = null;
    private boolean isUserMove = false;
    private void setUpAnimator(){
        cellAnimator.setDuration(500);
        cellAnimator.addUpdateListener(valueAnimator -> {
            animateIndex = (int) valueAnimator.getAnimatedValue();
            invalidate();
        });
        cellAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                checkForGameOver(isUserMove);
                invalidate();
            }
        });
    }

    private void processForAnimation(final CellState cell, boolean isUserMove){
        this.cellToAnimate = cell;
        moveLine = true;
        createIntermediatePaths(cell);

        moveVertLine(cell, () -> {
            moveLine = false;
            animateIndex = 0;

            GameBoard.this.isUserMove = isUserMove;
            cellAnimator.start();
        });
    }

    private void moveVertLine(CellState cell, LineCallBack callBack){
        final Matrix shiftMatrix = new Matrix();
        float[] prevX = new float[]{states[0][cell.y].hexagon.leftTop.x};

        lineAnimator = ValueAnimator.ofFloat(states[0][cell.y].hexagon.leftTop.x+10, cell.hexagon.leftTop.x);
        lineAnimator.setDuration( cell.x * 200L);

        lineAnimator.addUpdateListener(valueAnimator -> {
            float position = (float)valueAnimator.getAnimatedValue();
            float dx = position - prevX[0];
            shiftMatrix.setTranslate(dx,0);
            leftVertPath.transform(shiftMatrix);

            prevX[0] = position;
            invalidate();
        });
        lineAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                callBack.onEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                callBack.onEnd();
            }
        });
        lineAnimator.start();
    }

    private interface LineCallBack{
        void onEnd();
    }

    private Path leftVertPath = new Path();
    private boolean moveLine = false;

    private void createIntermediatePaths(final CellState cell){
        final Path path = new Path(cell.getFillablePath());

        final PathMeasure pm = new PathMeasure(path,true);

        final float leftVertLength = cell.getLeftVertLength();

        final Point leftTop = cell.hexagon.leftTop;
        final Point leftBottom = cell.hexagon.leftBottom;
        final Point topMiddle = cell.hexagon.topMiddle;
        final Point bottomMiddle = cell.hexagon.bottomMiddle;

        final float topHorizLength = ( pm.getLength() - 2*leftVertLength ) / 2f;
        final float divLength = (topHorizLength / NO_OF_INTERMEDIATE_PATHS);

        final float[] point = new float[2];

        for(int i=1; i<=NO_OF_INTERMEDIATE_PATHS; i++){
            final Path subPath = new Path();
            subPath.moveTo(leftBottom.x, leftBottom.y);
            subPath.lineTo(leftTop.x, leftTop.y);

            float dist = divLength * i;
            pm.getPosTan(dist, point, null);

            if(point[0] > topMiddle.x) { subPath.lineTo(topMiddle.x, topMiddle.y); }

            subPath.lineTo(point[0], point[1]);

            dist = pm.getLength() - leftVertLength - dist;
            pm.getPosTan(dist, point, null);
            subPath.lineTo(point[0], point[1]);

            if(point[0] > bottomMiddle.x) { subPath.lineTo(bottomMiddle.x, bottomMiddle.y); }

            subPath.lineTo(leftBottom.x, leftBottom.y);

            intermediatePaths[i-1] = subPath;
        }

        leftVertPath = new Path();

        final Hexagon hex = states[0][cell.y].hexagon;
        leftVertPath.moveTo(hex.leftTop.x, hex.leftTop.y);
        leftVertPath.lineTo(hex.leftTop.x+10, hex.leftTop.y);

        leftVertPath.lineTo(hex.leftTop.x+10, hex.leftBottom.y);
        leftVertPath.lineTo(hex.leftTop.x, hex.leftBottom.y);
        leftVertPath.lineTo(hex.leftTop.x, hex.leftTop.y);
        invalidate();
    }

    private void initTwoBoundaries(){

        vertPath.reset();
        horizPath.reset();

        vertPath.moveTo( states[0][0].hexagon.leftTop.x - BOUNDARY_GAP, states[0][0].hexagon.leftTop.y );
        horizPath.moveTo( states[0][0].hexagon.leftTop.x, states[0][0].hexagon.leftTop.y - BOUNDARY_GAP);

        // left and top
        for(int y=0; y<N; y++){
            CellState vertCell = states[0][y];

            Point point1, point2;
            point1 = vertCell.hexagon.leftBottom;
            point2 = vertCell.hexagon.bottomMiddle;

            vertPath.lineTo(point1.x - BOUNDARY_GAP, point1.y + BOUNDARY_GAP/2);
            if(y != N-1) {
                vertPath.lineTo(point2.x - BOUNDARY_GAP, point2.y + BOUNDARY_GAP/2);
            }

            // for horiz
            CellState horizCell = states[y][0];
            point1 = horizCell.hexagon.topMiddle;
            point2 = horizCell.hexagon.rightTop;
            horizPath.lineTo(point1.x, point1.y - BOUNDARY_GAP);
            horizPath.lineTo(point2.x, point2.y - BOUNDARY_GAP);
        }

        // right and bottom
        vertPath.moveTo( states[N-1][0].hexagon.rightTop.x + BOUNDARY_GAP, states[N-1][0].hexagon.rightTop.y );
        horizPath.moveTo( states[0][N-1].hexagon.leftBottom.x, states[0][N-1].hexagon.leftBottom.y + BOUNDARY_GAP );

        for(int y=0; y<N; y++){
            CellState vertCell = states[N-1][y];

            Point point1, point2;
            point1 = vertCell.hexagon.rightTop;
            point2 = vertCell.hexagon.rightBottom;

            vertPath.lineTo(point1.x + BOUNDARY_GAP, point1.y - BOUNDARY_GAP/2);
            vertPath.lineTo(point2.x + BOUNDARY_GAP, point2.y - BOUNDARY_GAP/2);

            // for horizontal
            CellState horizCell = states[y][N-1];
            point1 = horizCell.hexagon.bottomMiddle;
            point2 = horizCell.hexagon.rightBottom;
            horizPath.lineTo(point1.x, point1.y + BOUNDARY_GAP);
            horizPath.lineTo(point2.x, point2.y + BOUNDARY_GAP);
        }
    }

    private PredictionAlgo fixedPredictionAlgo = null;
    public void fixPredictionAlgo(PredictionAlgo algo){
        this.fixedPredictionAlgo = algo;
        this.predictionAlgo = algo;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        invalidate();
    }

    public void restart(){
        redTurn = true;
        if(boardListener != null) boardListener.showWhoseMove(redTurn);
        isTheFirstMove = true;
        for(CellState[] row : states){
            for(CellState cell : row){
                cell.reset();
            }
        }

        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                selectedBoardFromGenetic[i][j] = null;
            }
        }

        invalidate();
    }

    private void setPredictionAlgo(PredictionAlgo algo, boolean showToast){
        mHandler.post(() -> {
            if(boardListener != null) boardListener.onAlgoChanged(showToast);
        });
        this.predictionAlgo = algo;
    }

    public void swapPredictionAlgo(boolean requestedByBot, boolean showToast){
        if( !redTurn && !requestedByBot) {
            if(boardListener != null) boardListener.onMessageToShow("Be patient");
            return;
        }

        predictionAlgo = (predictionAlgo == PredictionAlgo.ALPHA_BETA_PRUNING) ?
                PredictionAlgo.GENETIC_ALGO : PredictionAlgo.ALPHA_BETA_PRUNING;

        if(boardListener != null) boardListener.onAlgoChanged(showToast);
    }

    public PredictionAlgo getPredictionAlgo() {
        return predictionAlgo;
    }

    private void checkForGameOver(boolean isUserMove){
        final CellState.MyColor[][] field = getCurrentBoard();
        final CellState.MyColor winner = Calculator.getGameWinner(field,N);

        if( winner == null) { // predict next move is bot`s turn
            if( isUserMove ){ // true = current turn was user's turn
                startPredicting(null);
            }
        }
        else { // game is finished
            if (boardListener != null) {
                stopAnimatorNow();
                boardListener.onGameEnds(winner);
                boardListener.onSoundPlayRequest(SoundController.SoundType.GAME_OVER);
            }
        }
    }

    private Hexagon getHexagon(int x, int y, int triangleHeight) {
        int leftPadding = (int) ( WIDTH_PAD + (y*CELL_WIDTH/2f) );
        final int left = (int)(x * CELL_WIDTH + leftPadding);
        final int right = (int)(left + CELL_WIDTH);

        final int top = (int)(y * CELL_HEIGHT + HEIGHT_PAD - y * triangleHeight - (y == 0 ? 0 : STROKE_WIDTH/2) );
        final int bottom = (int)(top + CELL_RECT_HEIGHT);

        Point leftTop = new Point(left,top);
        Point rightTop = new Point(right,top);

        int midX = (left + right)/2;
        Point topMiddle = new Point(midX,top - triangleHeight);

        Point rightBottom = new Point(right,bottom);
        Point leftBottom = new Point(left,bottom);
        Point bottomMiddle = new Point(midX, bottom+triangleHeight);

        return new Hexagon(leftTop, topMiddle, rightTop, rightBottom, bottomMiddle, leftBottom);
    }

    //private PreListener preListener;
    private int x, y;
    public void preCalc(){
        x = y = 0;

        PreListener preListener = new PreListener() {
            @Override
            public void onEnd() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restart();
                        System.out.println("Running on "+x+" "+y);
                        y++;
                        if(y == N){
                            y = 0;
                            x++;
                        }
                        if(x >= N && y >= N) return;
                        startPre(x,y);
                    }
                },2000L);
            }
        };
        startPre(0,0);
    }
    private void startPre(int x, int y){
        if(x >= N || y >= N) {
            //preListener.onEnd();
            return;
        };

        continueProcessing(states[x][y]);
    }
    private interface PreListener{
        void onEnd();
    }

    private CellState.MyColor[][] getCurrentBoard(){
        final CellState.MyColor[][] field = new CellState.MyColor[N][N];
        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                field[x][y] = states[y][x].getMyColor(); // y,x at the second is correct
            }
        }
        return field;
    }

    private int countEmptyCell(){
        int count = 0;
        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                if(states[y][x].isBlank()) count++;
            }
        }
        return count;
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());
//    private String botProgressPercentStr = "---";
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private int prevTimeTaken = 30; // in seconds
    private void startPredicting(PredictionAlgo reqAlgo){ // will use algo without any check if not null
        if(boardListener != null) boardListener.onProgressBarUpdate(true);

        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                states[i][j].score = ""; // empty
                selectedBoardFromGenetic[i][j] = null;
            }
        }

        service.execute(() -> {
            long startTime = System.currentTimeMillis();

            PredictionAlgo algo = reqAlgo;
            if(reqAlgo == null) {
                algo = FuzzyApplier
                        .getInstance()
                        .predictAlgo(N_N, countEmptyCell(), prevTimeTaken);

                if(fixedPredictionAlgo != null) {
                    algo = fixedPredictionAlgo;
                }
            }

            setPredictionAlgo(algo,false);

            Pair<Integer,Integer> xy = (predictionAlgo == PredictionAlgo.ALPHA_BETA_PRUNING )
                    ? predictByAlphaBeta() : predictByGeneticAlgo();

            if(xy == null) return;

            int x = xy.getFirst();
            int y = xy.getSecond();

            long endTime = System.currentTimeMillis();
            long dif = (endTime - startTime);

            prevTimeTaken = (prevTimeTaken + (int)(dif/1000)) / 2; // average so that it decreases slowly

            System.out.println("Actual time taken: "+dif);
//            botProgressPercentStr = "Took: "+ dif +"ms";
            mHandler.post(() ->{
                if(boardListener != null) {
                    boardListener.onProgressBarUpdate(false);
                    boardListener.onProgressUpdate("Took: "+ dif +"ms");
                }
            });

            long delay = (dif > 2000) ? 0L : (2000L - dif); // will take at least 2 seconds

            mHandler.postDelayed(() -> {
                states[x][y].setMyColor(CellState.MyColor.BLUE);
                processForAnimation(states[x][y], false);

                redTurn = !redTurn;
                if(boardListener != null) {
                    boardListener.onSoundPlayRequest(SoundController.SoundType.MOVE_DONE);
                    //preListener.onEnd();

                    boardListener.showWhoseMove(redTurn);
                }
            }, delay);
        });
    }

    @SuppressWarnings("unchecked")
    private Pair<Integer,Integer> predictByAlphaBeta(){

        final CompletableFuture<Pair<Integer, Integer>> futureResult = new CompletableFuture<>();
        Pair<Integer, Integer>[] posToPlace = new Pair[1];
        posToPlace[0] = null;

        final CellState.MyColor[][] currentBoard = getCurrentBoard();
        AlphaBetaApplier.getInstance()
                .setAlphaBetaListener(new AlphaBetaListener() {
                    @Override
                    public void onProgress(int progress) {
//                        botProgressPercentStr = progress +"%";
                        mHandler.post(() ->{
                            if(boardListener != null) boardListener.onProgressUpdate(progress +"%");
                        });
                    }

                    @Override
                    public void onError(String message, boolean changeToGenetic) {
                        mHandler.post(() -> {
                            if(!changeToGenetic && boardListener != null) boardListener.onMessageToShow(message);
                            if(changeToGenetic) {
                                futureResult.complete(null);
                                swapPredictionAlgo(true,true);
                                startPredicting(PredictionAlgo.GENETIC_ALGO);
                            }
                        });
                    }

                    @Override
                    public void onCellValueUpdated(int x, int y, int moveVal) {
                        states[y][x].score = moveVal+""; // y,x correct
                        mHandler.post(() -> invalidate());
                    }

                    @Override
                    public void onFinished(Pair<Integer, Integer> xy) {
                        if(xy == null){
                            futureResult.complete(null);
                            return;
                        }

                        Pair<Integer,Integer> yx = new Pair<>(xy.getSecond(), xy.getFirst()); // must

                        posToPlace[0] = yx;
                        futureResult.complete(yx);
                    }
                })
                .predict(currentBoard,N, lastClickedCell, (fixedPredictionAlgo != null));

        try {
            futureResult.get();
        } catch (InterruptedException | ExecutionException ignored){}

        return posToPlace[0];
    }

    @SuppressWarnings("unchecked")
    private Pair<Integer, Integer> predictByGeneticAlgo(){
        final CompletableFuture<Pair<Integer, Integer>> futureResult = new CompletableFuture<>();
        Pair<Integer, Integer>[] posToPlace = new Pair[1];
        posToPlace[0] = null;

        final CellState.MyColor[][] currentBoard = getCurrentBoard();
        GeneticApplier.getInstance()
                .setGeneticListener(new GeneticListener() {
                    @Override
                    public void onProgress(int progress) {
//                        botProgressPercentStr = progress +"%";
                        mHandler.post(() ->{
                            if(boardListener != null) boardListener.onProgressUpdate(progress +"%");
                        });
                    }

                    @Override
                    public void onError(String message, boolean changeToAlphaBeta) {
                        mHandler.post(() -> {
                            if(boardListener != null) boardListener.onMessageToShow(message);
                            if(changeToAlphaBeta) {
                                futureResult.complete(null);
                                swapPredictionAlgo(true, true);
                                startPredicting(PredictionAlgo.ALPHA_BETA_PRUNING);
                            }
                        });
                    }

                    @Override
                    public void onDrawRequest(List<Cell> selectedBoard) {
                        for(Cell cell : selectedBoard){
                            selectedBoardFromGenetic[cell.y][cell.x] = new Cell(-1,-1,cell.myColor);
                        }
                        mHandler.post(() ->
                                invalidate()
                        );
                    }

                    @Override
                    public void onFinished(Pair<Integer, Integer> xy) {
                        if(xy == null){
                            futureResult.complete(null);
                            return;
                        }
                        Pair<Integer,Integer> yx = new Pair<>(xy.getSecond(), xy.getFirst()); // must

                        posToPlace[0] = yx;
                        futureResult.complete(yx);
                    }
                })
                .predict(N,currentBoard, lastClickedCell);

        try {
            futureResult.get();
        } catch (InterruptedException | ExecutionException ignored){}

        return posToPlace[0];
    }

    public interface BoardListener{
        void onMessageToShow(String message);
        void onAlgoChanged(boolean showToast);
        void showWhoseMove(boolean userMove);
        void onGameEnds(CellState.MyColor winner);
        void onSoundPlayRequest(SoundController.SoundType soundType);
        void onProgressBarUpdate(boolean show);
        void onProgressUpdate(String strProgress);
    }

}
