package com.unknownn.aiproject.classes;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;

public class CellState {
    final int x,y;
    private MyColor myColor;
    private final Rect boundary;
    final Path cellPath;

    public CellState(int x, int y, Rect rect, Path cellPath) {
        this.x = x;
        this.y = y;
        this.boundary = rect;
        this.cellPath = cellPath;
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

    public Rect getBoundary() {
        return boundary;
    }

    public enum MyColor { RED, BLUE, BLANK }
}
