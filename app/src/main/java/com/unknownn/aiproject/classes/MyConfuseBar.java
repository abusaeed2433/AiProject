package com.unknownn.aiproject.classes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyConfuseBar extends View {

    private final Paint paintBrush = new Paint();
    private final Paint textBrush = new Paint();
    private int angle = 0;
    private boolean isRunning = false;
    private String strProgress = "- - -";

    public MyConfuseBar(Context context) {
        super(context);
        if(isInEditMode()) return;
        initBrush();

    }

    public MyConfuseBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(isInEditMode()) return;
        initBrush();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if(isRunning) {
            canvas.drawArc(
                    getWidth()/2f - 0.3f*getHeight(),
                    8,
                    getWidth()/2f + 0.3f*getHeight(),
                    0.6f*getHeight() - 8,
                    angle,270,
                    false,
                    paintBrush
            );
        }
        canvas.drawText(strProgress, getWidth()/2f, (0.6f + 0.21f)*getHeight(), textBrush);
    }

    public void setStrProgress(String strProgress){
        this.strProgress = strProgress;
        invalidate();
    }

    private ValueAnimator animator = null;
    private long startTime = System.currentTimeMillis();
    public void startRotating(){
        startTime = System.currentTimeMillis();

        isRunning = true;
        angle = 0;
        paintBrush.setColor(Color.BLACK);

        if(animator == null) {
            animator = ValueAnimator.ofInt(0,360);
            animator.addUpdateListener(valueAnimator -> {
                angle = (int)valueAnimator.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationRepeat(animation);
                    long curTime = System.currentTimeMillis();

                    System.out.println("Difference is: "+(curTime-startTime));

                    if(curTime - startTime >= 8000){ // 8s
                        animator.setDuration(120);
                        paintBrush.setColor(Color.RED);
                    }
                    else if(curTime - startTime >= 4000){ // 4s
                        animator.setDuration(200);
                        paintBrush.setColor(Color.CYAN);
                    }
                    else if(curTime - startTime >= 2000){ // 2s
                        animator.setDuration(350);
                        paintBrush.setColor(Color.BLUE);
                    }

                }
            });
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setRepeatCount(ValueAnimator.INFINITE);
        }
        animator.setDuration(500);
        animator.start();
    }

    public void stopRotating(){
        animator.cancel();
        angle = 0;
        isRunning = false;
        invalidate();
    }

    private void initBrush(){
        paintBrush.setAntiAlias(true);
        paintBrush.setColor(Color.BLACK);
        paintBrush.setStyle(Paint.Style.STROKE);
        paintBrush.setStrokeCap(Paint.Cap.ROUND);
        paintBrush.setStrokeJoin(Paint.Join.ROUND);
        paintBrush.setStrokeWidth(6f);

        textBrush.setColor(Color.BLACK);
        textBrush.setTextAlign(Paint.Align.CENTER);
        textBrush.setStyle(Paint.Style.FILL);
        textBrush.setTextSize(32f);
    }

}
