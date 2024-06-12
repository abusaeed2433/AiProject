package com.unknownn.aiproject.classes;

import android.content.Context;
import android.media.SoundPool;

import com.unknownn.aiproject.R;

import java.util.HashMap;
import java.util.Map;

public class SoundController {

    private static final int MOVE_DONE_SOUND = 1;
    private static final int GAME_OVER_SOUND = 2;

    private static SoundController instance = null;
    private final SoundPool soundPool;
    private final Map<SoundType,Integer> soundMap = new HashMap<>();

    private SoundController(Context context) {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .build();

        soundMap.put(SoundType.GAME_OVER, soundPool.load(context, R.raw.game_over,1));
        soundMap.put(SoundType.MOVE_DONE, soundPool.load(context, R.raw.move_done,1));
    }

    public static SoundController getInstance(Context context){
        if( instance == null ){
            instance = new SoundController(context);
        }
        return instance;
    }

    public void playSound(SoundType type){
        if(!soundMap.containsKey(type)) return;

        Integer id = soundMap.get(type);
        if(id == null) return;

        soundPool.autoPause();
        soundPool.play(id, 1,1,0,0,1);
    }

    public enum SoundType{
        GAME_OVER(GAME_OVER_SOUND), MOVE_DONE(MOVE_DONE_SOUND);
        final int id;
        SoundType(int id) {
            this.id = id;
        }
    }

}
