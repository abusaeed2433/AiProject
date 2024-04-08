package com.unknownn.aiproject.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GameBoard extends View {
    private static final long CLICK_DURATION = 300;
    private static final float STROKE_WIDTH = 8f;
    private static final int N = 11;
    private final CellState[][] states = new CellState[N][N];
    private final Paint gridBrush = new Paint();
    private final Paint cellBrush = new Paint();
    private float HEIGHT_PAD = 0, WIDTH_PAD = 0;
    private float CELL_WIDTH = 0, CELL_HEIGHT = 0;
    private long downTime = 0L;
    private boolean redTurn = true;


    public GameBoard(Context context) {
        super(context);
        if( isInEditMode() ) return;

        initBrush();
        initStates();
    }

    public GameBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBrush();
        initStates();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        for(int x = 0; x<=N; x++) {
            canvas.drawLine(
                    WIDTH_PAD,
                    HEIGHT_PAD + x * CELL_HEIGHT,
                    getWidth() - WIDTH_PAD,
                    HEIGHT_PAD + x * CELL_HEIGHT,
                    gridBrush
            );
        }

        for(int x = 0; x<=N; x++) {
            canvas.drawLine(
                    WIDTH_PAD + x*CELL_WIDTH,
                    HEIGHT_PAD,
                    WIDTH_PAD + x*CELL_WIDTH,
                    getHeight() - HEIGHT_PAD,
                    gridBrush
            );
        }

        for(CellState[] row : states){
            for(CellState cell : row){
                if(cell == null || cell.isBlank()) continue;

                cellBrush.setColor( cell.getBrushColor() );
                canvas.drawPath(cell.cellPath,cellBrush);
            }
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
        int r = (int) ( (x - WIDTH_PAD) / CELL_WIDTH );
        int c = (int) ( (y - HEIGHT_PAD) / CELL_HEIGHT );

        if(r < 0 || r >= N || c < 0 || c >= N) return;

        final CellState cell = states[r][c];
        if(!cell.isBlank()){
            System.out.println("Invalid cell");
            return;
        }

        cell.setMyColor( redTurn ? CellState.MyColor.RED : CellState.MyColor.BLUE );
        redTurn = !redTurn;
        invalidate();
    }

    private void initBrush(){
        final Paint[] brushes = new Paint[]{ gridBrush, cellBrush };

        for(Paint brush : brushes) {
            brush.setAntiAlias(true);
            brush.setColor(Color.BLACK);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeCap(Paint.Cap.ROUND);
            brush.setStrokeJoin(Paint.Join.ROUND);
            brush.setStrokeWidth(STROKE_WIDTH);
        }

        cellBrush.setColor(Color.GRAY);
        cellBrush.setStyle(Paint.Style.FILL);
    }

    private void initStates(){

        post(() -> {
            int width = getWidth();
            int height = getHeight();

            WIDTH_PAD = (int)(0.05f * width);
            HEIGHT_PAD = (int)(0.1f * height);

            width -= (int) (2*WIDTH_PAD);
            height -= (int) (2*HEIGHT_PAD);

            CELL_WIDTH = (float) width / N;
            CELL_HEIGHT = (float) height / N;

            for(int x = 0; x<N; x++){
                for(int y = 0; y<N; y++){
                    final int left = (int)(x * CELL_WIDTH + WIDTH_PAD + STROKE_WIDTH / 2 );
                    final int right = (int)(left + CELL_WIDTH - STROKE_WIDTH);

                    final int top = (int)(y * CELL_HEIGHT + HEIGHT_PAD + STROKE_WIDTH / 2);
                    final int bottom = (int)(top + CELL_HEIGHT - STROKE_WIDTH);

                    final Rect rect = new Rect(left,top,right,bottom);

                    final Path path = generatePath(rect);

                    states[x][y] = new CellState(x,y,rect, path);
                }
            }
            invalidate();
        });

    }

    private Path generatePath(Rect rect){
        final Path path = new Path();
        path.moveTo( rect.left, rect.top );

        path.lineTo( rect.right, rect.top );
        path.lineTo( rect.right, rect.bottom );
        path.lineTo( rect.left, rect.bottom );
        path.lineTo( rect.left, rect.top );

        return path;
    }



}
