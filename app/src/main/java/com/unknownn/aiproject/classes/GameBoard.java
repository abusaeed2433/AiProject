package com.unknownn.aiproject.classes;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class GameBoard extends View {
    private static final long CLICK_DURATION = 300;
    public static final float STROKE_WIDTH = 4f;
    private static final int N = 7;
    private final CellState[][] states = new CellState[N][N];
    private final Paint gridBrush = new Paint();
    private final Paint blueBrush = new Paint();
    private final Paint redBrush = new Paint();
    private final Paint textBrush = new Paint();
    private float HEIGHT_PAD = 0, WIDTH_PAD = 0;
    private float CELL_WIDTH = 0, CELL_HEIGHT = 0;
    private float CELL_RECT_HEIGHT = 0;
    private long downTime = 0L;
    private boolean redTurn = true;
    private BoardListener boardListener = null;
    private boolean isTheFirstMove = true;

    // for whose move
    private float circleRadius = 0f;
    private final Point circleCenter = new Point(0,0);

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
                checkForGameOver();
                return;
            }

            if(boardListener != null) boardListener.onMessageToShow("Invalid cell");
            return;
        }

        clickedCell.setMyColor( redTurn ? CellState.MyColor.RED : CellState.MyColor.BLUE );
        redTurn = !redTurn;
        invalidate();
        checkForGameOver();
    }

    private void initBrush(){
        final Paint[] brushes = new Paint[]{ gridBrush, redBrush, blueBrush };

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

            HEIGHT_PAD = (height - (N*CELL_RECT_HEIGHT + 2*triangleHeight)) / 2f;

            for(int x = 0; x<N; x++){
                for(int y = 0; y<N; y++){

                    final Hexagon hexagon = getHexagon(x, y, triangleHeight);

                    states[x][y] = new CellState(x,y, hexagon);
                }
            }

            initWhoseMovePath();
            invalidate();
        });
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

    private boolean isNotConnectedToEnd(CellState startCell, boolean horizontal){
        if(startCell.isBlank()) return true;

        final Queue<CellState> queue = new LinkedList<>();

        queue.add(startCell);
        final boolean[][] visited = new boolean[N][N];

        while ( !queue.isEmpty() ){
            CellState curCell = queue.poll();
            if(curCell == null) continue;

            visited[curCell.x][curCell.y] = true;

            final int[][] offsets = { {0, -1}, {1, -1}, {-1, 0}, {1, 0}, {-1, 1},{0, 1} };

            for(int[] offset : offsets){
                int row = curCell.x + offset[0];
                int col = curCell.y + offset[1];

                if(row < 0 || row >= N || col < 0 || col >= N) continue;

                final CellState adjCell = states[row][col];
                if( curCell.getMyColor() == adjCell.getMyColor() && !visited[adjCell.x][adjCell.y] ){

                    if( horizontal && (adjCell.x == N-1) ) return false;

                    if( !horizontal && (adjCell.y == N-1) ) return false;
                    queue.add(adjCell);
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

    private void checkForGameOver(){

        // left to right
        for(int y=0; y<N; y++){
            CellState startCell = states[0][y];
            if(isNotConnectedToEnd(startCell, true)) continue;
            if( boardListener != null) boardListener.onGameEnds(startCell.getMyColor());
            break;
        }

        // top to bottom
        for(int y=0; y<N; y++){
            CellState startCell = states[y][0];
            if(isNotConnectedToEnd(startCell, false)) continue;
            if( boardListener != null) boardListener.onGameEnds(startCell.getMyColor());
            break;
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

    public interface BoardListener{
        void onMessageToShow(String message);
        void onGameEnds(CellState.MyColor winner);
    }

}
