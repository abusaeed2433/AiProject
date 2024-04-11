package com.unknownn.aiproject.classes;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Pair;

public class GameBoard extends View {
    private static final long CLICK_DURATION = 300;
    public static final float STROKE_WIDTH = 4f;
    public static final float BOUNDARY_GAP = 12f;

    private static final int N = 3;
    private static final int N_N = N*N;
    private final CellState[][] states = new CellState[N][N];

    private final Paint gridBrush = new Paint();
    private final Paint blueBrush = new Paint();
    private final Paint redBrush = new Paint();
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

    // for whose move
    private float circleRadius = 0f;
    private final Point circleCenter = new Point(0,0);
    // for bot progress
    private final Point botProgressCenter = new Point(0,0);


    public GameBoard(Context context) {
        super(context);
        if( isInEditMode() ) return;

        initBrush();
        initStates();
    }

    public GameBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(isInEditMode()) return;

        initBrush();
        initStates();
    }

    public void setBoardListener(BoardListener boardListener) {
        this.boardListener = boardListener;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                CellState cell = states[i][j];
                if(cell == null) continue;

                canvas.drawPath( cell.getStrokePath(), gridBrush );

                Point pt = cell.getTextCenter();
                canvas.drawText(i+" "+j, pt.x,pt.y,textBrush);

                if(!cell.isBlank()) {
                    canvas.drawPath( cell.getFillablePath(), cell.isRed() ? redBrush : blueBrush );
                }
            }
        }

        // whose turn color
        if(circleCenter.x != 0) {
            canvas.drawCircle(circleCenter.x, circleCenter.y, circleRadius, redTurn ? redBrush : blueBrush);
        }

        // vertical boundary
        boundaryBrush.setColor(Color.RED);
        canvas.drawPath(vertPath, boundaryBrush);

        boundaryBrush.setColor(Color.BLUE);
        canvas.drawPath(horizPath, boundaryBrush);

        // bot progress
        if( botProgressInt != N_N ) {
            canvas.drawText(botProgressPercentStr, botProgressCenter.x, botProgressCenter.y, textBrush);
        }
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

    private void processClick(float x, float y){
        if( !redTurn ) {
            if(boardListener != null) boardListener.onMessageToShow("Wait for bot move");
            return;
        }

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


        if(!clickedCell.isBlank()){

            if(isTheFirstMove && !redTurn){
                clickedCell.setMyColor( CellState.MyColor.BLUE );
                redTurn = !redTurn;
                isTheFirstMove = false;
                invalidate();
                checkForGameOver(true);
                return;
            }

            if(boardListener != null) boardListener.onMessageToShow("Invalid cell");
            return;
        }

        clickedCell.setMyColor( redTurn ? CellState.MyColor.RED : CellState.MyColor.BLUE );
        redTurn = !redTurn;
        if(redTurn){
            isTheFirstMove = false;
        }
        invalidate();
        checkForGameOver(true);
    }

    private void initBrush(){
        final Paint[] brushes = new Paint[]{ gridBrush, redBrush, blueBrush, boundaryBrush };

        for(Paint brush : brushes) {
            brush.setAntiAlias(true);
            brush.setColor(Color.BLACK);
            brush.setStyle(Paint.Style.FILL);
            brush.setStrokeCap(Paint.Cap.ROUND);
            brush.setStrokeJoin(Paint.Join.ROUND);
            brush.setStrokeWidth(STROKE_WIDTH);
        }

        redBrush.setColor(Color.RED);
        blueBrush.setColor(Color.BLUE);

        gridBrush.setStyle(Paint.Style.STROKE);

        boundaryBrush.setStyle(Paint.Style.STROKE);
        boundaryBrush.setColor(Color.GRAY);
        boundaryBrush.setStrokeWidth(2*STROKE_WIDTH);

        // text brush
        textBrush.setColor(Color.BLACK);
        textBrush.setTextAlign(Paint.Align.CENTER);
        textBrush.setStyle(Paint.Style.FILL);
        textBrush.setTextSize(40f);
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

            HEIGHT_PAD = (height - (N*CELL_RECT_HEIGHT + 2*triangleHeight)) / 2f;

            for(int x = 0; x<N; x++){
                for(int y = 0; y<N; y++){

                    final Hexagon hexagon = getHexagon(x, y, triangleHeight);

                    states[x][y] = new CellState(x,y, hexagon);
                }
            }

            initWhoseMovePath();
            initTwoBoundaries();
            initBotProgressPoint();
            invalidate();
        });
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

    private void initWhoseMovePath(){
        int width = getWidth();
        int height = getHeight();

        float left = width * 0.9f;
        float right = width * 0.92f;
        float top = height * 0.1f;
        float bottom = height * 0.15f;

        float rad1 = right-left;
        float rad2 = bottom - top;

        circleRadius = Math.min(rad1,rad2);
        circleCenter.x = (int)( (left+right)/2f);
        circleCenter.y = (int)( (top+bottom)/2f );

        ValueAnimator animator = ValueAnimator.ofFloat(circleRadius*0.8f, circleRadius);
        animator.setDuration(1500L);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(valueAnimator -> {
            circleRadius = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.start();
    }
    private void initBotProgressPoint(){
        int width = getWidth();
        int height = getHeight();

        float x = width * 0.10f;
        float y = height * 0.90f;

        botProgressCenter.x = (int)x;
        botProgressCenter.y = (int)y;
    }

    private boolean isNotConnectedToEnd(CellState.MyColor[][] field, int x, int y,boolean horizontal){
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

    public void restart(){
        redTurn = true;
        isTheFirstMove = true;
        for(CellState[] row : states){
            for(CellState cell : row){
                cell.reset();
            }
        }
        invalidate();
    }

    private void checkForGameOver(boolean isUserMove){
        final CellState.MyColor[][] field = getField();
        final CellState.MyColor winner = getGameWinner(field);

        if( winner == null) { // predict next move is next is bot`s turn
            if( isUserMove ){ // true = current turn is user
                startPredicting();
            }
        }
        else { // game is finished
            if (boardListener != null) boardListener.onGameEnds(winner);
        }
    }

    private CellState.MyColor getGameWinner(CellState.MyColor[][] field){
        // left to right for Red
        for(int y=0; y<N; y++){
            if( field[0][y] != CellState.MyColor.RED ) continue;

            if(isNotConnectedToEnd(field,0,y, true)) continue;
            return CellState.MyColor.RED;
        }

        // top to bottom for Blue
        for(int x=0; x<N; x++){
            if( field[x][0] != CellState.MyColor.BLUE ) continue;

            if(isNotConnectedToEnd(field, x, 0, false)) continue;
            return CellState.MyColor.BLUE;
        }
        return null;
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

    private CellState.MyColor[][] getField(){
        final CellState.MyColor[][] field = new CellState.MyColor[N][N];
        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                field[x][y] = states[x][y].getMyColor();
            }
        }
        return field;
    }
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private void startPredicting(){
        service.execute(() -> {
            long startTime = System.currentTimeMillis();

            final CellState.MyColor[][] field = getField();
            Pair<Integer,Integer> xy = predictBotMove(field);

            int x = xy.getFirst();
            int y = xy.getSecond();

            long endTime = System.currentTimeMillis();
            long dif = (endTime - startTime);

            long delay = (dif > 1000) ? 0L : 500L;

            mHandler.postDelayed(() -> {
                states[x][y].setMyColor(CellState.MyColor.BLUE);
                redTurn = !redTurn;
                checkForGameOver(false);
                invalidate();
            }, delay);
        });
    }

    private int minimax(CellState.MyColor[][] field, int depth, final boolean isMax){
        int score = evaluate(field);

        if( score != 0 ) {
            return score; // someone wins
        }

        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                if( field[x][y] != CellState.MyColor.BLANK ) continue;

                if(isMax){ // user
                    field[x][y] = CellState.MyColor.RED;
                    int res = minimax(field,depth+1, false);
                    best = Math.max(best, res);
                }
                else { // bot
                    field[x][y] = CellState.MyColor.BLUE;
                    int res = minimax(field,depth+1, true);
                    best = Math.min(best, res);
                }

                field[x][y] = CellState.MyColor.BLANK;
            }
        }

        return best;
    }

    private int botProgressInt = N_N;
    private String botProgressPercentStr = "---";
    private Pair<Integer,Integer> predictBotMove(CellState.MyColor[][] field){
        botProgressInt = 0;
        int bestVal = Integer.MIN_VALUE;
        Pair<Integer, Integer> cellToPlace = null;

        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                if ( field[x][y] == CellState.MyColor.BLANK )
                {
                    field[x][y] =  CellState.MyColor.BLUE;
                    int moveVal = minimax(field,0, false);
                    field[x][y] = CellState.MyColor.BLANK;

                    if (moveVal > bestVal) {
                        cellToPlace = new Pair<>(x,y);
                        bestVal = moveVal;
                    }
                }
                botProgressInt++;
                botProgressPercentStr = (100 * botProgressInt) / N_N +"%";
                mHandler.post(this::invalidate);
            }
        }

        return cellToPlace;
    }

    private int evaluate(CellState.MyColor[][] field){
        CellState.MyColor winner = getGameWinner(field);
        if(winner == null){ // no winner
            return 0;
        }

        if( winner == CellState.MyColor.BLUE ){ // bot wins +10
            return 10;
        }

        return -10; // user wins -10
    }

    public interface BoardListener{
        void onMessageToShow(String message);
        void onGameEnds(CellState.MyColor winner);
    }

}
