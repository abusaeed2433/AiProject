package com.unknownn.aiproject.classes;


import static com.unknownn.aiproject.classes.GameBoard.STROKE_WIDTH;

import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Hexagon {
    final Point leftTop, topMiddle,  rightTop,  rightBottom,  bottomMiddle,  leftBottom;
    private Path strokePath = null;
    private Path fillablePath = null;
    private final List<Point> points = new ArrayList<>();

    public Hexagon(Point leftTop, Point topMiddle, Point rightTop, Point rightBottom, Point bottomMiddle, Point leftBottom) {
        this.leftTop = leftTop;
        this.topMiddle = topMiddle;
        this.rightTop = rightTop;
        this.rightBottom = rightBottom;
        this.bottomMiddle = bottomMiddle;
        this.leftBottom = leftBottom;

        points.add(leftTop); points.add(topMiddle); points.add(rightTop);
        points.add(rightBottom); points.add(bottomMiddle); points.add(leftBottom);
    }

    public boolean contains(float x, float y){
        int numIntersections = 0;

        int n = points.size();
        for (int i = 0; i < n; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % n);

            if (y > Math.min(p1.y, p2.y) && y <= Math.max(p1.y, p2.y) && x <= Math.max(p1.x, p2.x)) {
                double xIntersection = (double) ((y - p1.y) * (p2.x - p1.x)) / (p2.y - p1.y) + p1.x;
                if (p1.x == p2.x || x <= xIntersection) {
                    numIntersections++;
                }
            }
        }
        return numIntersections % 2 == 1;
    }

    public Point getCenter(){
        return new Point(
                (leftTop.x + rightTop.x)/2,
                (topMiddle.y+bottomMiddle.y)/2
        );
    }

    public float getLeftVertLength(){
        return leftBottom.y - leftTop.y;
    }

    public Path getLeftVertPath(){
        final Path path = new Path();
        path.moveTo(leftBottom.x, leftBottom.y);
        path.lineTo(leftTop.x, leftTop.y);
        return path;
    }

    public Path getFillablePath(){
        if(fillablePath != null) return fillablePath;

        fillablePath = new Path();
        fillablePath.moveTo( leftTop.x + STROKE_WIDTH/2, leftTop.y );

        fillablePath.lineTo( topMiddle.x, topMiddle.y+STROKE_WIDTH/2 );

        fillablePath.lineTo( rightTop.x-STROKE_WIDTH/2, rightTop.y );
        fillablePath.lineTo( rightBottom.x-STROKE_WIDTH/2, rightBottom.y );

        fillablePath.lineTo( bottomMiddle.x, bottomMiddle.y-STROKE_WIDTH/2 );

        fillablePath.lineTo( leftBottom.x+STROKE_WIDTH/2, leftBottom.y );

        fillablePath.lineTo( leftTop.x+STROKE_WIDTH/2, leftTop.y );

        return fillablePath;
    }

    public Path getStrokePath(){
        if(strokePath != null) return strokePath;

        strokePath = new Path();
        strokePath.moveTo( leftTop.x, leftTop.y );

        strokePath.lineTo( topMiddle.x, topMiddle.y );
        strokePath.lineTo( rightTop.x, rightTop.y );

        strokePath.lineTo( rightBottom.x, rightBottom.y );
        strokePath.lineTo( bottomMiddle.x, bottomMiddle.y );
        strokePath.lineTo( leftBottom.x, leftBottom.y );

        strokePath.lineTo( leftTop.x, leftTop.y );

        return strokePath;
    }

}
