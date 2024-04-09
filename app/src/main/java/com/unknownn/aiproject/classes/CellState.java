package com.unknownn.aiproject.classes;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;

public class CellState {
    final int x,y;
    private MyColor myColor;
    private final Hexagon hexagon;

    public CellState(int x, int y, Hexagon hexagon) {
        this.x = x;
        this.y = y;
        this.hexagon = hexagon;
        this.myColor = MyColor.BLANK;
    }

    public void setMyColor(MyColor color){
        this.myColor = color;
    }

    public boolean isBlank(){
        return myColor == MyColor.BLANK;
    }
    public int getBrushColor(){
        if(myColor == MyColor.RED) return Color.RED;
        return Color.BLUE;
    }

    public boolean isPointInside(float x, float y){
        return hexagon.contains(x,y);
    }

    public Path getStrokePath() {
        return hexagon.getStrokePath();
    }

    public Point getTextCenter(){
        return hexagon.getCenter();
    }
    public Path getFillablePath(){
        return hexagon.getFillablePath();
    }

    public enum MyColor { RED, BLUE, BLANK }
}
