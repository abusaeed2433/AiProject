package com.unknownn.aiproject.classes;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;

public class CellState extends Cell{
    final Hexagon hexagon;
    public String score = "";

    public CellState(int x, int y, Hexagon hexagon) {
        super(x,y, MyColor.BLANK);
        this.hexagon = hexagon;
    }

    public void reset(){
        this.myColor = MyColor.BLANK;
        score = "";
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


    public MyColor getMyColor() {
        return myColor;
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
    public float getLeftVertLength(){
        return hexagon.getLeftVertLength();
    }


    public enum MyColor {
        RED(1), BLUE(2), BLANK(0);

        final int id;
        MyColor(int id) {
            this.id = id;
        }

    }
}
