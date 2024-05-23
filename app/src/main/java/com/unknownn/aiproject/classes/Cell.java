package com.unknownn.aiproject.classes;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Cell {
    final int x,y;
    protected CellState.MyColor myColor;

    public Cell(int x, int y, CellState.MyColor myColor) {
        this.x = x;
        this.y = y;
        this.myColor = myColor;
    }

    public Cell(Cell cell){
        this.x = cell.x;
        this.y = cell.y;
        this.myColor = cell.myColor;
    }

    public boolean isRed(){
        return myColor == CellState.MyColor.RED;
    }
    public void setMyColor(CellState.MyColor color){
        this.myColor = color;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x,y);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj == null) return false;

        if( !(obj instanceof Cell) ) return false;

        Cell cell = (Cell)obj;

        return x == cell.x && y == cell.y;
    }

    @NonNull
    @Override
    public String toString() {
//        return "("+ x +","+ y + ")";
        return myColor.name();
    }

}
