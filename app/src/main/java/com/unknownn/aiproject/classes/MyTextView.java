package com.unknownn.aiproject.classes;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyTextView extends androidx.appcompat.widget.AppCompatTextView {

    private ValueAnimator animator = null;
    private float startPosition = 0f;
    private float endPosition = 0f;
    private boolean keepAnimating = false;
    private final Paint lineBrush = new Paint();
    private final Paint fillBrush = new Paint();

    public MyTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(isInEditMode()) return;
        initBrush();
    }

    public MyTextView(Context context) {
        super(context);
        if(isInEditMode()) return;
        initBrush();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!keepAnimating) {
            super.onDraw(canvas);
            return;
        }

        canvas.drawRect(startPosition,0f, endPosition, getHeight(), fillBrush);
        canvas.drawLine(startPosition, 0f, endPosition, 0f, lineBrush);
        canvas.drawLine(startPosition, getHeight(), endPosition, getHeight(), lineBrush);

        super.onDraw(canvas);
    }

    public void animateBackground(){
        post(() -> {
            keepAnimating = true;

            if(animator == null){
                animator = ValueAnimator.ofFloat(0f, 0.5f); // 1f for left to right, 0.5f for middle
                animator.addUpdateListener(valueAnimator -> {
                    final float percent = (float)valueAnimator.getAnimatedValue();

                    // from left to right expand
//                    startPosition = percent * getWidth();
//                    endPosition = Math.min(getWidth(), startPosition + length);
//                    startPosition = Math.max(startPosition, 0f);

                    // expand from middle
                    final int middle = getWidth()/2;
                    startPosition = middle - percent * getWidth();
                    endPosition = middle + getWidth() * percent;
                    invalidate();
                });
                animator.setDuration(700);

                final ValueAnimator alphaAnimator = ValueAnimator.ofInt(60,160);
                alphaAnimator.addUpdateListener(valueAnimator -> {
                    final int alpha = (int)valueAnimator.getAnimatedValue();
                    fillBrush.setColor(Color.argb(alpha, 100,120,110));
                    invalidate();
                });
                alphaAnimator.setDuration(3000);
                alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
                alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
                alphaAnimator.start();
            }
            animator.start();
        });
    }

    public void reset(){
        keepAnimating = false;
        invalidate();
    }

    private void initBrush(){
        final Paint[] brushes = new Paint[]{lineBrush, fillBrush};

        for(Paint brush : brushes) {
            brush.setAntiAlias(true);
            brush.setStyle(Paint.Style.STROKE);
            brush.setStrokeCap(Paint.Cap.ROUND);
            brush.setStrokeJoin(Paint.Join.ROUND);
            brush.setColor(Color.GREEN);
            brush.setStrokeWidth(8f);
        }
        lineBrush.setShader( new LinearGradient(0,0, 8f,8f, Color.BLACK, Color.GRAY, Shader.TileMode.MIRROR ) );
        fillBrush.setStyle(Paint.Style.FILL);
        fillBrush.setColor(Color.argb(180,222,224,252));
    }

}
