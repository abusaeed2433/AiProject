package com.unknownn.aiproject.classes;

import android.app.Activity;
import android.widget.Toast;

public class Helper {
    private static Toast mToast = null;
    public static void showSafeToast(Activity activity, String message){
        try{
            if(mToast != null) mToast.cancel();
            mToast = Toast.makeText(activity,message,Toast.LENGTH_SHORT);
            mToast.show();
        }catch (Exception ignored){}
    }

    public static String convertBoardToString(CellState.MyColor[][] board, int N){
        final StringBuilder builder = new StringBuilder();
        for(int x=0; x<N; x++){
            for(int y=0; y<N; y++){
                final char ch = ( (board[x][y]) == CellState.MyColor.RED ) ? 'R' :
                        ( (board[x][y]) == CellState.MyColor.BLUE ) ? 'B' : 'L';

                builder.append(ch);
            }
        }
        return builder.toString();
    }

}
