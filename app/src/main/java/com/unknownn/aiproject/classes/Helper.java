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

}
