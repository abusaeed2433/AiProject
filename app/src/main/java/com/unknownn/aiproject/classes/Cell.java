package com.unknownn.aiproject.classes;

public class Cell {
    final int x,y;
    protected CellState.MyColor myColor;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.myColor = CellState.MyColor.BLANK;
    }

}
